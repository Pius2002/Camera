package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HardwareCameraCapabilities
import com.example.data.model.ResolutionOption
import com.example.data.model.StorageInfo
import com.example.ui.theme.CameraAmber
import com.example.ui.theme.CameraCyan
import com.example.ui.theme.CameraDarkGrey
import com.example.ui.theme.CameraYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    capabilities: HardwareCameraCapabilities,
    selectedResolution: ResolutionOption,
    onResolutionSelected: (ResolutionOption) -> Unit,
    selectedVideoResolution: ResolutionOption,
    onVideoResolutionSelected: (ResolutionOption) -> Unit,
    storageInfo: StorageInfo,
    useHevc: Boolean,
    onUseHevcChanged: (Boolean) -> Unit,
    autoStorageProtection: Boolean,
    onAutoStorageProtectionChanged: (Boolean) -> Unit,
    showGrid: Boolean,
    onShowGridChanged: (Boolean) -> Unit,
    showLeveler: Boolean,
    onShowLevelerChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CameraDarkGrey,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .testTag("camera_settings_sheet")
        ) {
            Text(
                text = "Camera & Storage Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Hardware Sensor Limit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = CameraYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detected Hardware Capabilities",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SensorSpecRow(
                        label = "Max Supported Video:",
                        value = "${capabilities.maxVideoResolution.label} (${capabilities.maxVideoResolution.width}×${capabilities.maxVideoResolution.height})"
                    )
                    SensorSpecRow(
                        label = "8K Video Capture:",
                        value = if (capabilities.supports8KVideo) "Supported (Full Sensor 8K)" else "Max hardware: ${capabilities.maxVideoResolution.label}"
                    )
                    SensorSpecRow(
                        label = "4K UHD Video Capture:",
                        value = if (capabilities.supports4KVideo) "Supported (2160p UHD)" else "FHD Only"
                    )
                    SensorSpecRow(
                        label = "HEVC Hardware Encoder:",
                        value = if (capabilities.supportsHevc) "Active (H.265 Space-Saving)" else "AVC H.264"
                    )
                    SensorSpecRow(
                        label = "Max Photo Sensor:",
                        value = "${capabilities.maxPhotoWidth}×${capabilities.maxPhotoHeight} (~${String.format("%.1f", capabilities.maxMegaPixels)} MP)"
                    )
                    SensorSpecRow(
                        label = "Dual Camera Capture:",
                        value = if (capabilities.supportsConcurrentCamera) "Concurrent Hardware" else "Active (Dynamic Viewfinder)"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // VIDEO RESOLUTION PICKER
            Text(
                text = "HIGH-RESOLUTION VIDEO RECORDING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CameraYellow,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            ResolutionOption.entries.forEach { option ->
                val isSelected = selectedVideoResolution == option
                val isMaxSupported = option == capabilities.maxVideoResolution
                val isSupported = when (option) {
                    ResolutionOption.RES_8K -> capabilities.supports8KVideo
                    ResolutionOption.RES_4K -> capabilities.supports4KVideo
                    else -> true
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CameraYellow.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) CameraYellow else Color(0x22FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onVideoResolutionSelected(option) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.label,
                                color = if (isSelected) CameraYellow else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isMaxSupported) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CameraYellow)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("DEVICE MAX", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                }
                            } else if (option == ResolutionOption.RES_4K && !isMaxSupported) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x33FFFFFF))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("RECOMMENDED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        Text(
                            text = "${option.width}×${option.height} · ${option.approxBitrateMbps} Mbps · ~${if (useHevc) (option.storagePerMinuteMb * 0.65f).toInt() else option.storagePerMinuteMb} MB/min",
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onVideoResolutionSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CameraYellow,
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STORAGE & EFFICIENCY MANAGEMENT CARD
            Text(
                text = "STORAGE & EFFICIENCY MANAGEMENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CameraAmber,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SdStorage,
                                contentDescription = null,
                                tint = CameraCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Storage Space",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = storageInfo.availableFormatted,
                            color = CameraCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar representing used storage
                    val usedFraction = if (storageInfo.totalBytes > 0) {
                        ((storageInfo.totalBytes - storageInfo.availableBytes).toFloat() / storageInfo.totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else 0.5f
                    LinearProgressIndicator(
                        progress = { usedFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (storageInfo.isCriticalStorage) Color.Red else if (storageInfo.isLowStorage) Color(0xFFFF9800) else CameraCyan,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Est. Recording Left (${selectedVideoResolution.label}):",
                            color = Color(0xAAFFFFFF),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "~${storageInfo.estimatedTimeFormatted}",
                            color = CameraYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0x22FFFFFF))
                    Spacer(modifier = Modifier.height(14.dp))

                    // HEVC switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Efficient Video Coding (HEVC / H.265)",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Saves ~35-40% file size at high resolution with no quality loss.",
                                color = Color(0x99FFFFFF),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = useHevc,
                            onCheckedChange = onUseHevcChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CameraYellow
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto Storage Protection switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Storage Protection",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Cleanly finalizes recording before storage drops below safety limit.",
                                color = Color(0x99FFFFFF),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoStorageProtection,
                            onCheckedChange = onAutoStorageProtectionChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CameraYellow
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Photo Resolution Picker
            Text(
                text = "PHOTO STILL RESOLUTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CameraAmber,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            ResolutionOption.entries.forEach { option ->
                val isSelected = selectedResolution == option

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CameraYellow.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) CameraYellow else Color(0x22FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onResolutionSelected(option) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.label,
                                color = if (isSelected) CameraYellow else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (option == ResolutionOption.RES_8K && !capabilities.supports8K) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x33FFFFFF))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Upscaled to sensor max", fontSize = 9.sp, color = Color(0xAAFFFFFF))
                                }
                            }
                        }
                        Text(
                            text = option.description,
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onResolutionSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CameraYellow,
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Assistive Tools
            Text(
                text = "ASSISTIVE TOOLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CameraAmber,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Grid toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Grid Lines (3×3 Rule of Thirds)",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = showGrid,
                    onCheckedChange = onShowGridChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CameraYellow
                    )
                )
            }

            // Leveler toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HorizontalRule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Horizon Level Indicator",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = showLeveler,
                    onCheckedChange = onShowLevelerChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CameraYellow
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CameraYellow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Done",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SensorSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xAAFFFFFF), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
