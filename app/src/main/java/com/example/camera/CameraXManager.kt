package com.example.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.data.model.CameraMode
import com.example.data.model.FlashModeOption
import com.example.data.model.ProSettings
import com.example.data.model.ResolutionOption
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var primaryCamera: Camera? = null
    private var secondaryCamera: Camera? = null

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private var currentLensFacing = CameraSelector.LENS_FACING_BACK
    private var currentMode = CameraMode.PHOTO
    private var currentResolution = ResolutionOption.RES_4K
    private var currentFlashMode = FlashModeOption.OFF

    private var primaryPreviewView: PreviewView? = null
    private var secondaryPreviewView: PreviewView? = null
    var isDualHardwareActive: Boolean = false
        private set

    fun initialize(
        primaryView: PreviewView,
        secondaryView: PreviewView? = null,
        onInitialized: () -> Unit = {}
    ) {
        this.primaryPreviewView = primaryView
        this.secondaryPreviewView = secondaryView

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                onInitialized()
            } catch (e: Exception) {
                Log.e("CameraXManager", "Failed to initialize CameraProvider", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setMode(mode: CameraMode) {
        if (currentMode != mode) {
            currentMode = mode
            bindCameraUseCases()
        }
    }

    fun setResolution(resolution: ResolutionOption) {
        if (currentResolution != resolution) {
            currentResolution = resolution
            bindCameraUseCases()
        }
    }

    fun switchLens(isFront: Boolean) {
        val targetFacing = if (isFront) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        if (currentLensFacing != targetFacing) {
            currentLensFacing = targetFacing
            bindCameraUseCases()
        }
    }

    fun isFacingFront(): Boolean = currentLensFacing == CameraSelector.LENS_FACING_FRONT

    fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        val primaryView = primaryPreviewView ?: return

        try {
            provider.unbindAll()

            val primarySelector = CameraSelector.Builder()
                .requireLensFacing(currentLensFacing)
                .build()

            // 1. Preview Use Case (Smooth performance: 1080p target for video preview, full sensor for photo)
            val previewBuilder = Preview.Builder()
            val previewTargetSize = if (currentMode == CameraMode.VIDEO || currentMode == CameraMode.DUAL) {
                Size(1920, 1080)
            } else {
                Size(currentResolution.width, currentResolution.height)
            }
            val resolutionStrategy = ResolutionStrategy(
                previewTargetSize,
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
            )
            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(resolutionStrategy)
                .build()
            previewBuilder.setResolutionSelector(resolutionSelector)

            preview = previewBuilder.build().also {
                it.surfaceProvider = primaryView.surfaceProvider
            }

            // 2. Image Capture Use Case (Up to phone's 4K/8K max)
            val captureBuilder = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setResolutionSelector(resolutionSelector)

            val flash = when (currentFlashMode) {
                FlashModeOption.ON -> ImageCapture.FLASH_MODE_ON
                FlashModeOption.AUTO -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            captureBuilder.setFlashMode(flash)
            imageCapture = captureBuilder.build()

            // 3. Video Capture Use Case (Supports 8K Highest, 4K UHD, FHD, HD)
            val videoQualitySelector = when (currentResolution) {
                ResolutionOption.RES_8K -> QualitySelector.from(
                    Quality.HIGHEST,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.UHD)
                )
                ResolutionOption.RES_4K -> QualitySelector.from(
                    Quality.UHD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD)
                )
                ResolutionOption.RES_FHD -> QualitySelector.from(
                    Quality.FHD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.HD)
                )
                ResolutionOption.RES_HD -> QualitySelector.from(
                    Quality.HD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            }
            val recorder = Recorder.Builder()
                .setQualitySelector(videoQualitySelector)
                .setExecutor(cameraExecutor)
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Bind primary camera
            when (currentMode) {
                CameraMode.PHOTO, CameraMode.PRO -> {
                    primaryCamera = provider.bindToLifecycle(
                        lifecycleOwner,
                        primarySelector,
                        preview,
                        imageCapture
                    )
                }
                CameraMode.VIDEO -> {
                    primaryCamera = provider.bindToLifecycle(
                        lifecycleOwner,
                        primarySelector,
                        preview,
                        videoCapture
                    )
                }
                CameraMode.DUAL -> {
                    // Try concurrent binding if available, otherwise bind primary video
                    bindDualCamera(provider, primarySelector, primaryView)
                }
            }

            // Apply torch if enabled
            if (currentFlashMode == FlashModeOption.TORCH) {
                primaryCamera?.cameraControl?.enableTorch(true)
            } else {
                primaryCamera?.cameraControl?.enableTorch(false)
            }

        } catch (e: Exception) {
            Log.e("CameraXManager", "Use case binding failed", e)
        }
    }

    private fun bindDualCamera(
        provider: ProcessCameraProvider,
        primarySelector: CameraSelector,
        primaryView: PreviewView
    ) {
        val secondaryView = secondaryPreviewView
        val secondaryFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        val secondarySelector = CameraSelector.Builder()
            .requireLensFacing(secondaryFacing)
            .build()

        isDualHardwareActive = false

        // Check if device supports concurrent camera binding via CameraX
        val hasConcurrentSupport = try {
            provider.availableConcurrentCameraInfos.isNotEmpty()
        } catch (e: Exception) {
            false
        }

        if (hasConcurrentSupport && secondaryView != null && preview != null && videoCapture != null) {
            try {
                val secondaryPreview = Preview.Builder().build().also {
                    it.surfaceProvider = secondaryView.surfaceProvider
                }

                val primaryGroup = UseCaseGroup.Builder()
                    .addUseCase(preview!!)
                    .addUseCase(videoCapture!!)
                    .build()

                val secondaryGroup = UseCaseGroup.Builder()
                    .addUseCase(secondaryPreview)
                    .build()

                val primaryConfig = ConcurrentCamera.SingleCameraConfig(
                    primarySelector,
                    primaryGroup,
                    lifecycleOwner
                )

                val secondaryConfig = ConcurrentCamera.SingleCameraConfig(
                    secondarySelector,
                    secondaryGroup,
                    lifecycleOwner
                )

                val concurrentCamera = provider.bindToLifecycle(listOf(primaryConfig, secondaryConfig))
                primaryCamera = concurrentCamera.cameras.firstOrNull()
                secondaryCamera = concurrentCamera.cameras.getOrNull(1)
                isDualHardwareActive = true
                return
            } catch (e: Exception) {
                Log.w("CameraXManager", "Concurrent camera binding failed, falling back", e)
            }
        }

        // Fallback: Bind primary camera for video and preview
        try {
            primaryCamera = provider.bindToLifecycle(
                lifecycleOwner,
                primarySelector,
                preview,
                videoCapture
            )

            // Try secondary preview if possible
            if (secondaryView != null) {
                try {
                    val secondaryPreview = Preview.Builder().build().also {
                        it.surfaceProvider = secondaryView.surfaceProvider
                    }
                    secondaryCamera = provider.bindToLifecycle(
                        lifecycleOwner,
                        secondarySelector,
                        secondaryPreview
                    )
                    isDualHardwareActive = true
                } catch (e: Exception) {
                    Log.w("CameraXManager", "Secondary camera preview not supported concurrently by HAL", e)
                    isDualHardwareActive = false
                }
            }
        } catch (e: Exception) {
            Log.e("CameraXManager", "Failed to bind dual cameras", e)
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun applyProSettings(settings: ProSettings) {
        val camera = primaryCamera ?: return
        val cameraControl = camera.cameraControl
        val camera2Control = Camera2CameraControl.from(cameraControl)

        val optionsBuilder = CaptureRequestOptions.Builder()

        // Exposure / ISO & Shutter Speed
        if (settings.isIsoManual || settings.isShutterManual) {
            optionsBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )
            if (settings.isIsoManual) {
                optionsBuilder.setCaptureRequestOption(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    settings.iso
                )
            }
            if (settings.isShutterManual) {
                optionsBuilder.setCaptureRequestOption(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    settings.shutterNanos
                )
            }
        } else {
            optionsBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )
        }

        // Manual Focus
        if (settings.isFocusManual) {
            optionsBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )
            optionsBuilder.setCaptureRequestOption(
                CaptureRequest.LENS_FOCUS_DISTANCE,
                settings.focusDistance
            )
        } else {
            optionsBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
        }

        // White Balance
        optionsBuilder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AWB_MODE,
            settings.whiteBalance.value
        )

        try {
            camera2Control.setCaptureRequestOptions(optionsBuilder.build())
            // Exposure compensation index
            cameraControl.setExposureCompensationIndex(settings.evCompensation)
        } catch (e: Exception) {
            Log.e("CameraXManager", "Failed to apply Pro settings", e)
        }
    }

    fun setZoom(ratio: Float) {
        primaryCamera?.cameraControl?.setZoomRatio(ratio)
    }

    fun getZoomState() = primaryCamera?.cameraInfo?.zoomState

    fun focusOnPoint(x: Float, y: Float) {
        val view = primaryPreviewView ?: return
        val factory = view.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        primaryCamera?.cameraControl?.startFocusAndMetering(action)
    }

    fun setFlashMode(flashMode: FlashModeOption) {
        currentFlashMode = flashMode
        when (flashMode) {
            FlashModeOption.TORCH -> {
                primaryCamera?.cameraControl?.enableTorch(true)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
            }
            FlashModeOption.ON -> {
                primaryCamera?.cameraControl?.enableTorch(false)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
            }
            FlashModeOption.AUTO -> {
                primaryCamera?.cameraControl?.enableTorch(false)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
            }
            FlashModeOption.OFF -> {
                primaryCamera?.cameraControl?.enableTorch(false)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
            }
        }
    }

    fun capturePhoto(
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("ImageCapture not bound"))
            return
        }

        val name = "ProCam_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ProCamera")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    if (savedUri != null) {
                        onSuccess(savedUri)
                    } else {
                        onError(IllegalStateException("Saved URI is null"))
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraXManager", "Photo capture failed: ${exception.message}", exception)
                    onError(exception)
                }
            }
        )
    }

    fun startRecording(
        onStart: () -> Unit,
        onDurationUpdate: (Long) -> Unit,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val video = videoCapture ?: run {
            onError(IllegalStateException("VideoCapture not ready"))
            return
        }

        // Storage safety check
        if (!StorageHelper.isSafeToRecord(context)) {
            onError(IllegalStateException("Storage space is too low to record video."))
            return
        }

        val name = "ProCam_VID_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ProCamera")
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        var pendingRecording = video.output.prepareRecording(context, mediaStoreOutput)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pendingRecording = pendingRecording.withAudioEnabled()
        }

        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    onStart()
                }
                is VideoRecordEvent.Status -> {
                    val durationNanos = event.recordingStats.recordedDurationNanos
                    onDurationUpdate(durationNanos / 1_000_000_000L)
                    // Auto-protect storage: cleanly stop if device storage becomes critical
                    if (!StorageHelper.isSafeToRecord(context)) {
                        Log.w("CameraXManager", "Storage critical: stopping recording safely")
                        stopRecording()
                    }
                }
                is VideoRecordEvent.Finalize -> {
                    if (!event.hasError()) {
                        val uri = event.outputResults.outputUri
                        onSuccess(uri)
                    } else {
                        onError(Exception("Recording finalized with error code: ${event.error}"))
                    }
                    activeRecording = null
                }
            }
        }
    }

    fun pauseRecording() {
        activeRecording?.pause()
    }

    fun resumeRecording() {
        activeRecording?.resume()
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun isRecording(): Boolean = activeRecording != null

    fun release() {
        try {
            activeRecording?.stop()
            activeRecording = null
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e("CameraXManager", "Error releasing camera resources", e)
        }
    }
}
