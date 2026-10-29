package eu.kutscheid.elegoomonitor.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Since Android 17, apps targeting API 37 need the runtime `ACCESS_LOCAL_NETWORK` permission for
 * any traffic to the local network — which is everything this app does (UDP discovery broadcast,
 * printer websockets, the video stream). Below API 37 it is implicitly granted.
 */
@Stable
class LocalNetworkPermissionState internal constructor(
    granted: Boolean,
    private val onRequest: () -> Unit,
) {
    var isGranted by mutableStateOf(granted)
        internal set

    /** Shows the system dialog, or the app settings once the user has denied it for good. */
    fun request() = onRequest()
}

@Composable
fun rememberLocalNetworkPermissionState(): LocalNetworkPermissionState {
    val context = LocalContext.current
    // The dialog can only be shown again after a first denial if the system still offers a
    // rationale; afterwards the only way to grant it is from the app settings.
    var deniedBefore by rememberSaveable { mutableStateOf(false) }
    lateinit var state: LocalNetworkPermissionState
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.isGranted = granted
        if (!granted) deniedBefore = true
    }
    state = remember {
        LocalNetworkPermissionState(context.hasLocalNetworkPermission()) {
            val activity = context.findActivity()
            if (deniedBefore && activity?.shouldShowRequestPermissionRationale(LOCAL_NETWORK) == false) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    )
                )
            } else {
                launcher.launch(LOCAL_NETWORK)
            }
        }
    }
    // Picks up a grant made in the system settings while the app was in the background.
    LifecycleResumeEffect(state) {
        state.isGranted = context.hasLocalNetworkPermission()
        onPauseOrDispose {}
    }
    LaunchedEffect(state) {
        if (!state.isGranted && !deniedBefore) state.request()
    }
    return state
}

fun Context.hasLocalNetworkPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
            ContextCompat.checkSelfPermission(
                this,
                LOCAL_NETWORK
            ) == PackageManager.PERMISSION_GRANTED

// Only checked or requested on API 37+, see hasLocalNetworkPermission().
@SuppressLint("InlinedApi")
private const val LOCAL_NETWORK = Manifest.permission.ACCESS_LOCAL_NETWORK

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
