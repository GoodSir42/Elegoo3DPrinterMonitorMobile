package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.PrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.PrinterType
import eu.kutscheid.elegoomonitor.ui.theme.ElegooMonitorTheme

/**
 * Minimum card width; [GridCells.Adaptive] uses it to pick the column count, so phones show a
 * single column while foldables and tablets fan out into two or more.
 */
private val MinCardWidth = 340.dp

@Composable
fun PrinterListScreen(
    printers: List<PrinterEntity>,
    onPrinterSelected: (printerId: String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = MinCardWidth),
        modifier = modifier,
        contentPadding = contentPadding + PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(printers, key = { it.id }) { printer ->
            PrinterCard(
                printer = printer,
                onClick = { onPrinterSelected(printer.id) },
            )
        }
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
    }
}

@Composable
private fun PrinterCard(
    printer: PrinterEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .sharedBounds(printerDetails(printer.id))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = printer.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .sharedBounds(printerNameKey(printer.id))
                        .weight(1f)
                )
                StatusPill(
                    printer.status,
                    modifier = Modifier.sharedBounds(printerStatusKey(printer.id)),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(
                        when (printer.type) {
                            PrinterType.MARS_4 -> R.drawable.printer_mars4ultra
                            PrinterType.SATURN_3 -> R.drawable.printer_saturn3ultra
                            PrinterType.CENTAURI_CARBON -> R.drawable.printer_default
                            PrinterType.UNKNOWN -> R.drawable.printer_default
                        }
                    ),
                    contentDescription = stringResource(
                        R.string.content_description_printer_image,
                        printer.type.displayName
                    ),
                    modifier = Modifier
                        .size(72.dp)
                        .printerSharedElement(printerImageKey(printer.id))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (printer.resolution != null) {
                        Text(
                            text = stringResource(R.string.printer_resolution, printer.resolution),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.printerSharedElement(printerResolutionKey(printer.id))
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.printer_firmware_version,
                            printer.firmwareVersion
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (printer.progress > 0) {
                PrinterProgress(printer.progress.toFloat())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrinterProgress(progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.detail_label_complete),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.printer_progress_percent_short, progress * 100),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewScreenSizes
@Composable
private fun PrinterListScreenPreview() {
    ElegooMonitorTheme {
        PrinterListScreen(
            onPrinterSelected = {},
            printers = listOf(
                PrinterEntity(
                    id = "123",
                    name = "Test Printer 1",
                    type = PrinterType.MARS_4,
                    status = PrinterStatus.Complete,
                    resolution = "4098 x 2560",
                    firmwareVersion = "v1.2.3",
                    progress = 1.0,
                ),
                PrinterEntity(
                    id = "1234",
                    name = "Test Printer 2",
                    type = PrinterType.SATURN_3,
                    status = PrinterStatus.Printing,
                    resolution = "11520 x 5120",
                    firmwareVersion = "v1.2.3",
                    progress = 0.42,
                ),
                PrinterEntity(
                    id = "12345",
                    name = "Test Printer with a very long name because it has to be tested",
                    type = PrinterType.UNKNOWN,
                    status = PrinterStatus.Pausing,
                    resolution = "12x13",
                    firmwareVersion = "v1.2.3",
                    progress = 0.123456,
                )
            )
        )
    }
}
