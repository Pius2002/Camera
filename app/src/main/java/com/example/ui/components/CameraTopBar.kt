package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.CameraCyan
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
    isFacingFront: Boolean = false,
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
            // 1. Flash Control Button (Rear Camera Flash: Auto, On, Off)
            val flashInteractionSource = remember { MutableInteractionSource() }
            val isFlashPressed by flashInteractionSource.collectIsPressedAsState()
            val flashScale by animateFloatAsState(
                targetValue = if (isFlashPressed) 0.90f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "flash_scale"
            )

            Box(
                modifier = Modifier
                    .scale(flashScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (currentFlash != FlashModeOption.OFF) CameraYellow.copy(alpha = 0.22f)
                        else Color(0x55000000)
                    )
                    .border(
                        width = 1.dp,
                        color = if (currentFlash != FlashModeOption.OFF) CameraYellow.copy(alpha = 0.7f)
                        else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = flashInteractionSource,
                        indication = null
                    ) {
                        // Directly cycle between AUTO -> ON -> OFF -> AUTO
                        val nextMode = when (currentFlash) {
                            FlashModeOption.AUTO -> FlashModeOption.ON
                            FlashModeOption.ON -> FlashModeOption.OFF
                            else -> FlashModeOption.AUTO
                        }
                        onFlashChanged(nextMode)
                        showFlashMenu = true
                        showRatioMenu = false
                        showTimerMenu = false
                        showResMenu = false
                    }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
                    .testTag("flash_control_button")
                    .testTag("flash_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (icon, tint) = when (currentFlash) {
                        FlashModeOption.AUTO -> Icons.Filled.FlashAuto to CameraYellow
                        FlashModeOption.ON -> Icons.Filled.FlashOn to CameraYellow
                        FlashModeOption.OFF -> Icons.Filled.FlashOff to Color.White.copy(alpha = 0.75f)
                        FlashModeOption.TORCH -> Icons.Filled.Highlight to CameraYellow
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Rear Camera Flash ${currentFlash.label}",
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (currentFlash) {
                            FlashModeOption.AUTO -> "AUTO"
                            FlashModeOption.ON -> "ON"
                            FlashModeOption.OFF -> "OFF"
                            FlashModeOption.TORCH -> "ON"
                        },
                        color = if (currentFlash != FlashModeOption.OFF) CameraYellow else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CameraDarkGrey.copy(alpha = 0.95f))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFacingFront) "REAR CAMERA FLASH (Active on Rear Camera)" else "REAR CAMERA FLASH MODE",
                        color = if (isFacingFront) Color.LightGray else CameraYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    IconButton(
                        onClick = { showFlashMenu = false },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close flash menu",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val flashModes = listOf(
                        Triple(FlashModeOption.AUTO, Icons.Filled.FlashAuto, "flash_mode_auto"),
                        Triple(FlashModeOption.ON, Icons.Filled.FlashOn, "flash_mode_on"),
                        Triple(FlashModeOption.OFF, Icons.Filled.FlashOff, "flash_mode_off")
                    )
                    flashModes.forEach { (flash, icon, tag) ->
                        val isSelected = currentFlash == flash
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CameraYellow.copy(alpha = 0.22f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, CameraYellow) else null,
                            modifier = Modifier
                                .clickable {
                                    onFlashChanged(flash)
                                    showFlashMenu = false
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag(tag)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = flash.label,
                                    tint = if (isSelected) CameraYellow else Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(18.dp)
                                )
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
