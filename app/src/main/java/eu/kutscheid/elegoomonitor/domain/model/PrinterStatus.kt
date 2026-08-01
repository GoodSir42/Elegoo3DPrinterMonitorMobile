package eu.kutscheid.elegoomonitor.domain.model

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
    }
}
