package com.divarsmartsearch.app.presentation.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * A subtle, ambient particle field (soft amber-gold dust drifting slowly
 * upward) meant to sit behind ALL screen content, app-wide.
 *
 * Usage — wrap the app's root content once, in MainActivity:
 *
 *   DivarSmartSearchTheme(darkTheme = darkModeEnabled) {
 *       AppBackgroundEffects {
 *           Surface(modifier = Modifier.fillMaxSize()) {
 *               DivarNavGraph()
 *           }
 *       }
 *   }
 *
 * It is intentionally slow, low-contrast, and low particle-count (18) so
 * it reads as ambient texture rather than a distraction while reading
 * listing text — this is a data-dense, real-estate-scanning app, not a
 * game.
 */
private data class Particle(
    val startX: Float,      // 0f..1f, fraction of width
    val baseY: Float,       // 0f..1f, fraction of height
    val radius: Float,      // px
    val speed: Float,       // cycles per animation loop
    val phase: Float,       // 0f..1f offset so particles don't move in sync
    val driftAmplitude: Float, // px, horizontal sway
    val alpha: Float,
)

private fun generateParticles(count: Int, seed: Long): List<Particle> {
    val random = Random(seed)
    return List(count) {
        Particle(
            startX = random.nextFloat(),
            baseY = random.nextFloat(),
            radius = random.nextFloat() * 3.5f + 1.5f,
            speed = random.nextFloat() * 0.6f + 0.4f,
            phase = random.nextFloat(),
            driftAmplitude = random.nextFloat() * 30f + 10f,
            alpha = random.nextFloat() * 0.35f + 0.12f,
        )
    }
}

@Composable
fun AppBackgroundEffects(
    modifier: Modifier = Modifier,
    particleCount: Int = 18,
    content: @Composable () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val particles = remember { generateParticles(particleCount, seed = 42L) }

    val infiniteTransition = rememberInfiniteTransition(label = "particleField")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "particleProgress",
    )

    // Particles are drawn as an overlay ON TOP of the screen content (not
    // behind it) — the app's own Surface underneath is opaque, so an
    // effect drawn "behind" content would never be visible. Kept at very
    // low alpha (see Particle.alpha above), and the Canvas has no pointer
    // input attached, so it never blocks touches or readability.
    Box(modifier = modifier.fillMaxSize()) {
        content()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawParticleField(particles, progress, accent, background)
        }
        // App-wide confetti celebration layer — any composable can call
        // ConfettiHost.burst(offset) to trigger a burst here (see
        // ListingCard's save button for the wiring).
        ConfettiOverlay(modifier = Modifier.fillMaxSize())
    }
}

private fun DrawScope.drawParticleField(
    particles: List<Particle>,
    progress: Float,
    accentColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color,
) {
    val w = size.width
    val h = size.height

    for (particle in particles) {
        // Each particle loops from bottom to top over the animation cycle,
        // offset by its own phase so the field feels organic, not mechanical.
        val loopedT = (progress + particle.phase) % 1f
        val y = h * (1f - loopedT) // drifts upward
        val sway = sin((loopedT + particle.phase) * 2 * PI.toFloat() * particle.speed) * particle.driftAmplitude
        val x = (particle.startX * w) + sway

        // Fade in/out at the top and bottom of the loop so particles don't
        // pop in/out abruptly.
        val edgeFade = when {
            loopedT < 0.08f -> loopedT / 0.08f
            loopedT > 0.92f -> (1f - loopedT) / 0.08f
            else -> 1f
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = particle.alpha * edgeFade),
                    accentColor.copy(alpha = 0f),
                ),
                center = Offset(x, y),
                radius = particle.radius * 4f,
            ),
            radius = particle.radius * 4f,
            center = Offset(x, y),
        )
    }
}
