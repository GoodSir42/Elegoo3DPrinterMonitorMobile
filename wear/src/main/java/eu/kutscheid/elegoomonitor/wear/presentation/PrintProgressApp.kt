package eu.kutscheid.elegoomonitor.wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.labelResId
import eu.kutscheid.elegoomonitor.shared.sync.PrintSnapshot
import eu.kutscheid.elegoomonitor.shared.sync.WearPayload
import eu.kutscheid.elegoomonitor.wear.R
import eu.kutscheid.elegoomonitor.wear.sync.PhoneRefreshRequester
import eu.kutscheid.elegoomonitor.wear.sync.SnapshotStore
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PrintProgressApp(store: SnapshotStore) {
    val payloads = remember(store) { store.payloads() }
    val payload by payloads.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }

    PrintProgressScreen(
        payload = payload,
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            scope.launch {
                PhoneRefreshRequester.request(context)
                refreshing = false
            }
        },
    )
}

@Composable
private fun PrintProgressScreen(
    payload: WearPayload?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
) {
    MaterialTheme {
        AppScaffold {
            ScreenScaffold {
                val snapshot = payload?.snapshot
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    if (snapshot != null) {
                        CircularProgressIndicator(
                            progress = { snapshot.progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(CircularProgressIndicatorDefaults.FullScreenPadding),
                            startAngle = START_ANGLE,
                            endAngle = END_ANGLE,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        when {
                            payload == null -> Message(stringResource(R.string.wear_waiting_for_phone))
                            snapshot == null -> Message(stringResource(R.string.wear_no_active_prints))
                            else -> ActivePrint(snapshot)
                        }
                        RefreshButton(enabled = !refreshing, onRefresh = onRefresh)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivePrint(snapshot: PrintSnapshot) {
    val percent = (snapshot.progress.coerceIn(0f, 1f) * 100).roundToInt()
    Text(
        text = snapshot.printerName,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.complication_percent, percent),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(snapshot.status.labelResId()),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    if (snapshot.totalLayers > 0) {
        Text(
            text = stringResource(
                R.string.wear_layer,
                snapshot.currentLayer,
                snapshot.totalLayers,
            ),
            style = MaterialTheme.typography.bodyExtraSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun RefreshButton(enabled: Boolean, onRefresh: () -> Unit) {
    IconButton(
        onClick = onRefresh,
        enabled = enabled,
        modifier = Modifier
            .padding(top = 6.dp)
            .size(IconButtonDefaults.SmallButtonSize),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = stringResource(R.string.wear_refresh),
            modifier = Modifier.size(IconButtonDefaults.SmallIconSize),
        )
    }
}

/** Leaves a gap at the bottom of the ring so the progress arc does not close on itself. */
private const val START_ANGLE = 120f
private const val END_ANGLE = 60f

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun ActivePrintPreview() {
    PrintProgressScreen(
        payload = WearPayload(
            snapshot = PrintSnapshot(
                printerName = "Saturn 3 Ultra",
                progress = 0.65f,
                status = PrinterStatus.Printing,
                currentLayer = 1300,
                totalLayers = 2000,
            ),
        ),
        refreshing = false,
        onRefresh = {},
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun NoActivePrintPreview() {
    PrintProgressScreen(payload = WearPayload(), refreshing = false, onRefresh = {})
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun WaitingForPhonePreview() {
    PrintProgressScreen(payload = null, refreshing = false, onRefresh = {})
}
