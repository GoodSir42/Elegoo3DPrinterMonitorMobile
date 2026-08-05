package eu.kutscheid.elegoomonitor.wear.sync

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import eu.kutscheid.elegoomonitor.shared.sync.WearSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Asks the phone to run a discovery scan and publish the result. This is the only outbound work the
 * watch does — a short Bluetooth message, rather than the UDP broadcast and per-printer websockets
 * the phone handles.
 */
object PhoneRefreshRequester {

    private val logger = Logger.withTag("WEAR")
    private const val TIMEOUT_SECONDS = 10L

    /** Sends the request to every connected node. Returns true if at least one accepted it. */
    suspend fun request(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val nodes = Tasks.await(
                Wearable.getNodeClient(context).connectedNodes,
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
            )
            if (nodes.isEmpty()) {
                logger.d { "no connected phone to request a refresh from" }
                return@withContext false
            }
            nodes.forEach { node ->
                Tasks.await(
                    Wearable.getMessageClient(context).sendMessage(
                        node.id,
                        WearSync.PATH_REQUEST_REFRESH,
                        ByteArray(0),
                    ),
                    TIMEOUT_SECONDS,
                    TimeUnit.SECONDS,
                )
            }
            SnapshotStore(context).recordRefreshRequest()
            true
        } catch (ex: Throwable) {
            logger.w(ex) { "requesting a refresh from the phone failed" }
            false
        }
    }
}
