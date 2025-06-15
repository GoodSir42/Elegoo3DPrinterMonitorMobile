package eu.kutscheid.elegoomonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PrinterStatus {
    Ready,
    Preparing,
    Retracting,
    Exposing,
    Lifting,
    Pausing,
    Paused,
    Cancelling,
    Finalizing,
    Cancelled,
    Complete,
    Unknown
}