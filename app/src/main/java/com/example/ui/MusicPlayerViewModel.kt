package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TrackEntity
import com.example.data.model.LibraryTab
import com.example.data.model.ScanSettings
import com.example.data.model.SortOrder
import com.example.data.repository.MusicRepository
import com.example.data.repository.ScanState
import com.example.player.MusicsPlayerEngine
import com.example.ui.theme.DynamicPaletteColors
import com.example.ui.theme.DynamicPaletteManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    val playerEngine = MusicsPlayerEngine(application, viewModelScope)

    // Filter & Sort State
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _selectedTab = MutableStateFlow(LibraryTab.ALL)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val scanState: StateFlow<ScanState> = repository.scanState
    val scanSettings: StateFlow<ScanSettings> = repository.scanSettings

    // Dynamic Tracks Flow based on sort & filtering
    val tracks: StateFlow<List<TrackEntity>> = combine(
        _sortOrder.flatMapLatest { sort -> repository.getTracks(sort) },
        _selectedTab,
        _searchQuery
    ) { allTracks, tab, query ->
        var filtered = when (tab) {
            LibraryTab.ALL -> allTracks
            LibraryTab.LOSSLESS -> allTracks.filter { it.isLossless || it.format == "FLAC" || it.format == "WAV" }
            LibraryTab.FAVORITES -> allTracks.filter { it.isFavorite }
            LibraryTab.ARTISTS, LibraryTab.ALBUMS -> allTracks
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q) ||
                it.format.lowercase().contains(q)
            }
        }
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Playback Engine State passthroughs
    val currentTrack: StateFlow<TrackEntity?> = playerEngine.currentTrack
    val isPlaying: StateFlow<Boolean> = playerEngine.isPlaying
    val playbackPositionMs: StateFlow<Long> = playerEngine.playbackPositionMs
    val durationMs: StateFlow<Long> = playerEngine.durationMs
    val isShuffle: StateFlow<Boolean> = playerEngine.isShuffle
    val isRepeat: StateFlow<Boolean> = playerEngine.isRepeat
    val queue: StateFlow<List<TrackEntity>> = playerEngine.queue

    // Dynamic Palette from album art / track
    private val _dynamicPalette = MutableStateFlow(DynamicPaletteColors.Default)
    val dynamicPalette: StateFlow<DynamicPaletteColors> = _dynamicPalette.asStateFlow()

    // Navigation and Panels (Predictive Hierarchical Back-stack)
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isSortDrawerOpen = MutableStateFlow(false)
    val isSortDrawerOpen: StateFlow<Boolean> = _isSortDrawerOpen.asStateFlow()

    private val _selectedArtist = MutableStateFlow<String?>(null)
    val selectedArtist: StateFlow<String?> = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<String?>(null)
    val selectedAlbum: StateFlow<String?> = _selectedAlbum.asStateFlow()

    // V2 Cloud Roadmap & Monetization (per PDF page 3)
    private val _isV2RoadmapOpen = MutableStateFlow(false)
    val isV2RoadmapOpen: StateFlow<Boolean> = _isV2RoadmapOpen.asStateFlow()

    private val _isProUnlocked = MutableStateFlow(false)
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private val _otaStatus = MutableStateFlow<String?>(null)
    val otaStatus: StateFlow<String?> = _otaStatus.asStateFlow()

    init {
        // Initial scan / demo load
        viewModelScope.launch {
            repository.rescanLibrary()
        }

        // Observe current track for dynamic palette extraction
        viewModelScope.launch {
            playerEngine.currentTrack.collect { track ->
                val colors = DynamicPaletteManager.extractColors(getApplication(), track)
                _dynamicPalette.value = colors
            }
        }
    }

    fun playTrack(track: TrackEntity, trackList: List<TrackEntity>? = null) {
        val list = trackList ?: tracks.value
        val index = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playerEngine.playTrackList(list, index)
    }

    fun togglePlayPause() = playerEngine.togglePlayPause()

    fun seekTo(positionMs: Long) = playerEngine.seekTo(positionMs)

    fun playNext() = playerEngine.playNext()

    fun playPrevious() = playerEngine.playPrevious()

    fun toggleShuffle() = playerEngine.toggleShuffle()

    fun toggleRepeat() = playerEngine.toggleRepeat()

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _isSortDrawerOpen.value = false
    }

    fun setSelectedTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun setSortDrawerOpen(open: Boolean) {
        _isSortDrawerOpen.value = open
    }

    fun setV2RoadmapOpen(open: Boolean) {
        _isV2RoadmapOpen.value = open
    }

    fun selectArtist(artist: String?) {
        _selectedArtist.value = artist
    }

    fun selectAlbum(album: String?) {
        _selectedAlbum.value = album
    }

    fun updateScanSettings(bypass60s: Boolean, includeVoiceNotes: Boolean) {
        viewModelScope.launch {
            repository.updateScanSettings(
                ScanSettings(
                    bypass60SecondRule = bypass60s,
                    includeVoiceNotesAndRingtones = includeVoiceNotes
                )
            )
        }
    }

    fun triggerSmartScan() {
        viewModelScope.launch {
            repository.rescanLibrary()
        }
    }

    fun unlockProWithLicenseKey(key: String): Boolean {
        if (key.trim().length >= 6) {
            _isProUnlocked.value = true
            return true
        }
        return false
    }

    fun checkGitHubOtaUpdate() {
        _otaStatus.value = "Checking GitHub Releases... v1.0.0 is the latest stable build"
    }

    override fun onCleared() {
        super.onCleared()
        playerEngine.release()
    }
}
