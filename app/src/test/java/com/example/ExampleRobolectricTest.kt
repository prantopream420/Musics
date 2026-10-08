package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string resource matches Musics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Musics", appName)
    }

    @Test
    fun `test audiophile badge formatting for FLAC and MP3`() {
        val flacTrack = TrackEntity(
            id = 1L,
            contentUri = "content://media/external/audio/media/1",
            filePath = "/storage/emulated/0/Music/song.flac",
            title = "Lossless Symphony",
            artist = "Audiophile Artist",
            album = "Hi-Res Collection",
            durationMs = 180000L,
            bitrateBps = 4608000,
            sampleRateHz = 96000,
            bitDepth = 24,
            format = "FLAC",
            sizeBytes = 64000000L,
            dateAdded = 1700000000L,
            isLossless = true
        )
        assertEquals("FLAC 24-bit / 96kHz", flacTrack.audiophileBadge)

        val mp3Track = TrackEntity(
            id = 2L,
            contentUri = "content://media/external/audio/media/2",
            filePath = "/storage/emulated/0/Music/song.mp3",
            title = "Standard Track",
            artist = "Pop Artist",
            album = "Hit Singles",
            durationMs = 120000L,
            bitrateBps = 320000,
            sampleRateHz = 44100,
            bitDepth = 16,
            format = "MP3",
            sizeBytes = 8000000L,
            dateAdded = 1700000000L,
            isLossless = false
        )
        assertEquals("MP3 320 kbps", mp3Track.audiophileBadge)
    }
}
