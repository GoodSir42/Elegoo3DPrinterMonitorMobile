package eu.kutscheid.elegoomonitor.presentation.widget

import androidx.datastore.preferences.core.stringPreferencesKey
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Snapshot the widget renders. Persisted as JSON in the Glance state store, updated by the worker. */
@Serializable
data class WidgetData(
    val printers: List<WidgetPrinter> = emptyList(),
    val updatedAtEpochMs: Long = 0L,
    /** Distinguishes "not fetched yet" from "fetched, nothing printing". */
    val hasData: Boolean = false,
)

@Serializable
data class WidgetPrinter(
    val name: String,
    /** Print progress in the range 0f..1f. */
    val progress: Float,
    val status: PrinterStatus,
)

/** Statuses that represent a print currently in progress — what the widget considers "active". */
val PrinterStatus.isActivePrint: Boolean
    get() = when (this) {
        PrinterStatus.Printing,
        PrinterStatus.Retracting,
        PrinterStatus.Exposing,
        PrinterStatus.Lifting,
        PrinterStatus.Pausing,
        PrinterStatus.Paused,
        PrinterStatus.Finalizing -> true

        PrinterStatus.Ready,
        PrinterStatus.Cancelling,
        PrinterStatus.Cancelled,
        PrinterStatus.Complete,
        PrinterStatus.Unknown -> false
    }

/** String resource for a status label, usable outside Compose (the widget resolves it via [android.content.Context]). */
fun PrinterStatus.labelResId(): Int = when (this) {
    PrinterStatus.Ready -> R.string.printer_status_ready
    PrinterStatus.Printing -> R.string.printer_status_printing
    PrinterStatus.Retracting -> R.string.printer_status_retracting
    PrinterStatus.Exposing -> R.string.printer_status_exposing
    PrinterStatus.Lifting -> R.string.printer_status_lifting
    PrinterStatus.Pausing -> R.string.printer_status_pausing
    PrinterStatus.Paused -> R.string.printer_status_paused
    PrinterStatus.Cancelling -> R.string.printer_status_cancelling
    PrinterStatus.Finalizing -> R.string.printer_status_finalizing
    PrinterStatus.Cancelled -> R.string.printer_status_cancelled
    PrinterStatus.Complete -> R.string.printer_status_complete
    PrinterStatus.Unknown -> R.string.printer_status_unknown
}

internal object WidgetState {
    /** Preferences key holding the serialized [WidgetData]. */
    val DataKey = stringPreferencesKey("widget_data_json")

    val json = Json { ignoreUnknownKeys = true }

    fun encode(data: WidgetData): String = json.encodeToString(data)

    fun decode(raw: String?): WidgetData =
        raw?.let { runCatching { json.decodeFromString<WidgetData>(it) }.getOrNull() }
            ?: WidgetData()
}
