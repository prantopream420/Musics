package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SortOrder
import com.example.ui.components.FrostedGlassBox
import com.example.ui.theme.AppTheme
import com.example.ui.theme.NeonCyan




@Composable
fun SortDrawerSheet(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassBox(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("sort_side_drawer"),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        backgroundColor = Color(0xEB0A0F1D),
        borderColor = Color(0x3D94A3B8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIBRARY SORTING",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Sort MediaStore",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_sort_drawer")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DICTATE QUERY SORT_ORDER",
                color = AppTheme.colors.textMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            SortItem(
                label = "Date Added (Time)",
                description = "Most recently scanned or downloaded",
                icon = Icons.Default.AccessTime,
                isSelected = currentSort == SortOrder.DATE_ADDED,
                onClick = { onSortSelected(SortOrder.DATE_ADDED) }
            )

            SortItem(
                label = "Alphabetical (Name)",
                description = "Standard A-Z lexicographical order",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSort == SortOrder.ALPHABETICAL,
                onClick = { onSortSelected(SortOrder.ALPHABETICAL) }
            )

            SortItem(
                label = "File Weight (Size)",
                description = "High-bitrate & lossless audio files first",
                icon = Icons.Default.FolderZip,
                isSelected = currentSort == SortOrder.FILE_SIZE,
                onClick = { onSortSelected(SortOrder.FILE_SIZE) }
            )

            SortItem(
                label = "Artist (A-Z)",
                description = "Group alphabetically by artist name",
                icon = Icons.Default.Person,
                isSelected = currentSort == SortOrder.ARTIST,
                onClick = { onSortSelected(SortOrder.ARTIST) }
            )

            SortItem(
                label = "Duration (Length)",
                description = "Longest tracks and continuous sets",
                icon = Icons.Default.Timer,
                isSelected = currentSort == SortOrder.DURATION,
                onClick = { onSortSelected(SortOrder.DURATION) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Link to Smart Scan & Junk Filtering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x221E293B))
                    .clickable {
                        onClose()
                        onOpenSettings()
                    }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart Scan & Junk Filter",
                            color = AppTheme.colors.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure 60s rule & cache",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortItem(
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x3300F2FE) else Color(0x141E293B))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else AppTheme.colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = if (isSelected) NeonCyan else AppTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = AppTheme.colors.textMuted,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
