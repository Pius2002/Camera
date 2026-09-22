package com.example.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.PipLayout
import com.example.data.model.PipSizePreset
import com.example.ui.theme.CameraCyan
import com.example.ui.theme.CameraDarkGrey
import com.example.ui.theme.CameraRed
import com.example.ui.theme.CameraYellow
import kotlin.math.roundToInt

@Composable
fun DualCameraPipView(
    secondaryPreviewView: PreviewView,
    pipLayout: PipLayout,
    onPipLayoutChanged: (PipLayout) -> Unit,
    pipWidthDp: Float,
    pipHeightDp: Float,
    onDimensionsChanged: (Float, Float) -> Unit,
    offsetX: Float,
    offsetY: Float,
    onOffsetChanged: (Float, Float) -> Unit,
    onSwapCameras: () -> Unit,
    isFrontInPip: Boolean,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    if (pipLayout == PipLayout.SPLIT_SCREEN) {
        // Split-Screen mode: Top half or bottom half
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .border(2.dp, if (isRecording) CameraRed else CameraCyan, RoundedCornerShape(0.dp))
                .clip(RoundedCornerShape(0.dp))
                .testTag("dual_camera_split_view")
        ) {
            AndroidView(
                factory = { secondaryPreviewView },
                modifier = Modifier.fillMaxSize()
            )

            // Front/Rear Camera Reticle & Tag
            ReticleOverlay(
                label = if (isFrontInPip) "FRONT (SELFIE) FEED" else "REAR CAMERA FEED",
                isFront = isFrontInPip,
                isRecording = isRecording
            )

            // Header badge with swap and layout toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dual Rec Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xBB000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) CameraRed else CameraCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFrontInPip) "FRONT (SELFIE)" else "REAR CAM",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onSwapCameras,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x88000000))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FlipCameraAndroid,
                            contentDescription = "Swap Cameras",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onPipLayoutChanged(PipLayout.FLOATING_RECT) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x88000000))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = "Change to Floating PIP",
                            tint = CameraYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Floating PiP Mode (Draggable & Resizable)
        val isCircle = pipLayout == PipLayout.FLOATING_ROUND
        val activeWidth = pipWidthDp.dp
        val activeHeight = if (isCircle) pipWidthDp.dp else pipHeightDp.dp
        val shape = if (isCircle) CircleShape else RoundedCornerShape(18.dp)

        // Pulsing border animation during recording
        val infiniteTransition = rememberInfiniteTransition(label = "pip_rec_pulse")
        val borderColor by infiniteTransition.animateColor(
            initialValue = if (isRecording) CameraRed else CameraCyan,
            targetValue = if (isRecording) CameraYellow else CameraCyan.copy(alpha = 0.7f),
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rec_border"
        )

        Box(
            modifier = modifier
                .offset {
                    IntOffset(
                        with(density) { offsetX.dp.roundToPx() },
                        with(density) { offsetY.dp.roundToPx() }
                    )
                }
                .size(activeWidth, activeHeight)
                .shadow(16.dp, shape)
                .clip(shape)
                .border(2.5.dp, borderColor, shape)
                .background(CameraDarkGrey)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (offsetX + dragAmount.x / density.density)
                            .coerceIn(8f, (screenWidth - pipWidthDp - 8f).coerceAtLeast(8f))
                        val newY = (offsetY + dragAmount.y / density.density)
                            .coerceIn(60f, (screenHeight - (if (isCircle) pipWidthDp else pipHeightDp) - 130f).coerceAtLeast(60f))
                        onOffsetChanged(newX, newY)
                    }
                }
                .testTag("dual_camera_pip")
        ) {
            // Camera Preview in PiP
            AndroidView(
                factory = { secondaryPreviewView },
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Reticle overlay
            ReticleOverlay(
                label = if (isFrontInPip) "FRONT" else "REAR",
                isFront = isFrontInPip,
                isRecording = isRecording
            )

            // Header Overlay with source tag and quick swap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Pip Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) CameraRed else CameraCyan)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFrontInPip) "FRONT" else "REAR",
                                color = if (isRecording) CameraRed else CameraCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Swap Button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xBB000000))
                            .border(1.dp, Color(0x55FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onSwapCameras,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FlipCameraAndroid,
                                contentDescription = "Swap Main/Pip",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Bottom-Right Corner Resize Drag Handle (Only in Rect mode)
            if (!isCircle) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newW = (pipWidthDp + dragAmount.x / density.density)
                                    .coerceIn(90f, (screenWidth - 20f).coerceAtMost(250f))
                                val newH = (pipHeightDp + dragAmount.y / density.density)
                                    .coerceIn(120f, (screenHeight - 160f).coerceAtMost(340f))
                                onDimensionsChanged(newW, newH)
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val stroke = 2.dp.toPx()
                        // Draw diagonal corner resize notches
                        drawLine(
                            color = CameraCyan,
                            start = Offset(size.width * 0.4f, size.height),
                            end = Offset(size.width, size.height * 0.4f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = CameraCyan,
                            start = Offset(size.width * 0.7f, size.height),
                            end = Offset(size.width, size.height * 0.7f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Bottom-Left Shape Toggle Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                IconButton(
                    onClick = {
                        val next = when (pipLayout) {
                            PipLayout.FLOATING_RECT -> PipLayout.FLOATING_ROUND
                            PipLayout.FLOATING_ROUND -> PipLayout.SPLIT_SCREEN
                            PipLayout.SPLIT_SCREEN -> PipLayout.FLOATING_RECT
                        }
                        onPipLayoutChanged(next)
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000))
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = "Toggle Shape",
                        tint = CameraYellow,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReticleOverlay(
    label: String,
    isFront: Boolean,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bracketLen = 12.dp.toPx()
            val stroke = 1.5.dp.toPx()
            val margin = 14.dp.toPx()
            val color = if (isRecording) Color(0x66FF3B30) else Color(0x5500E5FF)

            // Top-left bracket
            drawLine(color, Offset(margin, margin), Offset(margin + bracketLen, margin), stroke)
            drawLine(color, Offset(margin, margin), Offset(margin, margin + bracketLen), stroke)

            // Top-right bracket
            drawLine(color, Offset(size.width - margin, margin), Offset(size.width - margin - bracketLen, margin), stroke)
            drawLine(color, Offset(size.width - margin, margin), Offset(size.width - margin, margin + bracketLen), stroke)

            // Bottom-left bracket
            drawLine(color, Offset(margin, size.height - margin), Offset(margin + bracketLen, size.height - margin), stroke)
            drawLine(color, Offset(margin, size.height - margin), Offset(margin, size.height - margin - bracketLen), stroke)

            // Bottom-right bracket
            drawLine(color, Offset(size.width - margin, size.height - margin), Offset(size.width - margin - bracketLen, size.height - margin), stroke)
            drawLine(color, Offset(size.width - margin, size.height - margin), Offset(size.width - margin, size.height - margin - bracketLen), stroke)

            // Subtle face framing circle in center if front camera
            if (isFront) {
                val radius = (size.minDimension * 0.22f)
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(size.width / 2f, size.height / 2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}
