package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FrostedGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x331E293B),
    borderColor: Color = Color(0x4094A3B8),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 1.25f).coerceAtMost(0.9f)),
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.85f).coerceAtMost(0.9f))
                    )
                ),
                shape = shape
            )
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.linearGradient(
                        colors = listOf(
                            borderColor.copy(alpha = 0.5f),
                            borderColor.copy(alpha = 0.15f)
                        )
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun FrostedGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x2B1E293B),
    borderColor: Color = Color(0x3394A3B8),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            content()
        }
    }
}
