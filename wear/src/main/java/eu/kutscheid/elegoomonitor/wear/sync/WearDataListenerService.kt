package eu.kutscheid.elegoomonitor.wear.sync

import co.touchlab.kermit.Logger
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import eu.kutscheid.elegoomonitor.shared.sync.WearSync
import eu.kutscheid.elegoomonitor.wear.complication.requestComplicationUpdate

/**
 * Receives snapshots pushed by the phone, caches them and nudges the complication to redraw. This is
 * what makes a phone widget refresh show up on the watch face: both are fed by the same phone-side
 * fetch.
 */
class WearDataListenerService : WearableListenerService() {

    private val logger = Logger.withTag("WEAR")

    override fun onDataChanged(events: DataEventBuffer) {
        var updated = false
        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            if (event.dataItem.uri.path != WearSync.PATH_ACTIVE_PRINT) return@forEach

            val json = DataMapItem.fromDataItem(event.dataItem)
                .dataMap
                .getString(WearSync.KEY_PAYLOAD_JSON)
            val payload = WearSync.decode(json)
            if (payload == null) {
                logger.w { "received an undecodable snapshot, ignoring" }
                return@forEach
            }
            SnapshotStore(this).save(payload)
            updated = true
        }

        if (updated) {
            logger.d { "cached a new snapshot from the phone" }
            requestComplicationUpdate(this)
        }
    }
}
