package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CameraMode
import com.example.ui.theme.CameraRed
import com.example.ui.theme.CameraYellow

@Composable
fun CameraBottomBar(
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    isRecording: Boolean,
    recordingDurationSeconds: Long,
    latestThumbnailUri: Uri?,
    onShutterClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        CameraMode.PRO,
        CameraMode.PHOTO,
        CameraMode.VIDEO,
        CameraMode.DUAL
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xEE08090C))
            .padding(bottom = 20.dp, top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recording Timer Indicator (When Video or Dual is recording)
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
            val blinkAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "blink"
            )

            val minutes = recordingDurationSeconds / 60
            val seconds = recordingDurationSeconds % 60
            val timeFormatted = String.format("%02d:%02d", minutes, seconds)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer(alpha = blinkAlpha)
                        .clip(CircleShape)
                        .background(CameraRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REC $timeFormatted",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mode Carousel Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEach { mode ->
                val isSelected = currentMode == mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            enabled = !isRecording,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onModeSelected(mode) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("mode_${mode.title}")
                ) {
                    Text(
                        text = mode.title,
                        color = if (isSelected) CameraYellow else Color(0x88FFFFFF),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(CameraYellow)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shutter Row: Gallery | Shutter Button | Lens Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Gallery Thumbnail (Tap opens default device gallery app)
            val galleryInteractionSource = remember { MutableInteractionSource() }
            val isGalleryPressed by galleryInteractionSource.collectIsPressedAsState()
            val galleryScale by animateFloatAsState(
                targetValue = if (isGalleryPressed) 0.88f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "gallery_scale"
            )

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .scale(galleryScale)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .border(2.dp, if (latestThumbnailUri != null) CameraYellow else Color(0x44FFFFFF), CircleShape)
                    .clickable(
                        interactionSource = galleryInteractionSource,
                        indication = null
                    ) { onGalleryClick() }
                    .testTag("gallery_thumbnail_button"),
                contentAlignment = Alignment.Center
            ) {
                if (latestThumbnailUri != null) {
                    AsyncImage(
                        model = latestThumbnailUri,
                        contentDescription = "Open last capture in default gallery app",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gallery launch badge overlay in the corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = null,
                            tint = CameraYellow,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Open default gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center: Iconic Shutter / Record Button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "shutter_scale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(4.dp, Color.White, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onShutterClick() }
                    .testTag("shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                when (currentMode) {
                    CameraMode.PHOTO, CameraMode.PRO -> {
                        // Crisp white inner circle for photo
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                    CameraMode.VIDEO, CameraMode.DUAL -> {
                        if (isRecording) {
                            // Morph to red square when recording
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CameraRed)
                            )
                        } else {
                            // Red recording circle
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(CameraRed)
                            )
                        }
                    }
                }
            }

            // Right: Flip Camera Button
            var flipAngle by remember { mutableFloatStateOf(0f) }
            val animatedFlip by animateFloatAsState(
                targetValue = flipAngle,
                animationSpec = tween(350, easing = FastOutSlowInEasing),
                label = "flip_camera"
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .clickable(enabled = !isRecording) {
                        flipAngle += 180f
                        onSwitchCameraClick()
                    }
                    .testTag("switch_camera_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FlipCameraAndroid,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer(rotationZ = animatedFlip)
                )
            }
        }
    }
}
