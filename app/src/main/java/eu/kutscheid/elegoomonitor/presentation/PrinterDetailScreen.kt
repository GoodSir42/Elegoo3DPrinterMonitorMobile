package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.PrinterType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PrinterDetailScreen(printerId: String, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<PrinterDetailViewModel>().also { it.printerId = printerId }
    val printer by viewModel.printer.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) { // Added modifier to the Column
        printer?.let { printer ->
            Text(text = "Name: ${printer.name}", style = MaterialTheme.typography.headlineLarge)
            Row {
                Image(
                    painterResource(
                        when (printer.type) {
                            PrinterType.MARS_4 -> R.drawable.printer_mars4ultra
                            PrinterType.SATURN_3 -> R.drawable.printer_saturn3ultra
                            PrinterType.UNKNOWN -> R.drawable.printer_default
                        }
                    ),
                    contentDescription = stringResource(
                        R.string.content_description_printer_image,
                        printer.type.displayName
                    ),
                    modifier = Modifier.size(80.dp)
                )
                Badge(
                    containerColor = colorForStatus(printer.status),
                    contentColor = foregroundColorForStatus(printer.status),
                ) {
                    Text(
                        when (printer.status) {
                            PrinterStatus.Ready -> stringResource(R.string.printer_status_ready)
                            PrinterStatus.Preparing -> stringResource(R.string.printer_status_preparing)
                            PrinterStatus.Retracting -> stringResource(R.string.printer_status_retracting)
                            PrinterStatus.Exposing -> stringResource(R.string.printer_status_exposing)
                            PrinterStatus.Lifting -> stringResource(R.string.printer_status_lifting)
                            PrinterStatus.Pausing -> stringResource(R.string.printer_status_pausing)
                            PrinterStatus.Paused -> stringResource(R.string.printer_status_paused)
                            PrinterStatus.Cancelling -> stringResource(R.string.printer_status_cancelling)
                            PrinterStatus.Finalizing -> stringResource(R.string.printer_status_finalizing)
                            PrinterStatus.Cancelled -> stringResource(R.string.printer_status_cancelled)
                            PrinterStatus.Complete -> stringResource(R.string.printer_status_complete)
                            PrinterStatus.Unknown -> stringResource(R.string.printer_status_unknown)
                        },
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
            Text(text = "Firmware Version: ${printer.firmwareVersion}")
            Text(text = "Progress: ${printer.progress}")
            Text(text = "Current Layer: ${printer.currentLayer} / ${printer.totalLayers}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrinterDetailScreenPreview() {
    PrinterDetailScreen(printerId = "123")
}