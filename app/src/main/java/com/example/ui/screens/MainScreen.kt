package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TrackEntity
import com.example.data.model.LibraryTab
import com.example.ui.MusicPlayerViewModel
import com.example.ui.components.FrostedGlassBox
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.TrackItemView
import com.example.ui.theme.AppTheme
import com.example.ui.theme.NeonCyan




@Composable
fun MainScreen(
    viewModel: MusicPlayerViewModel,
    onRequestStoragePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val positionMs by viewModel.playbackPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val isRepeat by viewModel.isRepeat.collectAsStateWithLifecycle()
    val palette by viewModel.dynamicPalette.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scanSettings by viewModel.scanSettings.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val isProUnlocked by viewModel.isProUnlocked.collectAsStateWithLifecycle()
    val otaStatus by viewModel.otaStatus.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isSortDrawerOpen by viewModel.isSortDrawerOpen.collectAsStateWithLifecycle()

    var isSearchVisible by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // HIERARCHICAL NAVIGATION & GRACEFUL EXIT LOGIC (Per PDF Page 1)
    BackHandler(enabled = true) {
        when {
            isNowPlayingExpanded -> viewModel.setNowPlayingExpanded(false)
            isSettingsOpen -> viewModel.setSettingsOpen(false)
            isSortDrawerOpen -> viewModel.setSortDrawerOpen(false)
            searchQuery.isNotEmpty() -> viewModel.setSearchQuery("")
            isSearchVisible -> isSearchVisible = false
            selectedTab != LibraryTab.ALL -> viewModel.setSelectedTab(LibraryTab.ALL)
            else -> {
                // Absolute root reached (Home tab)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000L) {
                    // Minimize application seamlessly preserving the playback session
                    activity?.moveTaskToBack(true)
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LiquidGlassBackground(
        palette = palette,
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brand Logo & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.setSelectedTab(LibraryTab.ALL) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(palette.primary, palette.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFF060911),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Musics",
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x3300F2FE))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "V1",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = sortOrder.label,
                                color = AppTheme.colors.textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action Icons: Search, Sort Drawer, Settings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isSearchVisible = !isSearchVisible },
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchVisible || searchQuery.isNotEmpty()) palette.primary else AppTheme.colors.textPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setSortDrawerOpen(true) },
                            modifier = Modifier.testTag("sort_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Menu",
                                tint = AppTheme.colors.textPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setSettingsOpen(true) },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = AppTheme.colors.textPrimary
                            )
                        }
                    }
                }

                // Expandable Search Bar
                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        )
                    ),
                    exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search title, artist, format, or album...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.primary,
                                unfocusedBorderColor = Color(0x3394A3B8),
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Filter Tabs (All, Hi-Res Lossless, Favorites, Artists, Albums)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = palette.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = palette.primary,
                            height = 2.5.dp
                        )
                    },
                    divider = {}
                ) {
                    LibraryTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { viewModel.setSelectedTab(tab) },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) palette.primary else AppTheme.colors.textSecondary
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Track Count & Smart Scan Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tracks.size} TRACKS • ${if (scanSettings.bypass60SecondRule) "ALL SIZES" else "FILTERED (>60s)"}",
                        color = AppTheme.colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1F00F2FE))
                            .clickable {
                                onRequestStoragePermission()
                                viewModel.triggerSmartScan()
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Smart Scan",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main Tracks LazyColumn
                if (tracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FrostedGlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0x2E1E293B)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Audio Tracks In This View",
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Run Smart Scan to query device storage or adjust your 60-second junk filtering criteria.",
                                    color = AppTheme.colors.textSecondary,
                                    fontSize = 12.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        onRequestStoragePermission()
                                        viewModel.triggerSmartScan()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Scan MediaStore Now",
                                        color = Color(0xFF090D17),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("tracks_list"),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 6.dp,
                            bottom = if (currentTrack != null) 90.dp else 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = tracks,
                            key = { it.id }
                        ) { track ->
                            val isPlayingThis = (currentTrack?.id == track.id) && isPlaying
                            TrackItemView(
                                track = track,
                                isPlayingThis = isPlayingThis,
                                onTrackClick = { viewModel.playTrack(track, tracks) },
                                onFavoriteToggle = { viewModel.toggleFavorite(track) }
                            )
                        }
                    }
                }
            }

            // Floating Frosted Glass Mini Player Bar (Bottom)
            if (currentTrack != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    MiniPlayerBar(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        onExpandNowPlaying = { viewModel.setNowPlayingExpanded(true) },
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        accentColor = palette.primary
                    )
                }
            }

            // Slide-out Contextual Sorting Side Drawer Overlay
            AnimatedVisibility(
                visible = isSortDrawerOpen,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x77000000))
                        .clickable { viewModel.setSortDrawerOpen(false) }
                ) {
                    SortDrawerSheet(
                        currentSort = sortOrder,
                        onSortSelected = { viewModel.setSortOrder(it) },
                        onOpenSettings = { viewModel.setSettingsOpen(true) },
                        onClose = { viewModel.setSortDrawerOpen(false) }
                    )
                }
            }

            // Full-Screen Settings & V2 Roadmap Screen Overlay
            AnimatedVisibility(
                visible = isSettingsOpen,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                SettingsAndRoadmapSheet(
                    scanSettings = scanSettings,
                    scanState = scanState,
                    isProUnlocked = isProUnlocked,
                    otaStatus = otaStatus,
                    palette = palette,
                    onClose = { viewModel.setSettingsOpen(false) },
                    onUpdateScanSettings = { bypass, include ->
                        viewModel.updateScanSettings(bypass, include)
                    },
                    onTriggerScan = {
                        onRequestStoragePermission()
                        viewModel.triggerSmartScan()
                    },
                    onActivateLicenseKey = { key ->
                        viewModel.unlockProWithLicenseKey(key)
                    },
                    onCheckOta = {
                        viewModel.checkGitHubOtaUpdate()
                    },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            }

            // Full-Screen Liquid Glass Now Playing Overlay
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NowPlayingSheet(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    palette = palette,
                    isShuffle = isShuffle,
                    isRepeat = isRepeat,
                    onCollapse = { viewModel.setNowPlayingExpanded(false) },
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onSeek = { targetMs -> viewModel.seekTo(targetMs) },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onShuffleToggle = { viewModel.toggleShuffle() },
                    onRepeatToggle = { viewModel.toggleRepeat() },
                    onFavoriteToggle = {
                        currentTrack?.let { viewModel.toggleFavorite(it) }
                    }
                )
            }
        }
    }
}
