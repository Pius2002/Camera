package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioOption
import com.example.data.model.FlashModeOption
import com.example.data.model.ResolutionOption
import com.example.data.model.StorageInfo
import com.example.data.model.TimerOption
import com.example.ui.theme.CameraDarkGrey
import com.example.ui.theme.CameraYellow

@Composable
fun CameraTopBar(
    currentFlash: FlashModeOption,
    onFlashChanged: (FlashModeOption) -> Unit,
    currentAspectRatio: AspectRatioOption,
    onAspectRatioChanged: (AspectRatioOption) -> Unit,
    currentResolution: ResolutionOption,
    onResolutionChanged: (ResolutionOption) -> Unit,
    currentTimer: TimerOption,
    onTimerChanged: (TimerOption) -> Unit,
    onOpenSettings: () -> Unit,
    isVideoMode: Boolean = false,
    maxVideoResolution: ResolutionOption = ResolutionOption.RES_4K,
    storageInfo: StorageInfo? = null,
    modifier: Modifier = Modifier
) {
    var showFlashMenu by remember { mutableStateOf(false) }
    var showRatioMenu by remember { mutableStateOf(false) }
    var showTimerMenu by remember { mutableStateOf(false) }
    var showResMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Flash Toggle Button
            IconButton(
                onClick = {
                    showFlashMenu = !showFlashMenu
                    showRatioMenu = false
                    showTimerMenu = false
                    showResMenu = false
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .testTag("flash_button")
            ) {
                val (icon, tint) = when (currentFlash) {
                    FlashModeOption.ON -> Icons.Filled.FlashOn to CameraYellow
                    FlashModeOption.AUTO -> Icons.Filled.FlashAuto to CameraYellow
                    FlashModeOption.TORCH -> Icons.Filled.Highlight to CameraYellow
                    FlashModeOption.OFF -> Icons.Filled.FlashOff to Color.White
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Flash ${currentFlash.label}",
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Aspect Ratio Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x55000000))
                    .clickable {
                        showRatioMenu = !showRatioMenu
                        showFlashMenu = false
                        showTimerMenu = false
                        showResMenu = false
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("aspect_ratio_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentAspectRatio.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. Resolution Badge (8K / 4K / FHD)
            val is8K = currentResolution == ResolutionOption.RES_8K
            val isMaxVideo = isVideoMode && currentResolution == maxVideoResolution
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (is8K) CameraYellow else Color(0x66000000))
                    .border(
                        1.dp,
                        if (is8K || isMaxVideo) CameraYellow else Color(0x44FFFFFF),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        showResMenu = !showResMenu
                        showFlashMenu = false
                        showRatioMenu = false
                        showTimerMenu = false
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("resolution_badge"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentResolution.label,
                        color = if (is8K) Color.Black else CameraYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (isMaxVideo) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (is8K) Color.Black else CameraYellow)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "MAX",
                                color = if (is8K) CameraYellow else Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 4. Timer Button
            IconButton(
                onClick = {
                    showTimerMenu = !showTimerMenu
                    showFlashMenu = false
                    showRatioMenu = false
                    showResMenu = false
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .testTag("timer_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Timer ${currentTimer.label}",
                        tint = if (currentTimer != TimerOption.OFF) CameraYellow else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    if (currentTimer != TimerOption.OFF) {
                        Text(
                            text = currentTimer.label,
                            color = CameraYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. Settings Button
            IconButton(
                onClick = {
                    showFlashMenu = false
                    showRatioMenu = false
                    showTimerMenu = false
                    showResMenu = false
                    onOpenSettings()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Camera Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Sub-menus popdowns
        AnimatedVisibility(visible = showFlashMenu, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CameraDarkGrey.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlashModeOption.entries.forEach { flash ->
                    val isSelected = currentFlash == flash
                    TextButton(
                        onClick = {
                            onFlashChanged(flash)
                            showFlashMenu = false
                        }
                    ) {
                        Text(
                            text = flash.label,
                            color = if (isSelected) CameraYellow else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showRatioMenu, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CameraDarkGrey.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AspectRatioOption.entries.forEach { ratio ->
                    val isSelected = currentAspectRatio == ratio
                    TextButton(
                        onClick = {
                            onAspectRatioChanged(ratio)
                            showRatioMenu = false
                        }
                    ) {
                        Text(
                            text = ratio.label,
                            color = if (isSelected) CameraYellow else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showResMenu, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CameraDarkGrey.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResolutionOption.entries.forEach { res ->
                    val isSelected = currentResolution == res
                    val isMax = isVideoMode && res == maxVideoResolution
                    TextButton(
                        onClick = {
                            onResolutionChanged(res)
                            showResMenu = false
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = res.label,
                                    color = if (isSelected) CameraYellow else Color.White,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (isMax) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(CameraYellow)
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    ) {
                                        Text("MAX", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }
                                }
                            }
                            if (isVideoMode) {
                                Text(
                                    text = "~${res.storagePerMinuteMb}MB/m",
                                    color = Color(0xAAFFFFFF),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Storage & Recording Estimate Pill (In Video / Dual mode)
        if (isVideoMode && storageInfo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when {
                        storageInfo.isCriticalStorage -> Color(0xDDB00020)
                        storageInfo.isLowStorage -> Color(0xDDFF9800)
                        else -> Color(0x77000000)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                storageInfo.isCriticalStorage || storageInfo.isLowStorage -> Icons.Filled.Warning
                                else -> Icons.Filled.SdCard
                            },
                            contentDescription = "Storage status",
                            tint = if (storageInfo.isCriticalStorage) Color.White else if (storageInfo.isLowStorage) Color.Black else CameraYellow,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${storageInfo.availableFormatted} · ~${storageInfo.estimatedTimeFormatted} left at ${currentResolution.label}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showTimerMenu, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CameraDarkGrey.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimerOption.entries.forEach { timer ->
                    val isSelected = currentTimer == timer
                    TextButton(
                        onClick = {
                            onTimerChanged(timer)
                            showTimerMenu = false
                        }
                    ) {
                        Text(
                            text = timer.label,
                            color = if (isSelected) CameraYellow else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
