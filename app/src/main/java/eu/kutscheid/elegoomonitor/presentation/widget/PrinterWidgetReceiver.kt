package eu.kutscheid.elegoomonitor.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class PrinterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrinterWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // First widget added: fetch now and start the recurring chain.
        PrinterWidgetScheduler.scheduleNow(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Last widget removed: stop refreshing.
        PrinterWidgetScheduler.cancel(context)
    }
}
