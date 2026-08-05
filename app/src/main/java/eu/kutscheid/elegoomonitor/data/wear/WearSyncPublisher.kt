package eu.kutscheid.elegoomonitor.data.wear

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import eu.kutscheid.elegoomonitor.shared.sync.PrintSnapshot
import eu.kutscheid.elegoomonitor.shared.sync.WearPayload
import eu.kutscheid.elegoomonitor.shared.sync.WearSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Pushes the current active print to any paired watch over the Wearable Data Layer.
 *
 * The watch deliberately does no discovery of its own: UDP broadcast plus a websocket per printer
 * would keep its radio awake far longer than a watch battery tolerates. Instead the phone — which
 * already scans for the home-screen widget — publishes the result here, and the watch only ever
 * reads a cached snapshot.
 */
class WearSyncPublisher(private val context: Context) {

    private val logger = Logger.withTag("WEAR")

    /**
     * Publishes [snapshot] (null meaning "scan succeeded, nothing printing"). Safe to call when no
     * watch is paired — the data item is simply stored locally until one connects.
     */
    suspend fun publish(snapshot: PrintSnapshot?, capturedAtEpochMs: Long) {
        val payload = WearPayload(snapshot = snapshot, capturedAtEpochMs = capturedAtEpochMs)
        val request = PutDataMapRequest.create(WearSync.PATH_ACTIVE_PRINT)
            .apply { dataMap.putString(WearSync.KEY_PAYLOAD_JSON, WearSync.encode(payload)) }
            .asPutDataRequest()
            // The watch should see a finished or newly started print promptly, not on the next
            // opportunistic sync.
            .setUrgent()

        try {
            withContext(Dispatchers.IO) {
                Tasks.await(
                    Wearable.getDataClient(context).putDataItem(request),
                    PUT_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS,
                )
            }
            logger.d { "published snapshot to data layer: ${snapshot?.printerName ?: "none"}" }
        } catch (ex: Throwable) {
            // A missing or outdated Play services install must not fail the widget refresh.
            logger.w(ex) { "publishing snapshot to the data layer failed" }
        }
    }

    private companion object {
        const val PUT_TIMEOUT_SECONDS = 10L
    }
}
