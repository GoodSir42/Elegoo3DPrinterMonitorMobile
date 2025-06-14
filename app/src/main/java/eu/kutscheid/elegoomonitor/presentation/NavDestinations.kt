package eu.kutscheid.elegoomonitor.presentation

import androidx.navigation3.runtime.NavKey
import eu.kutscheid.elegoomonitor.data.model.PrinterData
import kotlinx.serialization.Serializable


sealed class Destination : NavKey {
    @Serializable
    data object InitialLoading : Destination()

    @Serializable
    data class PrinterList(val printers: List<PrinterData>) : Destination()
}