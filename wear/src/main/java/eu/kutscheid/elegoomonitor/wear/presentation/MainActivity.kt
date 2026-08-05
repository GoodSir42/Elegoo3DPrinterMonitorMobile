package eu.kutscheid.elegoomonitor.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import eu.kutscheid.elegoomonitor.wear.sync.SnapshotStore

/**
 * The companion screen. It exists so the complication has somewhere to tap through to, and so the
 * user can see why the complication is empty (no print running, or the phone not reachable).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SnapshotStore(applicationContext)
        setContent {
            PrintProgressApp(store = store)
        }
    }
}
