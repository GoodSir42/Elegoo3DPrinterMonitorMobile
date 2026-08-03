package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus

/** Rounded, filled status chip shared by the list and detail screens. */
@Composable
internal fun StatusPill(status: PrinterStatus, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colorForStatus(status),
        contentColor = foregroundColorForStatus(status),
    ) {
        Text(
            text = statusLabel(status),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
internal fun statusLabel(status: PrinterStatus): String = when (status) {
    PrinterStatus.Ready -> stringResource(R.string.printer_status_ready)
    PrinterStatus.Printing -> stringResource(R.string.printer_status_printing)
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
}

internal fun colorForStatus(status: PrinterStatus): Color {
    return when (status) {
        PrinterStatus.Ready, PrinterStatus.Paused -> Color(0xFF999999)
        PrinterStatus.Pausing, PrinterStatus.Cancelling -> Color(0xFFc2b85c)
        PrinterStatus.Cancelled -> Color(0xFFc25c5c)
        PrinterStatus.Complete -> Color(0xFF64c25c)
        // Default blue color
        else -> Color(0xFF0077cc)
    }
}

internal fun foregroundColorForStatus(status: PrinterStatus): Color {
    return when (status) {
        PrinterStatus.Pausing, PrinterStatus.Cancelling -> Color.Black
        else -> Color.White
    }
}