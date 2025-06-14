package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kutscheid.elegoomonitor.data.model.PrinterData

@Composable
fun PrinterListScreen(printers: List<PrinterData>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(printers) { printer ->
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("name: " + printer.attributes.name)
                    Text("machine name: " + printer.attributes.machineName)
                    Text("IP: " + printer.attributes.mainboardIP)
                    Text(
                        "status: " + when (printer.status.currentStatus) {
                            0 -> "Ready"
                            1 -> "Preparing"
                            2 -> "Retracting"
                            3 -> "Exposing"
                            4 -> "Lifting"
                            5 -> "Pausing"
                            7 -> "Paused"
                            9 -> "Cancelling"
                            12 -> "Finalizing"
                            13 -> "Cancelled"
                            16 -> "Complete"
                            else -> "Unknown"
                        }
                    )
                }
            }
        }
    }
}