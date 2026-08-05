@file:OptIn(ExperimentalSharedTransitionApi::class)

package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * The [SharedTransitionScope] that hosts the list↔detail transition, provided by the
 * `SharedTransitionLayout` around the `NavDisplay`. Null outside that layout (e.g. in `@Preview`s),
 * in which case the shared-element modifiers below are no-ops so the screens stay previewable.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Shared-element keys, keyed by printer id so the list card and the detail screen match up. Kept in
 * one place so both sides can't drift apart.
 */
internal fun printerDetails(id: String) = "printer-details-$id"

internal fun printerImageKey(id: String) = "printer-image-$id"

internal fun printerResolutionKey(id: String) = "printer-resolution-$id"

internal fun printerNameKey(id: String) = "printer-name-$id"

internal fun printerStatusKey(id: String) = "printer-status-$id"

internal fun topBar() = "top-bar"

/** Marks a composable as a shared element for the list↔detail transition. */
@Composable
internal fun Modifier.printerSharedElement(key: Any): Modifier {
    val scope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedContentScope.current
    return with(scope) {
        this@printerSharedElement.sharedElement(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
        )
    }
}

/**
 * Like [printerSharedElement] but for content that changes size or style between screens (text, the
 * status pill); [SharedTransitionScope.sharedBounds] cross-fades and resizes rather than assuming
 * identical content.
 */
@Composable
internal fun Modifier.sharedBounds(key: Any): Modifier {
    val scope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedContentScope.current
    return with(scope) {
        this@sharedBounds.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
        )
    }
}
