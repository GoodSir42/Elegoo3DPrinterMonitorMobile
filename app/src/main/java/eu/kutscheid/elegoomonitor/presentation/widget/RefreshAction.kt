package eu.kutscheid.elegoomonitor.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/** Tapping the "Refresh" affordance triggers an immediate fetch, which then re-arms the 5-min chain. */
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        PrinterWidgetScheduler.scheduleNow(context)
    }
}
