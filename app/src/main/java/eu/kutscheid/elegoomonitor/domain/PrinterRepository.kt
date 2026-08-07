package eu.kutscheid.elegoomonitor.domain

import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.data.UdpDataSource
import eu.kutscheid.elegoomonitor.data.VideoStreamDataSource
import eu.kutscheid.elegoomonitor.data.WebSocketDataSource
import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import eu.kutscheid.elegoomonitor.data.model.StatusMessage
import eu.kutscheid.elegoomonitor.domain.model.FullPrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val RECONNECT_DELAY = 5.seconds

private const val VIDEO_STREAM_PORT = 3031

class DataRepository(
    udpDataSource: UdpDataSource,
    private val wsDataSource: WebSocketDataSource,
    private val videoDataSource: VideoStreamDataSource,
) {
    private val logger = Logger.withTag("NETWORK")
    private val scope = CoroutineScope(Dispatchers.IO)

    /** Accumulated discovery results, keyed by [PrinterItem.id]. Shared so the UDP socket binds once. */
    private val discoveredPrinters: Flow<List<PrinterItem>> = udpDataSource
        .startBroadcast()
        .runningFold(emptyList<PrinterItem>()) { initialList, item ->
            val existingIndex = initialList.indexOfFirst { it.id == item.id }
            if (existingIndex > -1) {
                initialList.toMutableList().apply { set(existingIndex, item) }
            } else {
                initialList + item
            }
        }
        .shareIn(scope, started = SharingStarted.WhileSubscribed(5000), replay = 1)

    /**
     * Latest live status per mainboard id, collected from the websocket of every discovered printer
     * whose protocol version reports status that way. Restarts when that set of printers changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveStatus: Flow<Map<String, StatusMessage>> = discoveredPrinters
        .map { printers ->
            printers.filter { it.reportsStatusOverWebSocket }
                .associate { it.data.attributes.mainboardID to it.data.attributes.mainboardIP }
        }
        .distinctUntilChanged()
        .flatMapLatest { targets ->
            if (targets.isEmpty()) {
                flowOf(emptyMap())
            } else {
                merge(*targets.values.distinct().map(::liveStatusOf).toTypedArray())
                    .runningFold(emptyMap<String, StatusMessage>()) { statuses, message ->
                        statuses + (message.mainboardID to message)
                    }
            }
        }
        .onStart { emit(emptyMap()) }

    /**
     * Discovery data with any live websocket status overlaid, so a socket-reporting printer looks
     * identical to one that returned its status directly in the UDP payload.
     */
    private val printers: Flow<List<FullPrinterEntity>> =
        combine(discoveredPrinters, liveStatus) { printerItems, statusByMainboard ->
            printerItems.map { item ->
                val entity = FullPrinterEntity(item)
                statusByMainboard[entity.mainboardID]?.let(entity::withLiveStatus) ?: entity
            }
        }.shareIn(scope, started = SharingStarted.WhileSubscribed())

    fun getPrinterList(): Flow<List<PrinterEntity>> =
        printers.map { fullPrinters -> fullPrinters.map { PrinterEntity(it) } }

    /**
     * One-shot fetch for background callers (e.g. the home-screen widget) that must not hold a
     * persistent connection. Subscribes to the discovery pipeline for [window] — long enough for the
     * broadcast reply and, for websocket-reporting printers, the first status frame to arrive — then
     * unsubscribes so the socket closes. Returns the most recent snapshot seen in that window.
     */
    suspend fun snapshotPrinters(window: Duration): List<PrinterEntity> =
        snapshotFullPrinters(window).map { PrinterEntity(it) }

    /**
     * As [snapshotPrinters], but keeping the layer and timing fields. The widget refresh uses this so
     * a single discovery window can feed both the widget and the watch complication.
     */
    suspend fun snapshotFullPrinters(window: Duration): List<FullPrinterEntity> {
        var latest = emptyList<FullPrinterEntity>()
        withTimeoutOrNull(window) {
            latest = printers.firstOrNull() ?: emptyList()
        }
        return latest
    }

    fun getPrinterDetail(id: String): Flow<FullPrinterEntity> =
        printers.mapNotNull { allPrinters -> allPrinters.find { it.id == id } }
            .distinctUntilChanged()

    /**
     * Returns the printer's MJPEG stream URL if a HEAD probe succeeds, or null if the printer does
     * not offer a stream (or is unreachable). Callers can display the stream only when non-null.
     */
    suspend fun getVideoStreamUrl(ipAddress: String): String? {
        val url = "http://$ipAddress:$VIDEO_STREAM_PORT/video"
        return url.takeIf { videoDataSource.isStreamAvailable(it) }
    }

    /** Keeps a printer's websocket open, reconnecting with a delay after a normal close or an error. */
    private fun liveStatusOf(ip: String): Flow<StatusMessage> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                emitAll(wsDataSource.connect(ip))
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Throwable) {
                logger.d(ex) { "websocket to $ip failed, will reconnect" }
            }
            delay(RECONNECT_DELAY)
        }
    }
}

/**
 * Printers speaking protocol V3 or newer (e.g. the Centauri Carbon) omit `Status` from the UDP
 * discovery reply and instead push it over the websocket; older ones inline it in the reply.
 */
private val PrinterItem.reportsStatusOverWebSocket: Boolean
    get() = data.attributes.protocolVersion.protocolMajorVersion() >= 3

/** Parses the leading number of an Elegoo protocol string such as "V3.0.0". */
private fun String.protocolMajorVersion(): Int =
    trimStart('V', 'v').substringBefore('.').toIntOrNull() ?: 0
