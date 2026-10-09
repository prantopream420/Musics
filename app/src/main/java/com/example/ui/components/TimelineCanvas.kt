package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.NeonCyan


import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

/**
 * 60fps Timeline Playback Canvas (PDF Requirement):
 * Polled at 16ms intervals via coroutine loop tied to ExoPlayer, advancing
 * with continuous buttery-smooth precision instead of 1-second ticking.
 */
@Composable
fun TimelineCanvas(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    trackSeed: Long = 42L
) {
    val totalDuration = durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val actualFraction = (positionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    val displayFraction = if (isDragging) dragFraction else actualFraction

    val scrubberScale by animateFloatAsState(
        targetValue = if (isDragging) 1.4f else 1.0f,
        label = "scrubberScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_canvas_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("timeline_canvas")
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (newFraction * totalDuration).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(totalDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val targetMs = (dragFraction * totalDuration).toLong()
                            onSeek(targetMs)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            val progressX = (displayFraction * width).coerceIn(0f, width)

            // Draw audiophile waveform background tick bars (36 ticks)
            val tickCount = 42
            val tickGap = width / tickCount
            val barWidth = 2.5.dp.toPx()

            for (i in 0 until tickCount) {
                val tickX = i * tickGap + tickGap / 2f
                // Procedural rhythmic harmonic waveform amplitude (bouncy)
                val seedOffset = (trackSeed % 1000).toDouble() / 100.0
                val beatPhase = displayTimeMs.toDouble() / 250.0 // Fast rhythmic pulse
                val wavePhase = i * 0.28 + seedOffset + beatPhase * 0.5
                val beatBounce = abs(kotlin.math.sin(beatPhase - i * 0.1))
                val normalizedAmp = (0.15f + 0.85f * abs(kotlin.math.sin(wavePhase) * kotlin.math.sin(wavePhase * 0.5)) * beatBounce).toFloat()
                val barHeight = (height * 0.65f * normalizedAmp).coerceAtLeast(4.dp.toPx())
                val topY = centerY - barHeight / 2f

                val isPassed = tickX <= progressX
                val barColor = if (isPassed) {
                    accentColor.copy(alpha = if (isDragging) 0.9f else 0.75f)
                } else {
                    Color(0x3364748B)
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(tickX - barWidth / 2f, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // Continuous background guide track
            val trackHeight = 3.dp.toPx()
            drawRoundRect(
                color = Color(0x33475569),
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = Size(width, trackHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )

            // Continuous active progress line with vibrant gradient
            if (progressX > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            accentColor
                        ),
                        startX = 0f,
                        endX = progressX
                    ),
                    topLeft = Offset(0f, centerY - (trackHeight + 1.dp.toPx()) / 2f),
                    size = Size(progressX, trackHeight + 1.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // Glowing Scrubber Thumb Head (60fps continuous position)
            val baseHeadRadius = 7.dp.toPx() * scrubberScale
            val glowRadius = baseHeadRadius * 2.2f

            // Outer soft glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(progressX, centerY),
                    radius = glowRadius
                ),
                center = Offset(progressX, centerY),
                radius = glowRadius
            )

            // Inner solid head
            drawCircle(
                color = Color.White,
                center = Offset(progressX, centerY),
                radius = baseHeadRadius
            )

            // Core accent ring
            drawCircle(
                color = accentColor,
                center = Offset(progressX, centerY),
                radius = baseHeadRadius * 0.55f
            )
        }

        // Live Timestamps Row
        val displayTimeMs = if (isDragging) (dragFraction * totalDuration).toLong() else positionMs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(displayTimeMs),
                color = if (isDragging) accentColor else AppTheme.colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = formatDuration(totalDuration),
                color = AppTheme.colors.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
