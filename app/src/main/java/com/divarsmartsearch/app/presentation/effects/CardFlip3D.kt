package com.divarsmartsearch.app.presentation.effects

/*
 * 3D flip technique adapted from skydoves/compose-animations
 * (AnimationExample15.kt "3D Card Flip"), Apache License 2.0.
 * Original source: https://github.com/skydoves/compose-animations
 * Designed and developed by 2026 skydoves (Jaewoong Eum).
 *
 * The original was one fixed demo card (heart ↔ "Saved!"). Generalized
 * here into a reusable Flip3D(front, back) composable so any two pieces
 * of content in this app can be wired up as a tap-to-flip pair.
 */

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity

/**
 * Wraps [front]/[back] content in a 3D flip: tapping toggles [isFlipped],
 * rotating the content around the Y axis (with a tuned [cameraDistanceFactor]
 * for perspective) and swapping which side is drawn once the rotation
 * passes 90°, so the "back" content appears the right way round rather
 * than mirrored.
 */
@Composable
fun Flip3D(
    isFlipped: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    flipDurationMs: Int = 500,
    cameraDistanceFactor: Float = 12f,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = flipDurationMs, easing = FastOutSlowInEasing),
        label = "flip3D",
    )
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = cameraDistanceFactor * density
            }
            .clickable { onToggle() },
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}
