package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.PipLayout
import com.example.data.model.PipPositionCorner
import com.example.data.model.PipSizePreset
import com.example.ui.theme.CameraCyan
import com.example.ui.theme.CameraYellow

@Composable
fun DualControlsBar(
    pipLayout: PipLayout,
    onPipLayoutChanged: (PipLayout) -> Unit,
    pipSizePreset: PipSizePreset,
    onPipSizeChanged: (PipSizePreset) -> Unit,
    pipPositionCorner: PipPositionCorner,
    onSnapCorner: (PipPositionCorner) -> Unit,
    onSwapCameras: () -> Unit,
    isFrontInPip: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xDD101318))
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .testTag("dual_controls_bar")
    ) {
        // Section 1: Quick Row for PIP Size & Position Snap
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PIP Size Section Title
            Text(
                text = "SIZE:",
                color = CameraCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, end = 2.dp)
            )

            // Size presets S, M, L, XL
            PipSizePreset.entries.forEach { sizePreset ->
                val isSelected = pipSizePreset == sizePreset && pipLayout != PipLayout.SPLIT_SCREEN
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CameraCyan else Color(0x33FFFFFF))
                        .clickable { onPipSizeChanged(sizePreset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("pip_size_${sizePreset.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (sizePreset) {
                            PipSizePreset.SMALL -> "S"
                            PipSizePreset.MEDIUM -> "M"
                            PipSizePreset.LARGE -> "L"
                            PipSizePreset.XLARGE -> "XL"
                        },
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0x44FFFFFF))
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Position Section Title
            Text(
                text = "POSITION:",
                color = CameraYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            // Corner Snap Buttons: TL, TR, BL, BR
            val corners = listOf(
                PipPositionCorner.TOP_LEFT to "⌜ TL",
                PipPositionCorner.TOP_RIGHT to "⌝ TR",
                PipPositionCorner.BOTTOM_LEFT to "⌞ BL",
                PipPositionCorner.BOTTOM_RIGHT to "⌟ BR"
            )

            corners.forEach { (corner, label) ->
                val isSelected = pipPositionCorner == corner && pipLayout != PipLayout.SPLIT_SCREEN
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CameraYellow else Color(0x33FFFFFF))
                        .clickable { onSnapCorner(corner) }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("pip_corner_${corner.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0x44FFFFFF))
            )
            Spacer(modifier = Modifier.width(6.dp))

            // PIP Shape toggle: Rect, Circle, Split
            PipLayout.entries.forEach { layout ->
                val isSelected = pipLayout == layout
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color.White else Color(0x33FFFFFF))
                        .clickable { onPipLayoutChanged(layout) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("pip_layout_${layout.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (layout) {
                                PipLayout.FLOATING_RECT -> Icons.Filled.CropSquare
                                PipLayout.FLOATING_ROUND -> Icons.Filled.Circle
                                PipLayout.SPLIT_SCREEN -> Icons.Filled.Splitscreen
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (layout) {
                                PipLayout.FLOATING_RECT -> "Rect"
                                PipLayout.FLOATING_ROUND -> "Circle"
                                PipLayout.SPLIT_SCREEN -> "Split"
                            },
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Swap Cameras Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x3300E5FF))
                    .border(1.dp, CameraCyan, RoundedCornerShape(10.dp))
                    .clickable { onSwapCameras() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("dual_swap_cameras_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FlipCameraAndroid,
                        contentDescription = "Swap Cameras",
                        tint = CameraCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFrontInPip) "Selfie in PIP" else "Rear in PIP",
                        color = CameraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
