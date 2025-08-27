package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.FullPrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.PrinterType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val timeFormatter = DateTimeComponents.Format {
    hour()
    char(':')
    minute()
}

@OptIn(ExperimentalTime::class)
@Composable
fun PrinterDetailScreen(printer: FullPrinterEntity, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) { // Added modifier to the Column
        Text(text = printer.name, style = MaterialTheme.typography.headlineLarge)
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
                modifier = Modifier.size(160.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        R.string.printer_firmware_version,
                        printer.firmwareVersion
                    )
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
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            val progress by animateFloatAsState(printer.progress.toFloat())
            CircularProgressIndicator(
                progress = {
                    progress
                },
                strokeWidth = 40.dp,
                strokeCap = StrokeCap.Butt,
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center)
            )
            Text(
                text = stringResource(
                    R.string.printer_progress_percent,
                    (progress * 100)
                ),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text(
            text = stringResource(
                R.string.current_layer,
                printer.currentLayer,
                printer.totalLayers
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.elapsed_time, printer.elapsedTime),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.estimated_leftover_time, printer.estimatedTime),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "ETA: ${
                Clock.System.now().plus(printer.estimatedTime).format(
                    timeFormatter,
                    offset = remember {
                        TimeZone.currentSystemDefault().offsetAt(Clock.System.now())
                    },
                )
            }",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
fun PrinterDetailScreenPreview() {
    PrinterDetailScreen(
        FullPrinterEntity(
            name = "Mars 4 Ultra",
            type = PrinterType.MARS_4,
            status = PrinterStatus.Ready,
            lastSeen = Clock.System.now(),
            id = "",
            resolution = "123x123",
            firmwareVersion = "1.2.3",
            totalLayers = 10000,
            currentLayer = 1000,
            elapsedTime = 12345.seconds,
            estimatedTime = 1.5.hours,
            progress = 0.1,
        )
    )
}