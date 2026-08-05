package eu.kutscheid.elegoomonitor.shared.sync

import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The single active print the watch renders. The watch never discovers printers itself — UDP
 * broadcast plus a websocket per printer would keep its radio awake far too long — so the phone
 * pushes this snapshot over the Wearable Data Layer instead.
 */
@Serializable
data class PrintSnapshot(
    val printerName: String,
    /** Print progress in the range 0f..1f. */
    val progress: Float,
    val status: PrinterStatus,
    val currentLayer: Long = 0L,
    val totalLayers: Long = 0L,
    /** Remaining print time in seconds, or 0 when the phone could not determine it. */
    val remainingSeconds: Long = 0L,
    /** When the phone captured this, used by the watch to decide whether to ask for a fresh one. */
    val capturedAtEpochMs: Long = 0L,
)

/**
 * What the phone published on its last fetch. [snapshot] is null when the fetch succeeded but no
 * printer was actively printing, which the watch must show differently from "never heard from the
 * phone".
 */
@Serializable
data class WearPayload(
    val snapshot: PrintSnapshot? = null,
    val capturedAtEpochMs: Long = 0L,
)

/** Paths and keys shared by the phone publisher and the watch listener. */
object WearSync {
    /** Data Layer item path carrying the [WearPayload]. */
    const val PATH_ACTIVE_PRINT = "/elegoo/active-print"

    /** Message path the watch sends to ask the phone for a fresh scan. */
    const val PATH_REQUEST_REFRESH = "/elegoo/request-refresh"

    /** DataMap key holding the JSON-encoded [WearPayload]. */
    const val KEY_PAYLOAD_JSON = "payload_json"

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(payload: WearPayload): String = json.encodeToString(payload)

    fun decode(raw: String?): WearPayload? =
        raw?.let { runCatching { json.decodeFromString<WearPayload>(it) }.getOrNull() }
}
