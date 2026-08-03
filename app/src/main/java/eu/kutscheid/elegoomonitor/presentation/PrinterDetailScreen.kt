package eu.kutscheid.elegoomonitor.presentation

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.FullPrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.PrinterType
import eu.kutscheid.elegoomonitor.ui.theme.ElegooMonitorTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val timeFormatter = DateTimeComponents.Format {
    hour()
    char(':')
    minute()
}

/** Width at which we switch from a single stacked column to a balanced two-pane layout. */
private val WideBreakpoint = 640.dp

/** Keeps content readable instead of stretching edge-to-edge on tablets and unfolded foldables. */
private val MaxContentWidth = 900.dp

@OptIn(ExperimentalTime::class)
@Composable
fun PrinterDetailScreen(
    printer: FullPrinterEntity,
    modifier: Modifier = Modifier,
    videoStreamUrl: String? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        val wide = maxWidth >= WideBreakpoint

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = printer.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        HeaderCard(printer)
                        videoStreamUrl?.let { StreamCard(url = it) }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        ProgressCard(printer)
                        StatsCard(printer)
                    }
                }
            } else {
                videoStreamUrl?.let { StreamCard(url = it) }
                HeaderCard(printer)
                ProgressCard(printer)
                StatsCard(printer)
            }
        }
    }
}

@Composable
private fun HeaderCard(printer: FullPrinterEntity, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
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
                    modifier = Modifier.size(120.dp)
                )
                StatusPill(printer.status)
            }
            HorizontalDivider()
            InfoRow(
                label = stringResource(R.string.detail_label_model),
                value = printer.type.displayName,
            )
            InfoRow(
                label = stringResource(R.string.detail_label_resolution),
                value = printer.resolution.ifBlank { "—" },
            )
            InfoRow(
                label = stringResource(R.string.detail_label_firmware),
                value = printer.firmwareVersion,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressCard(printer: FullPrinterEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val progress by animateFloatAsState(
                targetValue = printer.progress.toFloat(),
                label = "printProgress",
            )
            Box(contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(220.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(
                            R.string.printer_progress_percent_short,
                            progress * 100
                        ),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.detail_label_complete),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.current_layer,
                    printer.currentLayer,
                    printer.totalLayers
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun StatsCard(printer: FullPrinterEntity, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatRow(
                label = stringResource(R.string.elapsed_time_label),
                value = formatDuration(printer.elapsedTime),
            )
            HorizontalDivider()
            StatRow(
                label = stringResource(R.string.estimated_leftover_time_label),
                value = formatDuration(printer.estimatedTime),
            )
            HorizontalDivider()
            val eta = remember(printer.estimatedTime) {
                Clock.System.now().plus(printer.estimatedTime).format(
                    timeFormatter,
                    offset = TimeZone.currentSystemDefault().offsetAt(Clock.System.now()),
                )
            }
            StatRow(
                label = stringResource(R.string.eta_label),
                value = eta,
                emphasize = true,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (emphasize) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.titleMedium
            },
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun formatDuration(duration: Duration): String =
    duration.toComponents { hours, minutes, seconds, _ -> "${hours}h ${minutes}m ${seconds}s" }

/**
 * Renders a Motion-JPEG stream. Media3/ExoPlayer can't decode MJPEG, so the frames are shown via a
 * WebView `<img>` which the browser engine refreshes in place. Reloaded only when [url] changes so
 * frequent status recompositions don't restart the stream.
 */
@Composable
private fun StreamCard(url: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        key(url) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        loadUrl(url)
                    }
                },
                onRelease = { it.destroy() },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
private fun PrinterDetailScreenPreview() {
    ElegooMonitorTheme {
        PrinterDetailScreen(
            FullPrinterEntity(
                name = "Mars 4 Ultra",
                type = PrinterType.MARS_4,
                status = PrinterStatus.Printing,
                mainboardID = "MB1",
                ipAddress = "192.168.1.42",
                lastSeen = Clock.System.now(),
                id = "",
                resolution = "4098 x 2560",
                firmwareVersion = "1.2.3",
                totalLayers = 10000,
                currentLayer = 6234,
                elapsedTime = 12345.seconds,
                estimatedTime = 1.5.hours,
                progress = 0.62,
            )
        )
    }
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
private fun PrinterDetailScreenWidePreview() {
    ElegooMonitorTheme {
        PrinterDetailScreen(
            printer = FullPrinterEntity(
                name = "Saturn 3 Ultra",
                type = PrinterType.SATURN_3,
                status = PrinterStatus.Printing,
                mainboardID = "MB1",
                ipAddress = "192.168.1.42",
                lastSeen = Clock.System.now(),
                id = "",
                resolution = "11520 x 5120",
                firmwareVersion = "2.0.1",
                totalLayers = 8000,
                currentLayer = 2100,
                elapsedTime = 8000.seconds,
                estimatedTime = 3.2.hours,
                progress = 0.26,
            )
        )
    }
}