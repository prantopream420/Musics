package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val contentUri: String,
    val filePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val bitrateBps: Int,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val format: String,
    val sizeBytes: Long,
    val dateAdded: Long,
    val isLossless: Boolean = false,
    val isFavorite: Boolean = false,
    val albumArtUri: String? = null
) {
    val audiophileBadge: String
        get() {
            val fmt = format.uppercase(Locale.ROOT)
            return if (isLossless || fmt == "FLAC" || fmt == "WAV") {
                val depth = if (bitDepth > 0) "${bitDepth}-bit" else "24-bit"
                val rate = when {
                    sampleRateHz >= 1000 -> "${sampleRateHz / 1000}kHz"
                    sampleRateHz > 0 -> "${sampleRateHz}Hz"
                    else -> "96kHz"
                }
                "$fmt $depth / $rate"
            } else {
                val kbps = if (bitrateBps > 0) bitrateBps / 1000 else 320
                "$fmt $kbps kbps"
            }
        }

    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1.0) {
                String.format(Locale.getDefault(), "%.1f MB", mb)
            } else {
                String.format(Locale.getDefault(), "%d KB", sizeBytes / 1024)
            }
        }
}
