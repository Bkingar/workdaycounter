package com.bk.workdaycounter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Starts on a random number and rolls to [target] in exactly [durationMs] (default 300 ms).
 *
 * The roll only replays when [trigger] changes (i.e. when the app is opened). If [target]
 * changes for another reason - the user editing the leaves field, say - the number snaps
 * straight to the new value instead of re-rolling on every keystroke.
 */
@Composable
fun RollingNumber(
    target: Int,
    trigger: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    durationMs: Int = 300
) {
    var display by remember { mutableIntStateOf(target) }
    var animating by remember { mutableStateOf(false) }
    val latestTarget by rememberUpdatedState(target)

    // Declared first so it claims `animating` before the snap effect below runs.
    LaunchedEffect(trigger) {
        animating = true
        try {
            val landing = latestTarget
            val spread = max(40, landing * 2)
            val from = Random.nextInt(0, spread + 1)
            display = from

            var startNanos = 0L
            while (true) {
                val frame = withFrameNanos { it }
                if (startNanos == 0L) startNanos = frame
                val elapsedMs = (frame - startNanos) / 1_000_000f
                val p = (elapsedMs / durationMs).coerceIn(0f, 1f)
                // ease-out cubic: fast start, soft landing
                val eased = 1f - (1f - p) * (1f - p) * (1f - p)
                display = (from + (latestTarget - from) * eased).roundToInt()
                if (p >= 1f) break
            }
            display = latestTarget
        } finally {
            // Runs even if the effect is cancelled mid-roll (tab switch, config change).
            animating = false
            display = latestTarget
        }
    }

    LaunchedEffect(target) {
        if (!animating) display = target
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = display.toString(), style = style, textAlign = TextAlign.Center)
    }
}
