package eu.kutscheid.elegoomonitor.wear.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import eu.kutscheid.elegoomonitor.shared.sync.WearPayload
import eu.kutscheid.elegoomonitor.shared.sync.WearSync
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * The watch's local cache of whatever the phone last published, plus the throttle for asking the
 * phone to scan again. Everything the complication renders comes from here, so it can be drawn
 * without any radio work.
 */
@OptIn(ExperimentalTime::class)
class SnapshotStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wear_snapshot", Context.MODE_PRIVATE)

    fun save(payload: WearPayload) {
        prefs.edit { putString(KEY_PAYLOAD, WearSync.encode(payload)) }
    }

    /** The last payload received, or null if the phone has never been heard from. */
    fun load(): WearPayload? = WearSync.decode(prefs.getString(KEY_PAYLOAD, null))

    /** Emits on every write, so the app screen updates as soon as a new snapshot lands. */
    fun payloads(): Flow<WearPayload?> = callbackFlow {
        trySend(load())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_PAYLOAD) trySend(load())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    /**
     * Rate-limits refresh requests. The complication asks on every update so the phone knows a watch
     * is still watching, but watch faces may request updates far more often than the update period.
     */
    fun shouldRequestRefresh(): Boolean {
        val last = prefs.getLong(KEY_LAST_REQUEST, 0L)
        val now = Clock.System.now().toEpochMilliseconds()
        return last == 0L || now - last !in 0..MIN_REQUEST_SPACING.inWholeMilliseconds
    }

    fun recordRefreshRequest() {
        prefs.edit { putLong(KEY_LAST_REQUEST, Clock.System.now().toEpochMilliseconds()) }
    }

    private companion object {
        const val KEY_PAYLOAD = "payload_json"
        const val KEY_LAST_REQUEST = "last_request_at"
        val MIN_REQUEST_SPACING = 2.minutes
    }
}
