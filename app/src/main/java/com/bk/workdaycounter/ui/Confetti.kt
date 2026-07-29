package com.bk.workdaycounter.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val spin: Float,
    val phase: Float,
    val delay: Float
)

private val PALETTE = listOf(
    Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE),
    Color(0xFF40C4FF), Color(0xFFE040FB), Color(0xFFFF6E40)
)

/**
 * Celebration burst. [trigger] restarts the animation whenever its value changes,
 * so the pop plays every time the app is opened.
 */
@Composable
fun ConfettiOverlay(trigger: Int, modifier: Modifier = Modifier, particleCount: Int = 110) {
    val particles = remember(trigger) {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                vx = Random.nextFloat() * 0.5f - 0.25f,
                vy = 0.55f + Random.nextFloat() * 0.85f,
                color = PALETTE[Random.nextInt(PALETTE.size)],
                size = 10f + Random.nextFloat() * 14f,
                spin = Random.nextFloat() * 900f - 450f,
                phase = Random.nextFloat() * 6.28f,
                delay = Random.nextFloat() * 0.25f
            )
        }
    }

    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 2600, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        if (t <= 0f || t >= 1f) return@Canvas
        particles.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach

            val drift = sin(local * 7f + p.phase) * 0.05f
            val cx = (p.x + p.vx * local + drift) * size.width
            // start slightly above the top, fall past the bottom
            val cy = (-0.15f + p.vy * local * 1.5f) * size.height
            val alpha = (1f - local).coerceIn(0f, 1f)

            rotate(degrees = p.spin * local, pivot = Offset(cx, cy)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(cx - p.size / 2f, cy - p.size / 2f),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
