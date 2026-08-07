package eu.kutscheid.elegoomonitor.domain.model

import eu.kutscheid.elegoomonitor.shared.R
import kotlinx.serialization.Serializable

@Serializable
enum class PrinterStatus {
    Ready,
    Printing,
    Retracting,
    Exposing,
    Lifting,
    Pausing,
    Paused,
    Cancelling,
    Finalizing,
    Cancelled,
    Complete,
    Unknown;

    companion object {
        /** Maps an SDCP `PrintInfo.Status` code (from either the UDP or websocket payload) to a status. */
        fun fromStatusCode(code: Int?): PrinterStatus = when (code) {
            0 -> Ready
            1 -> Printing
            2 -> Retracting
            3 -> Exposing
            4 -> Lifting
            5, 6 -> Pausing
            7 -> Paused
            9 -> Cancelling
            12 -> Finalizing
            13 -> Cancelled
            16 -> Complete
            else -> Unknown
        }

        fun fromLiveStatusCode(code: Int?): PrinterStatus = when (code) {
            0 -> Ready
            13 -> Printing
            else -> Unknown
        }

    }
}

/**
 * Statuses that represent a print currently in progress — what the phone widget and the watch
 * complication both treat as "active".
 */
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

/**
 * String resource for a status label, usable outside Compose. Lives here rather than in each app so
 * the phone UI, the phone widget and the watch all resolve the same translated labels.
 */
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
