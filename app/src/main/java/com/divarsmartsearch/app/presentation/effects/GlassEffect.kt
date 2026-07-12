package com.divarsmartsearch.app.presentation.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Frosted-glass ("glassmorphism") surface treatment: a translucent
 * gradient tint plus a subtle top-highlight border, giving cards a
 * "frosted panel" read.
 *
 * Note: this deliberately does NOT use Modifier.blur() on the card
 * itself. Compose's blur affects everything drawn inside that
 * modifier's node — including the card's own text and icons — so
 * applying it here would blur the listing title/price along with the
 * background, hurting readability. A true "blur only what's behind
 * this card" backdrop effect needs a separate blurred layer sitting
 * behind the content (not available as a simple modifier in this
 * Compose/AGP setup), so this sticks to the tint + border look, which
 * reads as glass without touching content sharpness.
 *
 * Usage:
 *   Card(modifier = Modifier.glassEffect(), colors = CardDefaults.cardColors(
 *       containerColor = Color.Transparent, // let glassEffect supply the fill
 *   )) { ... }
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Color? = null,
): Modifier = composed {
    val accent = MaterialTheme.colorScheme.surfaceVariant
    val effectiveTint = tint ?: accent

    this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    effectiveTint.copy(alpha = 0.55f),
                    effectiveTint.copy(alpha = 0.82f),
                ),
            ),
            shape = shape,
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.04f),
                ),
            ),
            shape = shape,
        )
}
