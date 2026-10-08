package com.example.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.local.TrackEntity
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Player engine backed by MediaController → MusicsPlaybackService (MediaSessionService).
 *
 * This wiring means:
 *  - ExoPlayer lives inside the service → keeps playing when the app is backgrounded
 *  - The OS media session is populated → notification shade controls, lock screen,
 *    and the Android Dynamic Island / Now Playing chip all reflect the current track
 *  - Audio focus is handled by the service automatically
 */
@OptIn(UnstableApi::class)
class MusicsPlayerEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _queue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val queue: StateFlow<List<TrackEntity>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private var tickerJob: Job? = null

    init {
        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicsPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                controller?.addListener(playerListener)
            } catch (e: Exception) {
                Log.e("MusicsPlayerEngine", "Failed to connect to MediaSessionService", e)
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
            if (playing) startHighPrecisionTicker() else {
                _playbackPositionMs.value = controller?.currentPosition ?: 0L
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                val dur = controller?.duration ?: 0L
                if (dur > 0) _durationMs.value = dur
            } else if (playbackState == Player.STATE_ENDED) {
                playNext(userInitiated = false)
            }
        }
    }

    private fun startHighPrecisionTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                val c = controller ?: break
                if (!c.isPlaying) break
                _playbackPositionMs.value = c.currentPosition
                val dur = c.duration
                if (dur > 0 && dur != _durationMs.value) _durationMs.value = dur
                delay(16L)
            }
        }
    }

    fun playTrackList(tracks: List<TrackEntity>, startIndex: Int) {
        if (tracks.isEmpty() || startIndex !in tracks.indices) return
        _queue.value = tracks
        _currentIndex.value = startIndex
        playTrack(tracks[startIndex])
    }

    fun playTrack(track: TrackEntity) {
        _currentTrack.value = track
        _durationMs.value = if (track.durationMs > 0) track.durationMs else 1L
        _playbackPositionMs.value = 0L

        val c = controller
        if (c == null) {
            // Controller not yet ready - retry once connected
            scope.launch(Dispatchers.Main) {
                var waited = 0
                while (controller == null && waited < 3000) {
                    delay(100)
                    waited += 100
                }
                controller?.let { playTrackOnController(it, track) }
            }
            return
        }
        playTrackOnController(c, track)
    }

    private fun playTrackOnController(c: MediaController, track: TrackEntity) {
        try {
            val artUri = track.albumArtUri?.let { Uri.parse(it) }
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(artUri)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(track.contentUri))
                .setMediaMetadata(metadata)
                .build()

            c.setMediaItem(mediaItem)
            c.prepare()
            c.play()
            _isPlaying.value = true
            startHighPrecisionTicker()
        } catch (e: Exception) {
            Log.e("MusicsPlayerEngine", "Failed to start playback for ${track.title}", e)
        }
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
            _isPlaying.value = false
        } else {
            if (_currentTrack.value != null) {
                c.play()
                _isPlaying.value = true
                startHighPrecisionTicker()
            } else if (_queue.value.isNotEmpty()) {
                val index = if (_currentIndex.value in _queue.value.indices) _currentIndex.value else 0
                playTrackList(_queue.value, index)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _durationMs.value)
        controller?.seekTo(target)
        _playbackPositionMs.value = target
    }

    fun playNext(userInitiated: Boolean = true) {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_isRepeat.value && !userInitiated) {
            _currentTrack.value?.let { playTrack(it) }
            return
        }

        val nextIndex = if (_isShuffle.value) {
            q.indices.random()
        } else {
            (_currentIndex.value + 1) % q.size
        }

        _currentIndex.value = nextIndex
        playTrack(q[nextIndex])
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if ((controller?.currentPosition ?: 0L) > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (_isShuffle.value) {
            q.indices.random()
        } else {
            if (_currentIndex.value - 1 < 0) q.size - 1 else _currentIndex.value - 1
        }

        _currentIndex.value = prevIndex
        playTrack(q[prevIndex])
    }

    fun toggleShuffle() { _isShuffle.value = !_isShuffle.value }
    fun toggleRepeat()  { _isRepeat.value  = !_isRepeat.value  }

    fun release() {
        tickerJob?.cancel()
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }
}
