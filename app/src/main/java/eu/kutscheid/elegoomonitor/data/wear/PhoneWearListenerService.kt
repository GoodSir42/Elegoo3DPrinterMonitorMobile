package eu.kutscheid.elegoomonitor.data.wear

import co.touchlab.kermit.Logger
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import eu.kutscheid.elegoomonitor.presentation.widget.PrinterWidgetScheduler
import eu.kutscheid.elegoomonitor.shared.sync.WearSync

/**
 * Handles refresh requests from the watch. Runs a discovery scan now, which republishes to the data
 * layer as a side effect of the widget refresh.
 */
class PhoneWearListenerService : WearableListenerService() {

    private val logger = Logger.withTag("WEAR")

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != WearSync.PATH_REQUEST_REFRESH) {
            super.onMessageReceived(event)
            return
        }
        logger.d { "watch requested a refresh" }
        WearInterest(applicationContext).recordRequest()
        PrinterWidgetScheduler.scheduleNow(applicationContext)
    }
}
