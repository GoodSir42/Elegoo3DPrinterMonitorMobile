package eu.kutscheid.elegoomonitor.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed class Destination : NavKey {
    @Serializable
    data object InitialLoading : Destination()

    @Serializable
    data object PrinterList : Destination()


    @Serializable
    data class PrinterDetail(val printerId: String) : Destination()

    @Serializable
    data object LicenseOverview : Destination()
}