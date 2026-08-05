package eu.kutscheid.elegoomonitor.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.data.wear.WearInterest
import eu.kutscheid.elegoomonitor.data.wear.WearSyncPublisher
import eu.kutscheid.elegoomonitor.domain.DataRepository
import eu.kutscheid.elegoomonitor.domain.model.FullPrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.isActivePrint
import eu.kutscheid.elegoomonitor.shared.sync.PrintSnapshot
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Does a single quick discovery fetch, writes the active prints into the widget's Glance state and
 * publishes the leading one to any paired watch, then schedules the next run. WorkManager's real
 * periodic minimum is 15 minutes, so a ~5-minute cadence is achieved by a self-rescheduling one-time
 * job. The OS may still stretch the interval under Doze.
 *
 * The watch shares this fetch rather than scanning for itself, which would keep its radio awake far
 * longer than a watch battery tolerates.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
    private val dataRepository: DataRepository,
) : CoroutineWorker(context, params) {

    private val logger = Logger.withTag("WIDGET")

    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        val capturedAt = Clock.System.now().toEpochMilliseconds()

        val active = try {
            dataRepository.snapshotFullPrinters(FETCH_WINDOW)
                .filter { it.status.isActivePrint }
                // Most-progressed first: the widget shows the leading prints and the watch the first.
                .sortedByDescending { it.progress }
        } catch (ex: Throwable) {
            logger.e(ex) { "widget fetch failed" }
            null
        }

        if (active != null) {
            writeState(
                WidgetData(
                    printers = active.take(MAX_WIDGET_PRINTERS).map {
                        WidgetPrinter(
                            name = it.name,
                            progress = it.progress.toFloat(),
                            status = it.status
                        )
                    },
                    updatedAtEpochMs = capturedAt,
                    hasData = true
                )
            )
            WearSyncPublisher(applicationContext).publish(
                snapshot = active.firstOrNull()?.toPrintSnapshot(capturedAt),
                capturedAtEpochMs = capturedAt,
            )
        }

        val stillNeeded = rescheduleIfStillNeeded()
        return when {
            active != null -> Result.success()
            // A transient failure is worth retrying, but not once nothing is displaying the data.
            stillNeeded -> Result.retry()
            else -> Result.success()
        }
    }

    /**
     * Continues the chain only while something is actually displaying the data — a placed widget, or
     * a watch complication that has asked recently. Otherwise the chain would keep scanning forever
     * after the last widget was removed. Returns whether the chain was kept alive.
     */
    private suspend fun rescheduleIfStillNeeded(): Boolean {
        val hasWidgets = GlanceAppWidgetManager(applicationContext)
            .getGlanceIds(PrinterWidget::class.java)
            .isNotEmpty()
        val needed = hasWidgets || WearInterest(applicationContext).isWatchInterested()
        if (needed) {
            PrinterWidgetScheduler.schedule(applicationContext)
        } else {
            logger.d { "no widget and no recent watch request, stopping the refresh chain" }
        }
        return needed
    }

    private suspend fun writeState(data: WidgetData) {
        val manager = GlanceAppWidgetManager(applicationContext)
        val ids = manager.getGlanceIds(PrinterWidget::class.java)
        val encoded = WidgetState.encode(data)
        ids.forEach { id ->
            updateAppWidgetState(
                applicationContext,
                PreferencesGlanceStateDefinition,
                id
            ) { prefs ->
                prefs.toMutablePreferences().apply { this[WidgetState.DataKey] = encoded }
            }
        }
        PrinterWidget().updateAll(applicationContext)
    }

    companion object {
        /** Window we stay subscribed to discovery: enough for the UDP reply and a first websocket frame. */
        val FETCH_WINDOW = 8.seconds
        const val MAX_WIDGET_PRINTERS = 2
    }
}

/** Maps a discovered printer onto the compact snapshot the watch complication renders. */
private fun FullPrinterEntity.toPrintSnapshot(capturedAtEpochMs: Long) = PrintSnapshot(
    printerName = name,
    progress = progress.toFloat(),
    status = status,
    currentLayer = currentLayer,
    totalLayers = totalLayers,
    remainingSeconds = estimatedTime.inWholeSeconds,
    capturedAtEpochMs = capturedAtEpochMs,
)

/** Enqueues the self-rescheduling widget refresh chain. */
object PrinterWidgetScheduler {
    private const val UNIQUE_WORK = "printer_widget_refresh"
    private val INTERVAL = 10.minutes

    private fun request(delaySeconds: Long) =
        OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

    /** Schedules the next refresh ~5 minutes out (used both to bootstrap and to chain from the worker). */
    fun schedule(context: Context) {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_WORK,
                ExistingWorkPolicy.REPLACE,
                request(INTERVAL.inWholeSeconds)
            )
    }

    /** Runs a refresh as soon as constraints allow, then continues the 5-minute chain. */
    fun scheduleNow(context: Context) {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.REPLACE, request(0))
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
    }
}
