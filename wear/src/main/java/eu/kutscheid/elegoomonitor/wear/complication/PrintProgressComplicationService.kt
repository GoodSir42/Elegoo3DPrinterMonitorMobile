package eu.kutscheid.elegoomonitor.wear.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.labelResId
import eu.kutscheid.elegoomonitor.shared.sync.PrintSnapshot
import eu.kutscheid.elegoomonitor.wear.R
import eu.kutscheid.elegoomonitor.wear.presentation.MainActivity
import eu.kutscheid.elegoomonitor.wear.sync.PhoneRefreshRequester
import eu.kutscheid.elegoomonitor.wear.sync.SnapshotStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Shows the progress of the print the phone reported as leading.
 *
 * Always answers from the cached snapshot so the watch face never waits on Bluetooth, and separately
 * nudges the phone to publish a fresh one. That fresh snapshot arrives at
 * [eu.kutscheid.elegoomonitor.wear.sync.WearDataListenerService], which then asks for another
 * complication update — so the value shown is at most one update period behind.
 */
class PrintProgressComplicationService : SuspendingComplicationDataSourceService() {

    private val logger = Logger.withTag("WEAR")

    /** Refresh requests outlive the complication response, which must return promptly. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val store = SnapshotStore(this)

        // Asking on every update is also how the phone learns a watch is still displaying this, which
        // keeps its refresh chain alive even with no home-screen widget placed.
        if (store.shouldRequestRefresh()) {
            scope.launch { PhoneRefreshRequester.request(applicationContext) }
        }

        val payload = store.load()
        if (payload == null) {
            logger.d { "no snapshot cached yet" }
            return NoDataComplicationData()
        }
        val snapshot = payload.snapshot
        return if (snapshot == null) {
            idleData(request.complicationType)
        } else {
            activeData(request.complicationType, snapshot)
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData = activeData(
        type = type,
        snapshot = PrintSnapshot(
            printerName = "Saturn 3 Ultra",
            progress = 0.65f,
            status = PrinterStatus.Printing,
        ),
    )

    /** The phone scanned successfully but nothing is printing. */
    private fun idleData(type: ComplicationType): ComplicationData {
        val description = getString(R.string.complication_no_active_print)
        return when (type) {
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 0f,
                min = 0f,
                max = MAX_PERCENT,
                contentDescription = description.asComplicationText(),
            )
                .setText(getString(R.string.complication_idle_text).asComplicationText())
                .setMonochromaticImage(icon())
                .setTapAction(openApp())
                .build()

            else -> ShortTextComplicationData.Builder(
                text = getString(R.string.complication_idle_text).asComplicationText(),
                contentDescription = description.asComplicationText(),
            )
                .setMonochromaticImage(icon())
                .setTapAction(openApp())
                .build()
        }
    }

    private fun activeData(type: ComplicationType, snapshot: PrintSnapshot): ComplicationData {
        val percent = (snapshot.progress.coerceIn(0f, 1f) * 100).roundToInt()
        val percentText = getString(R.string.complication_percent, percent)
        val description = getString(
            R.string.complication_progress_description,
            snapshot.printerName,
            percent,
            getString(snapshot.status.labelResId()),
        )

        return when (type) {
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = percent.toFloat(),
                min = 0f,
                max = MAX_PERCENT,
                contentDescription = description.asComplicationText(),
            )
                .setText(percentText.asComplicationText())
                .setTitle(snapshot.printerName.asComplicationText())
                .setMonochromaticImage(icon())
                .setTapAction(openApp())
                .build()

            else -> ShortTextComplicationData.Builder(
                text = percentText.asComplicationText(),
                contentDescription = description.asComplicationText(),
            )
                .setTitle(snapshot.printerName.asComplicationText())
                .setMonochromaticImage(icon())
                .setTapAction(openApp())
                .build()
        }
    }

    private fun icon() = MonochromaticImage.Builder(
        image = android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_print_layers)
    ).build()

    // No FLAG_ACTIVITY_NEW_TASK: the system adds it for PendingIntent launches, and setting it
    // explicitly confuses the Wear OS recents stack.
    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val MAX_PERCENT = 100f
    }
}

private fun String.asComplicationText() = PlainComplicationText.Builder(this).build()

/** Tells the platform this data source has a new value, so watch faces showing it redraw. */
fun requestComplicationUpdate(context: Context) {
    ComplicationDataSourceUpdateRequester.create(
        context = context,
        complicationDataSourceComponent = ComponentName(
            context,
            PrintProgressComplicationService::class.java,
        ),
    ).requestUpdateAll()
}
