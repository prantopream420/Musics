package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.local.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicsPlayerEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Polled at 16ms (~60fps) for buttery-smooth timeline scrubbing as specified in PDF
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
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startHighPrecisionTicker()
                } else {
                    _playbackPositionMs.value = exoPlayer.currentPosition
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val dur = exoPlayer.duration
                        if (dur > 0) {
                            _durationMs.value = dur
                        }
                    }
                    Player.STATE_ENDED -> {
                        playNext(userInitiated = false)
                    }
                    else -> Unit
                }
            }
        })
    }

    /**
     * PDF Requirement:
     * "We will implement a custom Compose Canvas timeline polled at 16ms intervals
     * (via a coroutine loop tied to ExoPlayer). This ensures the scrubber advances
     * with buttery-smooth, continuous precision."
     */
    private fun startHighPrecisionTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Main) {
            while (isActive && exoPlayer.isPlaying) {
                _playbackPositionMs.value = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (dur > 0 && dur != _durationMs.value) {
                    _durationMs.value = dur
                }
                delay(16L) // 16ms = ~60 FPS update frequency
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

        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(track.contentUri))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            _isPlaying.value = true
            startHighPrecisionTicker()
        } catch (e: Exception) {
            Log.e("MusicsPlayer", "Failed to start playback for ${track.title}", e)
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            _isPlaying.value = false
        } else {
            if (_currentTrack.value != null) {
                exoPlayer.play()
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
        exoPlayer.seekTo(target)
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

        if (exoPlayer.currentPosition > 3000L) {
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

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    fun release() {
        tickerJob?.cancel()
        exoPlayer.release()
    }
}
