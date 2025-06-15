package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kutscheid.elegoomonitor.domain.model.PrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus

@Composable
fun PrinterListScreen(printers: List<PrinterEntity>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(printers) { printer ->
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("name: " + printer.name)
                    Text("machine type: " + printer.type)
                    Text(
                        "status: " + when (printer.status) {
                            PrinterStatus.Ready -> "Ready"
                            PrinterStatus.Preparing -> "Preparing"
                            PrinterStatus.Retracting -> "Retracting"
                            PrinterStatus.Exposing -> "Exposing"
                            PrinterStatus.Lifting -> "Lifting"
                            PrinterStatus.Pausing -> "Pausing"
                            PrinterStatus.Paused -> "Paused"
                            PrinterStatus.Cancelling -> "Cancelling"
                            PrinterStatus.Finalizing -> "Finalizing"
                            PrinterStatus.Cancelled -> "Cancelled"
                            PrinterStatus.Complete -> "Complete"
                            PrinterStatus.Unknown -> "Unknown"
                        }
                    )
                }
            }
        }
    }
}