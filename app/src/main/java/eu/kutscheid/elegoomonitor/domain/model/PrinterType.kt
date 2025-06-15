package eu.kutscheid.elegoomonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PrinterType {
    MARS_4,
    SATURN_3,
    UNKNOWN
}