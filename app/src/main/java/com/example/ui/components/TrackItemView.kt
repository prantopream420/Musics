package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackEntity
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantMagenta

@Composable
fun TrackItemView(
    track: TrackEntity,
    isPlayingThis: Boolean,
    onTrackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg by animateColorAsState(
        targetValue = if (isPlayingThis) Color(0x3300F2FE) else Color(0x1F1E293B),
        label = "trackBg"
    )

    FrostedGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("track_item_${track.id}"),
        backgroundColor = containerBg,
        borderColor = if (isPlayingThis) NeonCyan.copy(alpha = 0.5f) else Color(0x2494A3B8),
        shape = RoundedCornerShape(14.dp),
        onClick = onTrackClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art / format icon tile
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (track.isLossless) {
                                listOf(Color(0xFF2A2008), Color(0xFF140F04))
                            } else {
                                listOf(Color(0xFF0F172A), Color(0xFF030712))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayingThis) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Playing",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (track.isLossless) AudiophileGold.copy(alpha = 0.8f) else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Track Title, Artist & Audiophile Badges
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    color = if (isPlayingThis) NeonCyan else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${track.artist} • ${track.album}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Dynamic Audiophile Badge (FLAC 24-bit / 96kHz or MP3 320 kbps)
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudiophileBadge(
                        badgeText = track.audiophileBadge,
                        isLossless = track.isLossless
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${track.formattedDuration}  •  ${track.formattedSize}",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("track_favorite_${track.id}")
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) VibrantMagenta else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
