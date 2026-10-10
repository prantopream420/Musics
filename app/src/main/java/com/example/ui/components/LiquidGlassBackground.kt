package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DynamicPaletteColors
import kotlin.math.abs
import kotlin.math.sin

/**
 * Liquid Glass UI:
 * Renders a deeply blurred, full-screen background surface with fluid gradients
 * extracted dynamically from the current track's album art / palette, plus a
 * blurry colorful equalizer animation running under the whole UI elements layer.
 */
@Composable
fun LiquidGlassBackground(
    palette: DynamicPaletteColors,
    isDarkMode: Boolean = true,
    isPlaying: Boolean = false,
    positionMs: Long = 0L,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val targetBg = if (isDarkMode) palette.background else Color(0xFFF8FAFC)
    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    val animatedPrimary by animateColorAsState(
        targetValue = palette.primary,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "primaryColor"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = palette.secondary,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "secondaryColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "liquidMotion")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquidWave"
    )

    val beatPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 500 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beatPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (!isDarkMode) {
                // Gradient off-white background in light mode
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFEDF2F7),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
            }

            // Deep blurred ambient mesh orbs & Equalizer beats layer sitting under UI
            val orb1Center = Offset(
                x = width * (0.25f + 0.15f * animOffset),
                y = height * (0.2f + 0.1f * animOffset)
            )
            val orb2Center = Offset(
                x = width * (0.8f - 0.2f * animOffset),
                y = height * (0.6f + 0.15f * animOffset)
            )
            val orb3Center = Offset(
                x = width * 0.5f,
                y = height * (0.85f - 0.1f * animOffset)
            )

            val alphaMultiplier = if (isDarkMode) 1f else 0.5f

            // Orb 1: Primary vibrant glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedPrimary.copy(alpha = 0.35f * alphaMultiplier * beatPulse),
                        animatedPrimary.copy(alpha = 0.12f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = width * 0.85f
                ),
                center = orb1Center,
                radius = width * 0.85f
            )

            // Orb 2: Secondary accent glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedSecondary.copy(alpha = 0.28f * alphaMultiplier * (1.2f - beatPulse)),
                        animatedSecondary.copy(alpha = 0.08f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = width * 0.95f
                ),
                center = orb2Center,
                radius = width * 0.95f
            )

            // Blurry Colorful Equalizer / Beat Waves under UI elements layer
            val barCount = 20
            val barWidth = width / barCount
            for (i in 0 until barCount) {
                val freqFactor = sin((i + animOffset * 12f + positionMs / 200.0).toDouble()).toFloat()
                val heightMultiplier = if (isPlaying) (0.35f + 0.65f * abs(freqFactor) * beatPulse) else 0.12f
                val barHeight = height * 0.5f * heightMultiplier
                val x = i * barWidth + barWidth / 2f
                val y = height - barHeight

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            animatedPrimary.copy(alpha = if (isDarkMode) 0.35f else 0.22f),
                            animatedSecondary.copy(alpha = if (isDarkMode) 0.55f else 0.35f)
                        )
                    ),
                    topLeft = Offset(x - barWidth * 0.4f, y),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barHeight)
                )
            }

            if (isDarkMode) {
                // Overall obsidian dark vignette overlay for glass clarity in dark mode
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x990A0E17),
                            Color(0x660A0E17),
                            Color(0xCC070B12)
                        )
                    )
                )
            }
        }

        content()
    }
}
