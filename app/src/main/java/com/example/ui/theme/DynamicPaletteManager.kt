package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.example.data.local.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.abs

data class DynamicPaletteColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val accent: Color
) {
    companion object {
        val Default = DynamicPaletteColors(
            primary = Color(0xFF00F2FE),
            secondary = Color(0xFF4FACFE),
            background = Color(0xFF0A0F1D),
            accent = Color(0xFFF72585)
        )
    }
}

object DynamicPaletteManager {

    private val presetPalettes = listOf(
        DynamicPaletteColors(
            primary = Color(0xFF00F2FE),
            secondary = Color(0xFF1E3A8A),
            background = Color(0xFF070B14),
            accent = Color(0xFF38BDF8)
        ),
        DynamicPaletteColors(
            primary = Color(0xFFF72585),
            secondary = Color(0xFF7209B7),
            background = Color(0xFF10081C),
            accent = Color(0xFF4CC9F0)
        ),
        DynamicPaletteColors(
            primary = Color(0xFF06D6A0),
            secondary = Color(0xFF0B4F3E),
            background = Color(0xFF051611),
            accent = Color(0xFFFFD166)
        ),
        DynamicPaletteColors(
            primary = Color(0xFFFFD166),
            secondary = Color(0xFF8B4513),
            background = Color(0xFF181008),
            accent = Color(0xFFEF476F)
        ),
        DynamicPaletteColors(
            primary = Color(0xFF9D4EDD),
            secondary = Color(0xFF3C096C),
            background = Color(0xFF0F0419),
            accent = Color(0xFFE0AAFF)
        )
    )

    suspend fun extractColors(context: Context, track: TrackEntity?): DynamicPaletteColors = withContext(Dispatchers.IO) {
        if (track == null) return@withContext DynamicPaletteColors.Default

        if (!track.albumArtUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(track.albumArtUri)
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val palette = Palette.from(bitmap).maximumColorCount(24).generate()
                    val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) }
                        ?: palette.dominantSwatch?.rgb?.let { Color(it) }
                        ?: Color(0xFF00F2FE)
                    val darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
                        ?: palette.darkMutedSwatch?.rgb?.let { Color(it) }
                        ?: Color(0xFF0A0F1D)
                    val lightVibrant = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
                        ?: Color(0xFF38BDF8)

                    return@withContext DynamicPaletteColors(
                        primary = vibrant,
                        secondary = lightVibrant,
                        background = Color(
                            red = (darkVibrant.red * 0.4f).coerceIn(0f, 1f),
                            green = (darkVibrant.green * 0.4f).coerceIn(0f, 1f),
                            blue = (darkVibrant.blue * 0.4f).coerceIn(0f, 1f),
                            alpha = 1f
                        ),
                        accent = lightVibrant
                    )
                }
            } catch (e: Exception) {
                // Fall through to deterministic preset
            }
        }

        val hash = abs((track.title + track.artist).hashCode())
        presetPalettes[hash % presetPalettes.size]
    }
}
