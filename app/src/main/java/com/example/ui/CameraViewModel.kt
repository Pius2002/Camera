package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraHardwareHelper
import com.example.camera.SensorLevelManager
import com.example.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraUiState(
    val currentMode: CameraMode = CameraMode.PHOTO,
    val currentResolution: ResolutionOption = ResolutionOption.RES_4K,
    val currentVideoResolution: ResolutionOption = ResolutionOption.RES_4K,
    val currentAspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val currentFlashMode: FlashModeOption = FlashModeOption.OFF,
    val currentTimer: TimerOption = TimerOption.OFF,
    val zoomRatio: Float = 1.0f,
    val isFacingFront: Boolean = false,
    val proSettings: ProSettings = ProSettings(),
    val pipLayout: PipLayout = PipLayout.FLOATING_RECT,
    val pipSizePreset: PipSizePreset = PipSizePreset.MEDIUM,
    val pipWidthDp: Float = 140f,
    val pipHeightDp: Float = 185f,
    val pipOffsetX: Float = 20f,
    val pipOffsetY: Float = 75f,
    val pipPositionCorner: PipPositionCorner = PipPositionCorner.TOP_RIGHT,
    val isFrontInPip: Boolean = true,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Long = 0L,
    val countdownRemaining: Int = 0,
    val showGrid: Boolean = true,
    val showLeveler: Boolean = true,
    val rollAngle: Float = 0f,
    val isLevel: Boolean = false,
    val tapFocusPoint: Offset? = null,
    val showSettingsSheet: Boolean = false,
    val showGallerySheet: Boolean = false,
    val capturedMediaList: List<CapturedMedia> = emptyList(),
    val selectedGalleryMedia: CapturedMedia? = null,
    val hardwareCapabilities: HardwareCameraCapabilities = HardwareCameraCapabilities(),
    val storageInfo: StorageInfo = StorageInfo(),
    val useHevcCodec: Boolean = true,
    val autoStorageProtection: Boolean = true,
    val statusNotice: String? = null
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val sensorLevelManager = SensorLevelManager(application)
    private var timerJob: Job? = null
    private var focusClearJob: Job? = null

    init {
        loadHardwareCapabilities()
        startSensorMonitoring()
        refreshStorageInfo()
        loadLatestMedia()
    }

    private fun loadLatestMedia() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val media = com.example.camera.GalleryHelper.queryLatestMedia(getApplication())
            if (media.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    capturedMediaList = media,
                    selectedGalleryMedia = media.firstOrNull()
                )
            }
        }
    }

    private fun loadHardwareCapabilities() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val caps = CameraHardwareHelper.queryCapabilities(getApplication(), isFront = false)
            val initialPhotoRes = if (caps.supports8K) ResolutionOption.RES_8K else if (caps.supports4K) ResolutionOption.RES_4K else ResolutionOption.RES_FHD
            val initialVideoRes = caps.maxVideoResolution
            val storage = com.example.camera.StorageHelper.queryStorageInfo(getApplication(), initialVideoRes, isHevc = true)
            _uiState.value = _uiState.value.copy(
                hardwareCapabilities = caps,
                currentResolution = initialPhotoRes,
                currentVideoResolution = initialVideoRes,
                useHevcCodec = caps.supportsHevc,
                storageInfo = storage
            )
        }
    }

    fun refreshStorageInfo() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val state = _uiState.value
            val res = if (state.currentMode == CameraMode.VIDEO || state.currentMode == CameraMode.DUAL) {
                state.currentVideoResolution
            } else {
                state.currentResolution
            }
            val storage = com.example.camera.StorageHelper.queryStorageInfo(
                context = getApplication(),
                resolution = res,
                isHevc = state.useHevcCodec
            )
            _uiState.value = _uiState.value.copy(storageInfo = storage)
        }
    }

    private fun startSensorMonitoring() {
        sensorLevelManager.startListening()
        viewModelScope.launch {
            sensorLevelManager.rollAngle.collect { angle ->
                _uiState.value = _uiState.value.copy(rollAngle = angle)
            }
        }
        viewModelScope.launch {
            sensorLevelManager.isLevel.collect { level ->
                _uiState.value = _uiState.value.copy(isLevel = level)
            }
        }
    }

    fun setMode(mode: CameraMode) {
        val notice = when (mode) {
            CameraMode.VIDEO -> {
                val maxRes = _uiState.value.hardwareCapabilities.maxVideoResolution
                "Video Mode · Max ${maxRes.label} Supported"
            }
            CameraMode.DUAL -> "Dual Mode · Simultaneous Capture"
            CameraMode.PRO -> "Pro Mode · Manual Shutter & ISO"
            CameraMode.PHOTO -> null
        }
        _uiState.value = _uiState.value.copy(
            currentMode = mode,
            statusNotice = notice
        )
        refreshStorageInfo()
        if (notice != null) {
            viewModelScope.launch {
                delay(2500)
                if (_uiState.value.statusNotice == notice) {
                    _uiState.value = _uiState.value.copy(statusNotice = null)
                }
            }
        }
    }

    fun setResolution(resolution: ResolutionOption) {
        _uiState.value = _uiState.value.copy(currentResolution = resolution)
        refreshStorageInfo()
    }

    fun setVideoResolution(resolution: ResolutionOption) {
        _uiState.value = _uiState.value.copy(
            currentVideoResolution = resolution,
            statusNotice = "Video: ${resolution.label} (${resolution.width}×${resolution.height})"
        )
        refreshStorageInfo()
        viewModelScope.launch {
            delay(2000)
            if (_uiState.value.statusNotice?.startsWith("Video:") == true) {
                _uiState.value = _uiState.value.copy(statusNotice = null)
            }
        }
    }

    fun setUseHevcCodec(useHevc: Boolean) {
        _uiState.value = _uiState.value.copy(useHevcCodec = useHevc)
        refreshStorageInfo()
    }

    fun setAutoStorageProtection(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoStorageProtection = enabled)
    }

    fun setAspectRatio(ratio: AspectRatioOption) {
        _uiState.value = _uiState.value.copy(currentAspectRatio = ratio)
    }

    fun setFlashMode(flash: FlashModeOption) {
        val notice = if (_uiState.value.isFacingFront) {
            "Rear Flash: ${flash.label} (Set for rear camera)"
        } else {
            "Flash: ${flash.label}"
        }
        _uiState.value = _uiState.value.copy(
            currentFlashMode = flash,
            statusNotice = notice
        )
        viewModelScope.launch {
            delay(1800)
            if (_uiState.value.statusNotice == notice) {
                _uiState.value = _uiState.value.copy(statusNotice = null)
            }
        }
    }

    fun cycleFlashMode() {
        val next = when (_uiState.value.currentFlashMode) {
            FlashModeOption.AUTO -> FlashModeOption.ON
            FlashModeOption.ON -> FlashModeOption.OFF
            else -> FlashModeOption.AUTO
        }
        setFlashMode(next)
    }

    fun setTimer(timer: TimerOption) {
        _uiState.value = _uiState.value.copy(currentTimer = timer)
    }

    fun setZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoomRatio = zoom)
    }

    fun toggleCameraFacing() {
        _uiState.value = _uiState.value.copy(isFacingFront = !_uiState.value.isFacingFront)
    }

    fun updateProSettings(settings: ProSettings) {
        _uiState.value = _uiState.value.copy(proSettings = settings)
    }

    fun setPipLayout(layout: PipLayout) {
        _uiState.value = _uiState.value.copy(pipLayout = layout)
    }

    fun setPipSizePreset(preset: PipSizePreset) {
        val isCircle = _uiState.value.pipLayout == PipLayout.FLOATING_ROUND
        val width = preset.widthDp
        val height = if (isCircle) preset.widthDp else preset.heightDp
        _uiState.value = _uiState.value.copy(
            pipSizePreset = preset,
            pipWidthDp = width,
            pipHeightDp = height
        )
    }

    fun setPipCustomDimensions(widthDp: Float, heightDp: Float) {
        val clampedW = widthDp.coerceIn(80f, 260f)
        val clampedH = heightDp.coerceIn(100f, 350f)
        _uiState.value = _uiState.value.copy(
            pipWidthDp = clampedW,
            pipHeightDp = clampedH
        )
    }

    fun setPipOffset(x: Float, y: Float) {
        _uiState.value = _uiState.value.copy(
            pipOffsetX = x,
            pipOffsetY = y,
            pipPositionCorner = PipPositionCorner.FREE
        )
    }

    fun snapPipToCorner(corner: PipPositionCorner, screenWidthDp: Float, screenHeightDp: Float) {
        val currentW = _uiState.value.pipWidthDp
        val currentH = if (_uiState.value.pipLayout == PipLayout.FLOATING_ROUND) currentW else _uiState.value.pipHeightDp
        val marginHorizontal = 16f
        val marginTop = 70f
        val marginBottom = 155f // avoid bottom mode selector & shutter

        val (targetX, targetY) = when (corner) {
            PipPositionCorner.TOP_LEFT -> Pair(marginHorizontal, marginTop)
            PipPositionCorner.TOP_RIGHT -> Pair((screenWidthDp - currentW - marginHorizontal).coerceAtLeast(0f), marginTop)
            PipPositionCorner.BOTTOM_LEFT -> Pair(marginHorizontal, (screenHeightDp - currentH - marginBottom).coerceAtLeast(marginTop))
            PipPositionCorner.BOTTOM_RIGHT -> Pair((screenWidthDp - currentW - marginHorizontal).coerceAtLeast(0f), (screenHeightDp - currentH - marginBottom).coerceAtLeast(marginTop))
            PipPositionCorner.FREE -> Pair(_uiState.value.pipOffsetX, _uiState.value.pipOffsetY)
        }

        _uiState.value = _uiState.value.copy(
            pipOffsetX = targetX,
            pipOffsetY = targetY,
            pipPositionCorner = corner
        )
    }

    fun swapDualCameras() {
        _uiState.value = _uiState.value.copy(isFrontInPip = !_uiState.value.isFrontInPip)
    }

    fun setRecording(isRecording: Boolean) {
        _uiState.value = _uiState.value.copy(
            isRecording = isRecording,
            recordingDurationSeconds = if (!isRecording) 0L else _uiState.value.recordingDurationSeconds
        )
    }

    fun updateRecordingDuration(durationSeconds: Long) {
        _uiState.value = _uiState.value.copy(recordingDurationSeconds = durationSeconds)
    }

    fun triggerTapToFocus(offset: Offset) {
        focusClearJob?.cancel()
        _uiState.value = _uiState.value.copy(tapFocusPoint = offset)
        focusClearJob = viewModelScope.launch {
            delay(2500)
            _uiState.value = _uiState.value.copy(tapFocusPoint = null)
        }
    }

    fun setShowGrid(show: Boolean) {
        _uiState.value = _uiState.value.copy(showGrid = show)
    }

    fun setShowLeveler(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLeveler = show)
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSettingsSheet = show)
    }

    fun setShowGallerySheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showGallerySheet = show)
    }

    fun setSelectedGalleryMedia(media: CapturedMedia?) {
        _uiState.value = _uiState.value.copy(selectedGalleryMedia = media)
    }

    fun addCapturedMedia(media: CapturedMedia) {
        val updated = listOf(media) + _uiState.value.capturedMediaList
        _uiState.value = _uiState.value.copy(
            capturedMediaList = updated,
            selectedGalleryMedia = media,
            statusNotice = if (media.type == MediaType.PHOTO) "Saved High-Res Photo" else "Saved Video"
        )
        viewModelScope.launch {
            delay(2000)
            _uiState.value = _uiState.value.copy(statusNotice = null)
        }
    }

    fun deleteMedia(media: CapturedMedia) {
        val updated = _uiState.value.capturedMediaList.filter { it.uri != media.uri }
        _uiState.value = _uiState.value.copy(
            capturedMediaList = updated,
            selectedGalleryMedia = updated.firstOrNull()
        )
    }

    fun runCountdownIfNeeded(onFinished: () -> Unit) {
        val seconds = _uiState.value.currentTimer.seconds
        if (seconds <= 0) {
            onFinished()
            return
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _uiState.value = _uiState.value.copy(countdownRemaining = i)
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(countdownRemaining = 0)
            onFinished()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorLevelManager.stopListening()
    }
}
