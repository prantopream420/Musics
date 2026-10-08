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

/**
 * Liquid Glass UI:
 * Renders a deeply blurred, full-screen background surface with fluid gradients
 * extracted dynamically from the current track's album art / palette.
 */
@Composable
fun LiquidGlassBackground(
    palette: DynamicPaletteColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = palette.background,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Deep blurred ambient mesh orbs
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

            // Orb 1: Primary vibrant glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedPrimary.copy(alpha = 0.35f),
                        animatedPrimary.copy(alpha = 0.12f),
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
                        animatedSecondary.copy(alpha = 0.28f),
                        animatedSecondary.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = width * 0.95f
                ),
                center = orb2Center,
                radius = width * 0.95f
            )

            // Orb 3: Subtle bottom atmospheric glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedPrimary.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = orb3Center,
                    radius = width * 0.7f
                ),
                center = orb3Center,
                radius = width * 0.7f
            )

            // Overall obsidian dark vignette overlay for glass clarity
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

        content()
    }
}
