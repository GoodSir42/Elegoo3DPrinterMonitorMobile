package eu.kutscheid.elegoomonitor.data.wear

import android.content.Context
import androidx.core.content.edit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Remembers when a watch last asked for data.
 *
 * The refresh chain used to run only while a home-screen widget existed. A watch complication is
 * just as good a reason to keep scanning, but the phone cannot see which complications a watch face
 * shows. The watch therefore asks for a refresh whenever its complication updates, and this sliding
 * window keeps the chain alive while those requests keep arriving — and lets it stop once the
 * complication is removed.
 */
@OptIn(ExperimentalTime::class)
class WearInterest(context: Context) {

    private val prefs =
        context.getSharedPreferences("wear_sync", Context.MODE_PRIVATE)

    fun recordRequest() {
        prefs.edit { putLong(KEY_LAST_REQUEST, Clock.System.now().toEpochMilliseconds()) }
    }

    /** True while requests from a watch are recent enough to justify continuing to scan. */
    fun isWatchInterested(): Boolean {
        val last = prefs.getLong(KEY_LAST_REQUEST, 0L)
        if (last == 0L) return false
        val age = Clock.System.now().toEpochMilliseconds() - last
        return age in 0..WINDOW.inWholeMilliseconds
    }

    private companion object {
        const val KEY_LAST_REQUEST = "last_request_at"

        /** Comfortably longer than the complication's update period, so one missed update is fine. */
        val WINDOW = 1.hours
    }
}
