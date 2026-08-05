package eu.kutscheid.elegoomonitor.data

import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.awaitClosed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

private const val BROADCAST_MESSAGE = "M99999"

private const val PORT = 3000

private const val BIND_ADDRESS = "0.0.0.0"

private const val BROADCAST_ADDRESS = "255.255.255.255"

/** How often the discovery datagram goes out while at least one caller is listening. */
private val BROADCAST_INTERVAL = 1.seconds

/**
 * How long the socket stays bound after the last caller leaves. Long enough that the UI, the widget
 * refresh and a watch-triggered refresh overlapping or following each other reuse one socket instead
 * of tearing it down and rebinding.
 */
private val SOCKET_LINGER = 5.seconds

/** Delay before rebinding after the socket failed, e.g. across a WiFi transition. */
private val REBIND_DELAY = 5.seconds

/** How long to wait for a closing socket to actually release the port. */
private val CLOSE_TIMEOUT = 5.seconds

/**
 * Discovers printers by broadcasting on the SDCP discovery port.
 *
 * The socket binds a fixed port, so only one may exist in the process at a time: a second bind fails
 * with "Address already in use". The UI, the widget refresh and the watch-triggered refresh all ask
 * for discovery independently and can easily overlap, so rather than one socket per caller this
 * exposes a single shared stream — the socket is opened for the first listener, reused by everyone
 * who joins while it is live, and closed once they have all gone.
 */
class UdpDataSource {

    private val logger = Logger.withTag("NETWORK")

    /** Outlives any individual caller, so the socket can span them. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Serialises bind against release. Ktor's `close()` only requests a close — the port stays held
     * until the selector completes it — so a restart that binds before the previous socket has
     * finished releasing would fail. Holding this across the whole socket lifetime means a restart
     * waits for the teardown of the socket it is replacing.
     */
    private val socketLock = Mutex()

    private val discoveries: SharedFlow<PrinterItem> = channelFlow {
        // Rebind after a failure — a WiFi transition closes the socket, and callers are still
        // collecting. Mirrors how the repository reconnects a printer's websocket.
        while (currentCoroutineContext().isActive) {
            try {
                socketLock.withLock {
                    useSocket { socket -> pump(socket) }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Throwable) {
                logger.e(ex) { "discovery socket failed, will rebind" }
            }
            delay(REBIND_DELAY)
        }
    }.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = SOCKET_LINGER.inWholeMilliseconds,
        ),
        replay = 0,
    )

    /**
     * Replies from printers on the local network, as long as the caller collects. Every caller gets
     * the same underlying socket; the returned flow can safely be collected concurrently from the
     * UI, a worker and a wear listener.
     *
     * Replies are not replayed, so a caller joining an already-running broadcast sees only what
     * arrives from then on — within [BROADCAST_INTERVAL] of subscribing, since the broadcast repeats.
     */
    fun startBroadcast(): Flow<PrinterItem> = discoveries

    /** Broadcasts on a timer while draining replies, until the collector goes away. */
    private suspend fun ProducerScope<PrinterItem>.pump(socket: BoundDatagramSocket) {
        coroutineScope {
            val broadcaster = launch { broadcastRepeatedly(socket) }
            try {
                receiveReplies(socket)
            } finally {
                broadcaster.cancel()
            }
        }
    }

    private suspend fun broadcastRepeatedly(socket: BoundDatagramSocket) {
        val target = InetSocketAddress(BROADCAST_ADDRESS, PORT)
        while (currentCoroutineContext().isActive) {
            logger.d { "sending discovery datagram" }
            socket.send(
                Datagram(
                    Buffer().apply { writeString(BROADCAST_MESSAGE, Charsets.UTF_8) },
                    target,
                )
            )
            delay(BROADCAST_INTERVAL)
        }
    }

    /**
     * Receives continuously rather than polling, so replies from several printers are all forwarded
     * as they land instead of one per broadcast tick.
     */
    private suspend fun ProducerScope<PrinterItem>.receiveReplies(socket: BoundDatagramSocket) {
        while (currentCoroutineContext().isActive) {
            val result = socket.incoming.receiveCatching()
            val datagram = result.getOrNull()
            if (datagram == null) {
                result.exceptionOrNull()?.let { logger.e(it) { "failed to get message" } }
                return
            }

            val messageString = datagram.packet.readString()
            logger.d { "Got message $messageString" }
            // Our own broadcast comes back to us, since we are bound to the port we send to.
            if (messageString == BROADCAST_MESSAGE) continue
            try {
                send(Json.decodeFromString<PrinterItem>(messageString))
            } catch (ex: Throwable) {
                logger.d(ex) { "can't parse message \"$messageString\", ignoring" }
            }
        }
    }

    /** Binds the socket, runs [block], and does not return until the port is released again. */
    private suspend fun <T> useSocket(block: suspend (BoundDatagramSocket) -> T): T {
        val selectorManager = ActorSelectorManager(Dispatchers.IO)
        val socket = try {
            aSocket(selectorManager)
                .udp()
                .bind(InetSocketAddress(BIND_ADDRESS, PORT)) { broadcast = true }
        } catch (ex: Throwable) {
            selectorManager.close()
            throw ex
        }
        logger.d { "bound discovery socket on port $PORT" }

        try {
            return block(socket)
        } finally {
            // NonCancellable: this runs while the flow is being cancelled, and skipping the wait
            // would let the next bind race a port that is still held.
            withContext(NonCancellable) {
                socket.close()
                // Bounded, because socketLock is held until this returns: a socket that never
                // reports closed must not block every later bind.
                if (withTimeoutOrNull(CLOSE_TIMEOUT) { socket.awaitClosed() } == null) {
                    logger.w { "discovery socket did not report closed within $CLOSE_TIMEOUT" }
                }
                selectorManager.close()
                logger.d { "released discovery socket" }
            }
        }
    }
}
