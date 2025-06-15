package eu.kutscheid.elegoomonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PrinterType(val displayName: String) {
    MARS_4("ELEGOO Mars 4 Ultra"),
    SATURN_3("ELEGOO Saturn 3 Ultra"),
    UNKNOWN("Unknown")
}