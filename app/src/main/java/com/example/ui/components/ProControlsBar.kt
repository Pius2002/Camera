package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ProSettings
import com.example.data.model.WhiteBalanceOption
import com.example.ui.theme.CameraAmber
import com.example.ui.theme.CameraDarkGrey
import com.example.ui.theme.CameraYellow

enum class ProControlTab {
    NONE,
    ISO,
    SHUTTER,
    FOCUS,
    EV,
    WB
}

@Composable
fun ProControlsBar(
    settings: ProSettings,
    onSettingsChanged: (ProSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ProControlTab.NONE) }

    val isoPresets = listOf(
        null to "Auto",
        50 to "50",
        100 to "100",
        200 to "200",
        400 to "400",
        800 to "800",
        1600 to "1600",
        3200 to "3200",
        6400 to "6400"
    )

    val shutterPresets = listOf(
        null to ("Auto" to 0L),
        "1/4000" to ("1/4000" to 250_000L),
        "1/2000" to ("1/2000" to 500_000L),
        "1/1000" to ("1/1000" to 1_000_000L),
        "1/500" to ("1/500" to 2_000_000L),
        "1/250" to ("1/250" to 4_000_000L),
        "1/125" to ("1/125" to 8_000_000L),
        "1/60" to ("1/60" to 16_666_666L),
        "1/30" to ("1/30" to 33_333_333L),
        "1/15" to ("1/15" to 66_666_666L),
        "1/8" to ("1/8" to 125_000_000L),
        "1/4" to ("1/4" to 250_000_000L),
        "1/2" to ("1/2" to 500_000_000L),
        "1s" to ("1s" to 1_000_000_000L),
        "2s" to ("2s" to 2_000_000_000L)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xDD12141A))
            .padding(vertical = 8.dp)
            .testTag("pro_controls_panel")
    ) {
        // Active Sub-Control Slider / Selector Drawer
        AnimatedVisibility(
            visible = activeTab != ProControlTab.NONE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CameraDarkGrey)
                    .padding(8.dp)
            ) {
                when (activeTab) {
                    ProControlTab.ISO -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            isoPresets.forEach { (isoVal, label) ->
                                val isSelected = if (isoVal == null) !settings.isIsoManual else (settings.isIsoManual && settings.iso == isoVal)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isoVal == null) {
                                            onSettingsChanged(settings.copy(isIsoManual = false))
                                        } else {
                                            onSettingsChanged(settings.copy(isIsoManual = true, iso = isoVal))
                                        }
                                    },
                                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CameraYellow,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0x33FFFFFF),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ProControlTab.SHUTTER -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            shutterPresets.forEach { (_, data) ->
                                val (display, nanos) = data
                                val isAuto = display == "Auto"
                                val isSelected = if (isAuto) !settings.isShutterManual else (settings.isShutterManual && settings.shutterDisplay == display)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isAuto) {
                                            onSettingsChanged(settings.copy(isShutterManual = false))
                                        } else {
                                            onSettingsChanged(settings.copy(isShutterManual = true, shutterNanos = nanos, shutterDisplay = display))
                                        }
                                    },
                                    label = { Text(display, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CameraYellow,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0x33FFFFFF),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ProControlTab.FOCUS -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (!settings.isFocusManual) "AF (Auto Focus)" else "MF (Manual Focus): ${String.format("%.2f", settings.focusDistance)}",
                                    color = if (settings.isFocusManual) CameraYellow else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = {
                                        onSettingsChanged(settings.copy(isFocusManual = !settings.isFocusManual))
                                    }
                                ) {
                                    Text(
                                        text = if (settings.isFocusManual) "Switch to AF" else "Switch to MF",
                                        color = CameraYellow,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (settings.isFocusManual) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Macro", color = Color(0x88FFFFFF), fontSize = 11.sp)
                                    Slider(
                                        value = settings.focusDistance,
                                        onValueChange = { dist ->
                                            onSettingsChanged(settings.copy(isFocusManual = true, focusDistance = dist))
                                        },
                                        valueRange = 0.0f..10.0f,
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = CameraYellow,
                                            activeTrackColor = CameraYellow
                                        )
                                    )
                                    Text("Infinity", color = Color(0x88FFFFFF), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    ProControlTab.EV -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Exposure Compensation: ${if (settings.evCompensation > 0) "+${settings.evCompensation}" else "${settings.evCompensation}"} EV",
                                color = CameraYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = settings.evCompensation.toFloat(),
                                onValueChange = { ev ->
                                    onSettingsChanged(settings.copy(evCompensation = ev.toInt()))
                                },
                                valueRange = -6f..6f,
                                steps = 11,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = CameraYellow,
                                    activeTrackColor = CameraYellow
                                )
                            )
                        }
                    }

                    ProControlTab.WB -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WhiteBalanceOption.entries.forEach { wb ->
                                val isSelected = settings.whiteBalance == wb
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onSettingsChanged(settings.copy(whiteBalance = wb))
                                    },
                                    label = { Text(wb.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CameraYellow,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0x33FFFFFF),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ProControlTab.NONE -> {}
                }
            }
        }

        // Main Tab Buttons (ISO | SPEED | EV | FOCUS | WB | RESET)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ISO Button
            ProPillButton(
                title = "ISO",
                value = if (settings.isIsoManual) "${settings.iso}" else "AUTO",
                isActive = activeTab == ProControlTab.ISO,
                isModified = settings.isIsoManual,
                onClick = {
                    activeTab = if (activeTab == ProControlTab.ISO) ProControlTab.NONE else ProControlTab.ISO
                }
            )

            // Shutter Speed Button
            ProPillButton(
                title = "SPEED",
                value = if (settings.isShutterManual) settings.shutterDisplay else "AUTO",
                isActive = activeTab == ProControlTab.SHUTTER,
                isModified = settings.isShutterManual,
                onClick = {
                    activeTab = if (activeTab == ProControlTab.SHUTTER) ProControlTab.NONE else ProControlTab.SHUTTER
                }
            )

            // Focus Button
            ProPillButton(
                title = "FOCUS",
                value = if (settings.isFocusManual) "MF" else "AF",
                isActive = activeTab == ProControlTab.FOCUS,
                isModified = settings.isFocusManual,
                onClick = {
                    activeTab = if (activeTab == ProControlTab.FOCUS) ProControlTab.NONE else ProControlTab.FOCUS
                }
            )

            // EV Button
            ProPillButton(
                title = "EV",
                value = if (settings.evCompensation != 0) "${if (settings.evCompensation > 0) "+" else ""}${settings.evCompensation}" else "0.0",
                isActive = activeTab == ProControlTab.EV,
                isModified = settings.evCompensation != 0,
                onClick = {
                    activeTab = if (activeTab == ProControlTab.EV) ProControlTab.NONE else ProControlTab.EV
                }
            )

            // WB Button
            ProPillButton(
                title = "WB",
                value = settings.whiteBalance.label,
                isActive = activeTab == ProControlTab.WB,
                isModified = settings.whiteBalance != WhiteBalanceOption.AUTO,
                onClick = {
                    activeTab = if (activeTab == ProControlTab.WB) ProControlTab.NONE else ProControlTab.WB
                }
            )

            // Reset Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .clickable {
                        onSettingsChanged(ProSettings())
                        activeTab = ProControlTab.NONE
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("RESET", color = Color(0xBBFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProPillButton(
    title: String,
    value: String,
    isActive: Boolean,
    isModified: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) CameraYellow else if (isModified) CameraAmber.copy(alpha = 0.25f) else Color(0x33FFFFFF))
            .border(
                1.dp,
                if (isActive) CameraYellow else if (isModified) CameraAmber else Color(0x22FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isActive) Color.Black else if (isModified) CameraAmber else Color(0x99FFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = if (isActive) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
