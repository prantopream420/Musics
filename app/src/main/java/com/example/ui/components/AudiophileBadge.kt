package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.NeonCyan

@Composable
fun AudiophileBadge(
    badgeText: String,
    isLossless: Boolean,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val shape = RoundedCornerShape(if (isExpanded) 10.dp else 6.dp)

    val borderColor = if (isLossless) {
        AudiophileGold.copy(alpha = 0.65f)
    } else {
        NeonCyan.copy(alpha = 0.45f)
    }

    val backgroundColor = if (isLossless) {
        Color(0x2EFFD166)
    } else {
        Color(0x2600F2FE)
    }

    val textColor = if (isLossless) {
        AudiophileGold
    } else {
        NeonCyan
    }

    Box(
        modifier = modifier
            .testTag("audiophile_badge")
            .clip(shape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    width = (if (isExpanded) 1.2.dp else 0.8.dp),
                    brush = Brush.horizontalGradient(
                        listOf(
                            borderColor,
                            borderColor.copy(alpha = 0.25f)
                        )
                    )
                ),
                shape = shape
            )
            .padding(
                horizontal = if (isExpanded) 10.dp else 6.dp,
                vertical = if (isExpanded) 4.dp else 2.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLossless && isExpanded) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AudiophileGold,
                    modifier = Modifier
                        .size(12.dp)
                        .padding(end = 3.dp)
                )
            } else if (!isLossless && isExpanded) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier
                        .size(12.dp)
                        .padding(end = 3.dp)
                )
            }

            Text(
                text = badgeText,
                color = textColor,
                fontSize = if (isExpanded) 11.sp else 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp
            )
        }
    }
}
