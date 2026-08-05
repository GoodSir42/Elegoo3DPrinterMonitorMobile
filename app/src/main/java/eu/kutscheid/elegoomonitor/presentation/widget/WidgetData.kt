package eu.kutscheid.elegoomonitor.presentation.widget

import androidx.datastore.preferences.core.stringPreferencesKey
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

internal object WidgetState {
    /** Preferences key holding the serialized [WidgetData]. */
    val DataKey = stringPreferencesKey("widget_data_json")

    val json = Json { ignoreUnknownKeys = true }

    fun encode(data: WidgetData): String = json.encodeToString(data)

    fun decode(raw: String?): WidgetData =
        raw?.let { runCatching { json.decodeFromString<WidgetData>(it) }.getOrNull() }
            ?: WidgetData()
}
