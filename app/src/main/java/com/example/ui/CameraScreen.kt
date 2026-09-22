package com.example.ui

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.CameraXManager
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.CameraDarkGrey
import com.example.ui.theme.CameraYellow

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Preview views for CameraX
    val primaryPreviewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val secondaryPreviewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val cameraManager = remember {
        CameraXManager(context, lifecycleOwner)
    }

    // Initialize CameraX
    LaunchedEffect(Unit) {
        cameraManager.initialize(
            primaryView = primaryPreviewView,
            secondaryView = secondaryPreviewView
        )
    }

    val isVideoMode = uiState.currentMode == CameraMode.VIDEO || uiState.currentMode == CameraMode.DUAL
    val activeResolution = if (isVideoMode) uiState.currentVideoResolution else uiState.currentResolution

    // React to camera mode changes
    LaunchedEffect(uiState.currentMode) {
        cameraManager.setMode(uiState.currentMode)
    }

    // React to resolution changes
    LaunchedEffect(activeResolution) {
        cameraManager.setResolution(activeResolution)
    }

    // React to lens facing changes
    LaunchedEffect(uiState.isFacingFront) {
        cameraManager.switchLens(uiState.isFacingFront)
    }

    // React to flash changes
    LaunchedEffect(uiState.currentFlashMode) {
        cameraManager.setFlashMode(uiState.currentFlashMode)
    }

    // React to zoom changes
    LaunchedEffect(uiState.zoomRatio) {
        cameraManager.setZoom(uiState.zoomRatio)
    }

    // React to pro settings changes
    LaunchedEffect(uiState.proSettings, uiState.currentMode) {
        if (uiState.currentMode == CameraMode.PRO) {
            cameraManager.applyProSettings(uiState.proSettings)
        }
    }

    // Main shutter action
    val onShutterClick: () -> Unit = {
        when (uiState.currentMode) {
            CameraMode.PHOTO, CameraMode.PRO -> {
                viewModel.runCountdownIfNeeded {
                    cameraManager.capturePhoto(
                        onSuccess = { uri ->
                            val media = CapturedMedia(
                                uri = uri,
                                type = MediaType.PHOTO,
                                title = "Photo",
                                timestamp = System.currentTimeMillis(),
                                resolution = uiState.currentResolution.label
                            )
                            viewModel.addCapturedMedia(media)
                        },
                        onError = { err ->
                            Toast.makeText(context, "Capture error: ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            CameraMode.VIDEO, CameraMode.DUAL -> {
                if (uiState.isRecording) {
                    cameraManager.stopRecording()
                    viewModel.setRecording(false)
                } else {
                    viewModel.runCountdownIfNeeded {
                        cameraManager.startRecording(
                            onStart = {
                                viewModel.setRecording(true)
                            },
                            onDurationUpdate = { seconds ->
                                viewModel.updateRecordingDuration(seconds)
                            },
                            onSuccess = { uri ->
                                viewModel.setRecording(false)
                                val media = CapturedMedia(
                                    uri = uri,
                                    type = MediaType.VIDEO,
                                    title = if (uiState.currentMode == CameraMode.DUAL) "Dual PiP Video" else "Video",
                                    timestamp = System.currentTimeMillis(),
                                    resolution = uiState.currentVideoResolution.label
                                )
                                viewModel.addCapturedMedia(media)
                            },
                            onError = { err ->
                                viewModel.setRecording(false)
                                Toast.makeText(context, "Recording failed: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Primary Camera Viewfinder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        cameraManager.focusOnPoint(offset.x, offset.y)
                        viewModel.triggerTapToFocus(offset)
                    }
                }
        ) {
            AndroidView(
                factory = { primaryPreviewView },
                modifier = Modifier.fillMaxSize()
            )

            // Overlays: Grid lines
            if (uiState.showGrid) {
                GridOverlay()
            }

            // Overlays: Horizon Leveler
            if (uiState.showLeveler) {
                LevelIndicatorOverlay(
                    rollAngle = uiState.rollAngle,
                    isLevel = uiState.isLevel
                )
            }

            // Overlays: Tap-to-Focus Indicator Ring
            uiState.tapFocusPoint?.let { point ->
                TapToFocusIndicator(x = point.x, y = point.y)
            }

            // Overlays: Timer countdown
            if (uiState.countdownRemaining > 0) {
                TimerCountdownOverlay(secondsRemaining = uiState.countdownRemaining)
            }
        }

        // 2. Dual Camera Picture-in-Picture View (in DUAL mode)
        if (uiState.currentMode == CameraMode.DUAL) {
            DualCameraPipView(
                secondaryPreviewView = secondaryPreviewView,
                pipLayout = uiState.pipLayout,
                onPipLayoutChanged = { layout -> viewModel.setPipLayout(layout) },
                pipWidthDp = uiState.pipWidthDp,
                pipHeightDp = uiState.pipHeightDp,
                onDimensionsChanged = { w, h -> viewModel.setPipCustomDimensions(w, h) },
                offsetX = uiState.pipOffsetX,
                offsetY = uiState.pipOffsetY,
                onOffsetChanged = { x, y -> viewModel.setPipOffset(x, y) },
                onSwapCameras = {
                    viewModel.swapDualCameras()
                    viewModel.toggleCameraFacing()
                },
                isFrontInPip = uiState.isFrontInPip,
                isRecording = uiState.isRecording,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Dual PIP Size & Position Controls
            if (!uiState.isRecording) {
                DualControlsBar(
                    pipLayout = uiState.pipLayout,
                    onPipLayoutChanged = { viewModel.setPipLayout(it) },
                    pipSizePreset = uiState.pipSizePreset,
                    onPipSizeChanged = { viewModel.setPipSizePreset(it) },
                    pipPositionCorner = uiState.pipPositionCorner,
                    onSnapCorner = { corner ->
                        viewModel.snapPipToCorner(
                            corner,
                            configuration.screenWidthDp.toFloat(),
                            configuration.screenHeightDp.toFloat()
                        )
                    },
                    onSwapCameras = {
                        viewModel.swapDualCameras()
                        viewModel.toggleCameraFacing()
                    },
                    isFrontInPip = uiState.isFrontInPip,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                )
            }
        }

        // 3. Status Notification Pill
        uiState.statusNotice?.let { notice ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CameraYellow)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = notice,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Top Quick Controls Bar (Flash, Aspect, 8K/4K Resolution, Timer, Settings)
        CameraTopBar(
            currentFlash = uiState.currentFlashMode,
            onFlashChanged = { viewModel.setFlashMode(it) },
            currentAspectRatio = uiState.currentAspectRatio,
            onAspectRatioChanged = { viewModel.setAspectRatio(it) },
            currentResolution = activeResolution,
            onResolutionChanged = {
                if (isVideoMode) {
                    viewModel.setVideoResolution(it)
                } else {
                    viewModel.setResolution(it)
                }
            },
            currentTimer = uiState.currentTimer,
            onTimerChanged = { viewModel.setTimer(it) },
            onOpenSettings = { viewModel.setShowSettingsSheet(true) },
            isVideoMode = isVideoMode,
            maxVideoResolution = uiState.hardwareCapabilities.maxVideoResolution,
            storageInfo = uiState.storageInfo,
            isFacingFront = uiState.isFacingFront,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // 5. Center-Bottom Zoom Controls (When not in recording, dual, or manual focus)
        if (!uiState.isRecording && uiState.currentMode != CameraMode.DUAL) {
            ZoomControls(
                currentZoom = uiState.zoomRatio,
                onZoomSelected = { zoom -> viewModel.setZoom(zoom) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (uiState.currentMode == CameraMode.PRO) 190.dp else 140.dp)
            )
        }

        // 6. Pro Controls Bar (when in PRO mode)
        if (uiState.currentMode == CameraMode.PRO) {
            ProControlsBar(
                settings = uiState.proSettings,
                onSettingsChanged = { viewModel.updateProSettings(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 124.dp)
            )
        }

        // 7. Bottom Shutter Bar (Gallery Thumbnail | Shutter/Rec Button | Flip Lens)
        CameraBottomBar(
            currentMode = uiState.currentMode,
            onModeSelected = { viewModel.setMode(it) },
            isRecording = uiState.isRecording,
            recordingDurationSeconds = uiState.recordingDurationSeconds,
            latestThumbnailUri = uiState.capturedMediaList.firstOrNull()?.uri,
            onShutterClick = onShutterClick,
            onSwitchCameraClick = { viewModel.toggleCameraFacing() },
            onGalleryClick = {
                val latest = uiState.capturedMediaList.firstOrNull()
                com.example.camera.GalleryHelper.openDefaultGallery(
                    context = context,
                    latestMedia = latest,
                    onFallbackToInternal = {
                        viewModel.setShowGallerySheet(true)
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )

        // Settings Sheet Modal
        if (uiState.showSettingsSheet) {
            SettingsSheet(
                capabilities = uiState.hardwareCapabilities,
                selectedResolution = uiState.currentResolution,
                onResolutionSelected = { viewModel.setResolution(it) },
                selectedVideoResolution = uiState.currentVideoResolution,
                onVideoResolutionSelected = { viewModel.setVideoResolution(it) },
                storageInfo = uiState.storageInfo,
                useHevc = uiState.useHevcCodec,
                onUseHevcChanged = { viewModel.setUseHevcCodec(it) },
                autoStorageProtection = uiState.autoStorageProtection,
                onAutoStorageProtectionChanged = { viewModel.setAutoStorageProtection(it) },
                showGrid = uiState.showGrid,
                onShowGridChanged = { viewModel.setShowGrid(it) },
                showLeveler = uiState.showLeveler,
                onShowLevelerChanged = { viewModel.setShowLeveler(it) },
                currentFlash = uiState.currentFlashMode,
                onFlashChanged = { viewModel.setFlashMode(it) },
                onDismiss = { viewModel.setShowSettingsSheet(false) }
            )
        }

        // Gallery Sheet Modal
        if (uiState.showGallerySheet) {
            GallerySheet(
                mediaList = uiState.capturedMediaList,
                selectedMedia = uiState.selectedGalleryMedia ?: uiState.capturedMediaList.firstOrNull(),
                onMediaSelected = { viewModel.setSelectedGalleryMedia(it) },
                onDeleteMedia = { viewModel.deleteMedia(it) },
                onDismiss = { viewModel.setShowGallerySheet(false) }
            )
        }
    }
}
