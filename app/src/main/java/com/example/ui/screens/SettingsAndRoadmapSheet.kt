package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ScanSettings
import com.example.data.repository.ScanState
import com.example.ui.components.FrostedGlassBox
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.DynamicPaletteColors
import com.example.ui.theme.HighResGreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.NeonCyan



import com.example.ui.theme.VibrantMagenta

@Composable
fun SettingsAndRoadmapSheet(
    scanSettings: ScanSettings,
    scanState: ScanState,
    isProUnlocked: Boolean,
    otaStatus: String?,
    palette: DynamicPaletteColors,
    onClose: () -> Unit,
    onUpdateScanSettings: (Boolean, Boolean) -> Unit,
    onTriggerScan: () -> Unit,
    onActivateLicenseKey: (String) -> Boolean,
    onCheckOta: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) { onClose() }

    var licenseInput by remember { mutableStateOf("") }
    var licenseFeedback by remember { mutableStateOf<String?>(null) }

    LiquidGlassBackground(
        palette = palette,
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Musics Engine Settings",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Smart Scanner & V2 Architecture",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // SECTION 1: SMART SCAN & JUNK FILTERING
            SectionHeader(text = "APPEARANCE & THEME", color = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))
            FrostedGlassBox(
                modifier = Modifier.fillMaxWidth().testTag("appearance_card"),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0x2E1E293B),
                borderColor = Color(0x3B94A3B8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ToggleRow(
                        title = "Dark Mode",
                        description = "Enable Obsidian Liquid Glass Dark Mode. Disable for Light Mode.",
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() },
                        testTag = "dark_mode_switch"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "MEDIASTORE SCANNING & JUNK FILTERING", color = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))

            FrostedGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_scan_settings_card"),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0x2E1E293B),
                borderColor = Color(0x3B94A3B8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ToggleRow(
                        title = "Bypass 60-Second Filter",
                        description = "When OFF, files under 60s are excluded to hide voice notes & ringtones. Toggling re-scans the Room DB cache.",
                        checked = scanSettings.bypass60SecondRule,
                        onCheckedChange = { bypass ->
                            onUpdateScanSettings(bypass, scanSettings.includeVoiceNotesAndRingtones)
                        },
                        testTag = "bypass_60s_switch"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ToggleRow(
                        title = "Include WhatsApp & Voice Notes",
                        description = "Include WhatsApp Audio, Telegram, call recordings, alarms & system tones.",
                        checked = scanSettings.includeVoiceNotesAndRingtones,
                        onCheckedChange = { include ->
                            onUpdateScanSettings(scanSettings.bypass60SecondRule, include)
                        },
                        testTag = "include_voice_notes_switch"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onTriggerScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rescan_library_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x3B00F2FE)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Re-Run Smart Scan Now",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }
                    }

                    when (scanState) {
                        is ScanState.Scanning -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    progress = { scanState.progress },
                                    modifier = Modifier.size(16.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scanning MediaStore: ${scanState.scannedCount} tracks found",
                                    color = AppTheme.colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        is ScanState.Completed -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = HighResGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scan Complete • ${scanState.totalScanned} files cached (${scanState.filteredOutCount} filtered)",
                                    color = HighResGreen,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        is ScanState.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Scan error: ${scanState.message}",
                                color = Color(0xFFFF5252),
                                fontSize = 11.5.sp
                            )
                        }
                        else -> Unit
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: AUDIO ENGINE
            SectionHeader(text = "AUDIO ENGINE & DECODER STATUS", color = AudiophileGold)
            Spacer(modifier = Modifier.height(8.dp))

            FrostedGlassBox(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0x2E1E293B),
                borderColor = Color(0x3B94A3B8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DecoderRow(
                        label = "Backend Engine",
                        value = "AndroidX Media3 ExoPlayer",
                        status = "Hardware Accelerated"
                    )
                    DecoderRow(
                        label = "Playback Service",
                        value = "MusicsPlaybackService (Foreground)",
                        status = "Active"
                    )
                    DecoderRow(
                        label = "Metadata Inspector",
                        value = "MediaMetadataRetriever + Room Cache",
                        status = "Active"
                    )
                    DecoderRow(
                        label = "Supported Formats",
                        value = "FLAC, WAV, ALAC, MP3, AAC, OGG",
                        status = "Native Lossless"
                    )
                    DecoderRow(
                        label = "Max Supported Resolution",
                        value = "24-bit / 192 kHz Stereo",
                        status = "Audiophile Ready"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: V2 ROADMAP
            SectionHeader(text = "V2 ROADMAP & ARCHITECTURE (PREVIEW)", color = VibrantMagenta)
            Spacer(modifier = Modifier.height(8.dp))

            FrostedGlassBox(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0x2E1E293B),
                borderColor = Color(0x3B94A3B8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RoadmapTile(
                        icon = Icons.Default.CloudQueue,
                        title = "MicroG / GmsCore Auth",
                        subtitle = "Extracts Google Account OAuth token to preserve personal YouTube Music algorithm recommendations."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RoadmapTile(
                        icon = Icons.Default.FilterAlt,
                        title = "InnerTube Feed Extraction",
                        subtitle = "Reverse-engineered endpoints to stream 'Mixed for You' and 'Discover' feeds directly into V2."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RoadmapTile(
                        icon = Icons.Default.CloudDownload,
                        title = "WorkManager + FFmpegKit Pipeline",
                        subtitle = "Multiplexes raw AAC/Opus streams with high-resolution ID3 artwork tags into your local V1 library."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 4: LICENSE & OTA
            SectionHeader(text = "LICENSE KEY & OVER-THE-AIR UPDATER", color = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))

            FrostedGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("license_key_card"),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0x2E1E293B),
                borderColor = Color(0x3B94A3B8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProUnlocked) Icons.Default.Verified else Icons.Default.Key,
                            contentDescription = null,
                            tint = if (isProUnlocked) AudiophileGold else NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProUnlocked) "Pro License Activated" else "Direct Purchase License Key",
                            color = AppTheme.colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "V1 local playback is free indefinitely. Entering your license key permanently unlocks background downloading, cloud sync, and the high-res quality selector.",
                        color = AppTheme.colors.textMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isProUnlocked) {
                        OutlinedTextField(
                            value = licenseInput,
                            onValueChange = {
                                licenseInput = it
                                licenseFeedback = null
                            },
                            placeholder = { Text("Enter license key (e.g. MUSIC-PRO-XXXX)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("license_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0x4094A3B8),
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val success = onActivateLicenseKey(licenseInput)
                                licenseFeedback = if (success) {
                                    "License verified! Pro features unlocked."
                                } else {
                                    "Invalid license format. Minimum 6 characters required."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("activate_license_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Activate Permanent License",
                                color = Color(0xFF090D17),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        licenseFeedback?.let { msg ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg,
                                color = if (msg.contains("verified")) HighResGreen else Color(0xFFFF5252),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33FFD166))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✓ Permanent Audiophile & Cloud Key Verified. Unlimited access granted.",
                                color = AudiophileGold,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // OTA Updates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Over-The-Air (OTA) Updates",
                                color = AppTheme.colors.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Direct community distribution via GitHub Releases",
                                color = AppTheme.colors.textMuted,
                                fontSize = 11.5.sp
                            )
                        }

                        IconButton(
                            onClick = onCheckOta,
                            modifier = Modifier.testTag("check_ota_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Check OTA",
                                tint = NeonCyan
                            )
                        }
                    }

                    otaStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status,
                            color = NeonCyan,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ─── Reusable private composables ────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = AppTheme.colors.textMuted,
                fontSize = 11.5.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = Color(0xFF004D5A)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DecoderRow(label: String, value: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = AppTheme.colors.textMuted, fontSize = 11.sp)
            Text(text = value, color = AppTheme.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x1FFFFD166))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(text = status, color = AudiophileGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RoadmapTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x22F72585)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VibrantMagenta,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = AppTheme.colors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = AppTheme.colors.textSecondary,
                fontSize = 11.5.sp
            )
        }
    }
}
