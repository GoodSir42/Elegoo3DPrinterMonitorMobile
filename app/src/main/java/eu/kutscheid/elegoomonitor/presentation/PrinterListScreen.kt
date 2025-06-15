package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.PrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.PrinterType
import eu.kutscheid.elegoomonitor.ui.theme.ElegooMonitorTheme

@Composable
fun PrinterListScreen(printers: List<PrinterEntity>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(printers) { printer ->
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(printer.name, style = MaterialTheme.typography.headlineLarge)
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
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.printer_resolution, printer.resolution),
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text(
                                stringResource(
                                    R.string.printer_firmware_version,
                                    printer.firmwareVersion
                                ),
                                style = MaterialTheme.typography.labelSmall,
                            )
                            if (printer.progress > 0) {
                                Text(
                                    stringResource(
                                        R.string.printer_progress_percent,
                                        (printer.progress * 100)
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
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
                    }

                }
            }
        }
    }
}

private fun colorForStatus(status: PrinterStatus): Color {
    return when (status) {
        PrinterStatus.Ready, PrinterStatus.Paused -> Color(0xFF999999)
        PrinterStatus.Pausing, PrinterStatus.Cancelling -> Color(0xFFc2b85c)
        PrinterStatus.Cancelled -> Color(0xFFc25c5c)
        PrinterStatus.Complete -> Color(0xFF64c25c)
        // Default blue color
        else -> Color(0xFF0077cc)
    }
}

private fun foregroundColorForStatus(status: PrinterStatus): Color {
    return when (status) {
        PrinterStatus.Pausing, PrinterStatus.Cancelling -> Color.Black
        else -> Color.White
    }
}

@Preview
@Composable
private fun PrinterListScreenPreview() {
    ElegooMonitorTheme {
        PrinterListScreen(
            printers = listOf(
                PrinterEntity(
                    id = "123",
                    name = "Test Printer 1",
                    type = PrinterType.MARS_4,
                    status = PrinterStatus.Complete,
                    resolution = "12x13",
                    firmwareVersion = "v1.2.3",
                    progress = 0.0,
                ),
                PrinterEntity(
                    id = "1234",
                    name = "Test Printer 2",
                    type = PrinterType.SATURN_3,
                    status = PrinterStatus.Pausing,
                    resolution = "12x13",
                    firmwareVersion = "v1.2.3",
                    progress = 0.123456,
                ),
                PrinterEntity(
                    id = "1234",
                    name = "Test Printer with a very long name because it has to be tested",
                    type = PrinterType.UNKNOWN,
                    status = PrinterStatus.Preparing,
                    resolution = "12x13",
                    firmwareVersion = "v1.2.3",
                    progress = 0.123456,
                )
            )
        )
    }

}