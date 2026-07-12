package com.divarsmartsearch.app.presentation.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
 * Shimmer ("skeleton loading") effect: a soft diagonal highlight sweeps
 * across the element in a loop. Meant for placeholder cards/rows shown
 * while listings/results are loading — e.g. Results screen while a scan
 * is running, History while data loads, Seller Report while fetching.
 *
 * Usage:
 *   Box(modifier = Modifier
 *       .fillMaxWidth()
 *       .height(96.dp)
 *       .shimmerLoading()
 *   )
 */
fun Modifier.shimmerLoading(): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translateAnim * 400f, 0f),
        end = Offset(translateAnim * 400f + 400f, 400f),
    )

    this.background(brush)
}
