package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.labelResId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

/** Rounded, filled status chip shared by the list and detail screens. */
@Composable
internal fun StatusPill(status: PrinterStatus, modifier: Modifier = Modifier) {
    val transitionScope = LocalSharedTransitionScope.current
    Surface(
        modifier = modifier.then(if (transitionScope != null) Modifier.animateBounds(transitionScope) else Modifier.animateContentSize()),
        shape = RoundedCornerShape(50),
        color = animateColorAsState(colorForStatus(status)).value,
        contentColor = animateColorAsState(foregroundColorForStatus(status)).value,
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
internal fun statusLabel(status: PrinterStatus): String = stringResource(status.labelResId())

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

@Preview
@Composable
private fun PrinterStatusPreview() {
    val currentStatus by remember {
        flow {
            emit(PrinterStatus.Printing)
            delay(2.seconds)
            emit(PrinterStatus.Cancelling)
            delay(2.seconds)
            emit(PrinterStatus.Ready)
        }
    }.collectAsStateWithLifecycle(
        PrinterStatus.Printing
    )
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        StatusPill(
            status = currentStatus
        )
    }
}