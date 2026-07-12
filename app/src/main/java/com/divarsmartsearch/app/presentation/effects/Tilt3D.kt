package com.divarsmartsearch.app.presentation.effects

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable

/**
 * A subtle 3D perspective-tilt effect for cards: as the user drags a
 * finger across the card it rotates in 3D like it's a physical object
 * being tilted, then springs back flat on release.
 *
 * Kept understated (max ~9 degrees of rotation) since this is a
 * listings/data app — the goal is a tactile, premium feel, not a
 * gimmick that fights with reading the card's content.
 *
 * Usage: Modifier.tilt3D() on any Composable, e.g. ListingCard's Card(...).
 */
fun Modifier.tilt3D(
    maxRotationDegrees: Float = 9f,
): Modifier = composed {
    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val scope = rememberCoroutineScopeCompat()

    this
        .graphicsLayer {
            this.rotationX = rotationX.value
            this.rotationY = rotationY.value
            cameraDistance = 12f * density
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    val width = size.width.takeIf { it > 0 } ?: 1
                    val height = size.height.takeIf { it > 0 } ?: 1
                    val deltaY = (dragAmount.x / width) * maxRotationDegrees * 2f
                    val deltaX = (-dragAmount.y / height) * maxRotationDegrees * 2f
                    scope.launch {
                        rotationY.snapTo((rotationY.value + deltaY).coerceIn(-maxRotationDegrees, maxRotationDegrees))
                        rotationX.snapTo((rotationX.value + deltaX).coerceIn(-maxRotationDegrees, maxRotationDegrees))
                    }
                },
                onDragEnd = {
                    scope.launch {
                        rotationX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    scope.launch {
                        rotationY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                },
                onDragCancel = {
                    scope.launch { rotationX.animateTo(0f) }
                    scope.launch { rotationY.animateTo(0f) }
                },
            )
        }
}

/**
 * Small helper so this file doesn't need an extra import block juggling
 * act at the call site — just wraps rememberCoroutineScope().
 */
@androidx.compose.runtime.Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
