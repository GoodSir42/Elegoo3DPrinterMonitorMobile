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
import eu.kutscheid.elegoomonitor.domain.DataRepository
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Does a single quick discovery fetch, writes the active prints into the widget's Glance state, then
 * schedules the next run. WorkManager's real periodic minimum is 15 minutes, so a ~5-minute cadence
 * is achieved by a self-rescheduling one-time job. The OS may still stretch the interval under Doze.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val logger = Logger.withTag("WIDGET")

    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        val repository = GlobalContext.get().get<DataRepository>()

        val data = try {
            val active = repository.snapshotPrinters(FETCH_WINDOW)
                .filter { it.status.isActivePrint }
                .sortedByDescending { it.progress }
                .take(MAX_WIDGET_PRINTERS)
                .map {
                    WidgetPrinter(
                        name = it.name,
                        progress = it.progress.toFloat(),
                        status = it.status
                    )
                }
            WidgetData(
                printers = active,
                updatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                hasData = true
            )
        } catch (ex: Throwable) {
            logger.e(ex) { "widget fetch failed" }
            null
        }

        if (data != null) {
            writeState(data)
        }

        // Reschedule regardless, so a transient failure doesn't stop the recurring updates.
        PrinterWidgetScheduler.schedule(applicationContext)
        return if (data != null) Result.success() else Result.retry()
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
