package com.divarsmartsearch.app.presentation.effects

/*
 * Confetti particle system adapted from skydoves/compose-animations
 * (AnimationExample16.kt "Confetti Burst"), Apache License 2.0.
 * Original source: https://github.com/skydoves/compose-animations
 * Designed and developed by 2026 skydoves (Jaewoong Eum).
 *
 * Adapted here from a single tap-anywhere demo Canvas into a reusable,
 * app-wide "confetti host": particles live in a top-level object so ANY
 * composable (e.g. the "save listing" button) can trigger a burst at
 * its own on-screen position, while one full-screen overlay — wired
 * once, in MainActivity — does all the physics and drawing for the
 * whole app. Tuned softer/slower than the original (a tap-toy) since
 * this fires as a small reward for a real action, not as the main
 * attraction of the screen. Colors pulled from the app's own
 * navy/amber-gold theme instead of the original's generic rainbow.
 */

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val GRAVITY = 1100f
private const val AIR_DRAG = 1.4f
private const val WOBBLE_AMP = 90f
private const val WOBBLE_FREQ = 0.006f
private const val FADE_OUT_FRACTION = 0.3f

private class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    val rotSpeed: Float,
    var ageMs: Float,
    val lifetimeMs: Float,
    val color: Color,
    val w: Float,
    val h: Float,
    val wobblePhase: Float,
)

/**
 * App-wide confetti particle store. Call [ConfettiHost.burst] from
 * anywhere (e.g. a button's onClick, passing that button's on-screen
 * center) — [ConfettiOverlay] (wired once, in MainActivity) does the
 * rest.
 */
object ConfettiHost {
    internal val particles = mutableStateListOf<ConfettiParticle>()

    private const val BURST_COUNT = 46
    private const val SPEED_MIN = 500f
    private const val SPEED_MAX = 1100f
    private const val LAUNCH_ANGLE_DEG = -90f // straight up
    private const val SPREAD_DEG = 110f
    private const val LIFETIME_MS_MIN = 1100f
    private const val LIFETIME_MS_MAX = 2000f
    private const val ROT_SPEED_MAX = 720f
    private const val PARTICLE_W_PX = 18f
    private const val PARTICLE_H_PX = 34f

    // Amber-gold + neutral palette pulled from the app's own theme (see
    // presentation/theme/Color.kt) so the celebration reads "on brand"
    // rather than a generic rainbow.
    private val palette = listOf(
        Color(0xFFE3A94F), // AccentGoldDark
        Color(0xFFFFDFA8), // OnAccentGoldContainerDark
        Color(0xFF8B99A6), // NeutralAccentDark
        Color(0xFFECEEF0), // OnSurfaceDark
    )

    fun burst(at: Offset, count: Int = BURST_COUNT) {
        val baseRad = Math.toRadians(LAUNCH_ANGLE_DEG.toDouble()).toFloat()
        val spreadRad = Math.toRadians(SPREAD_DEG.toDouble() / 2.0).toFloat()
        repeat(count) {
            val angle = baseRad + (Random.nextFloat() * 2f - 1f) * spreadRad
            val speed = SPEED_MIN + Random.nextFloat() * (SPEED_MAX - SPEED_MIN)
            val sizeJitter = 0.7f + Random.nextFloat() * 0.6f
            particles.add(
                ConfettiParticle(
                    x = at.x,
                    y = at.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() * 2f - 1f) * ROT_SPEED_MAX,
                    ageMs = 0f,
                    lifetimeMs = LIFETIME_MS_MIN + Random.nextFloat() * (LIFETIME_MS_MAX - LIFETIME_MS_MIN),
                    color = palette[Random.nextInt(palette.size)],
                    w = PARTICLE_W_PX * sizeJitter,
                    h = PARTICLE_H_PX * sizeJitter,
                    wobblePhase = Random.nextFloat() * (2f * Math.PI.toFloat()),
                ),
            )
        }
    }
}

/**
 * Full-screen overlay that animates and draws whatever is currently in
 * [ConfettiHost]. Wire this ONCE near the root of the app (see
 * MainActivity's AppBackgroundEffects) — do not add it per-screen.
 */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    var frameNanos by remember { mutableLongStateOf(0L) }
    val particles = ConfettiHost.particles

    LaunchedEffect(Unit) {
        var lastFrame = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dtMs = min((now - lastFrame) / 1_000_000f, 50f)
            val dtSec = dtMs / 1000f
            lastFrame = now

            val damping = exp(-AIR_DRAG * dtSec)
            var i = 0
            while (i < particles.size) {
                val p = particles[i]
                p.ageMs += dtMs
                if (p.ageMs >= p.lifetimeMs) {
                    particles.removeAt(i)
                    continue
                }
                p.vy += GRAVITY * dtSec
                p.vx *= damping
                p.vy *= damping
                val wobble = sin(p.ageMs * WOBBLE_FREQ + p.wobblePhase) * WOBBLE_AMP
                p.x += (p.vx + wobble) * dtSec
                p.y += p.vy * dtSec
                p.rotation += p.rotSpeed * dtSec
                i++
            }
            frameNanos = now
        }
    }

    Canvas(modifier = modifier) {
        // Read frameNanos so this draw scope recomposes/redraws every frame.
        @Suppress("UNUSED_EXPRESSION")
        frameNanos

        for (p in particles) {
            val lifeFraction = (1f - p.ageMs / p.lifetimeMs).coerceIn(0f, 1f)
            val alpha = if (lifeFraction < FADE_OUT_FRACTION) lifeFraction / FADE_OUT_FRACTION else 1f
            rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(p.x - p.w / 2f, p.y - p.h / 2f),
                    size = Size(p.w, p.h),
                )
            }
        }
    }
}
