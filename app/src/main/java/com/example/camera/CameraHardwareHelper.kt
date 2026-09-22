package com.example.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Size
import com.example.data.model.HardwareCameraCapabilities

object CameraHardwareHelper {

    fun queryCapabilities(context: Context, isFront: Boolean = false): HardwareCameraCapabilities {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val targetFacing = if (isFront) {
                CameraCharacteristics.LENS_FACING_FRONT
            } else {
                CameraCharacteristics.LENS_FACING_BACK
            }

            var selectedCameraId: String? = null
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == targetFacing) {
                    selectedCameraId = id
                    break
                }
            }

            if (selectedCameraId == null && cameraManager.cameraIdList.isNotEmpty()) {
                selectedCameraId = cameraManager.cameraIdList[0]
            }

            val chars = selectedCameraId?.let { cameraManager.getCameraCharacteristics(it) }

            val streamMap = chars?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val jpegSizes: Array<Size> = streamMap?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()

            var maxWidth = 1920
            var maxHeight = 1080
            for (size in jpegSizes) {
                if (size.width * size.height > maxWidth * maxHeight) {
                    maxWidth = size.width
                    maxHeight = size.height
                }
            }

            // Check video recording output sizes
            val videoSizes: Array<Size> = streamMap?.getOutputSizes(android.media.MediaRecorder::class.java)
                ?: streamMap?.getOutputSizes(android.graphics.SurfaceTexture::class.java)
                ?: emptyArray()

            var maxVideoW = 1920
            var maxVideoH = 1080
            for (size in videoSizes) {
                if (size.width * size.height > maxVideoW * maxVideoH) {
                    maxVideoW = size.width
                    maxVideoH = size.height
                }
            }

            val maxPixels = maxWidth.toLong() * maxHeight.toLong()
            val maxMegaPixels = (maxPixels / 1_000_000f)
            val supports8KPhoto = maxWidth >= 7680 || maxHeight >= 7680 || maxMegaPixels >= 33f
            val supports4KPhoto = maxWidth >= 3840 || maxHeight >= 2160 || maxMegaPixels >= 8f

            var supports4KCamcorder = false
            var supports8KCamcorder = false
            try {
                val camIdInt = selectedCameraId?.toIntOrNull()
                if (camIdInt != null) {
                    if (android.media.CamcorderProfile.hasProfile(camIdInt, android.media.CamcorderProfile.QUALITY_2160P)) {
                        supports4KCamcorder = true
                    }
                    // QUALITY_8K is 10008 in Android API 31+
                    if (android.media.CamcorderProfile.hasProfile(camIdInt, 10008)) {
                        supports8KCamcorder = true
                    }
                }
            } catch (e: Exception) {
                // Ignore profile check errors
            }

            val supports8KVideo = supports8KCamcorder || (supports8KPhoto && maxVideoW >= 7680)
            val supports4KVideo = supports4KCamcorder || supports8KVideo || maxVideoW >= 3840 || supports4KPhoto

            val supportedVideoResolutions = mutableListOf<com.example.data.model.ResolutionOption>().apply {
                if (supports8KVideo) add(com.example.data.model.ResolutionOption.RES_8K)
                if (supports4KVideo) add(com.example.data.model.ResolutionOption.RES_4K)
                add(com.example.data.model.ResolutionOption.RES_FHD)
                add(com.example.data.model.ResolutionOption.RES_HD)
            }
            val maxVideoRes = supportedVideoResolutions.firstOrNull() ?: com.example.data.model.ResolutionOption.RES_FHD

            var supportsHevc = true
            try {
                val codecList = android.media.MediaCodecList(android.media.MediaCodecList.REGULAR_CODECS)
                supportsHevc = codecList.codecInfos.any {
                    it.isEncoder && it.supportedTypes.any { type -> type.equals("video/hevc", ignoreCase = true) }
                }
            } catch (e: Exception) {
                supportsHevc = true
            }

            val isoRange = chars?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val minIso = isoRange?.lower ?: 50
            val maxIso = isoRange?.upper ?: 3200

            val exposureRange = chars?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val minExposure = exposureRange?.lower ?: 100_000L // 0.1ms
            val maxExposure = exposureRange?.upper ?: 1_000_000_000L // 1s

            val minFocusDist = chars?.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 10.0f

            val supportsConcurrent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val concurrentSets = cameraManager.concurrentCameraIds
                    concurrentSets.isNotEmpty()
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }

            HardwareCameraCapabilities(
                maxPhotoWidth = maxWidth,
                maxPhotoHeight = maxHeight,
                maxMegaPixels = maxMegaPixels,
                supports8K = supports8KPhoto,
                supports4K = supports4KPhoto,
                supports8KVideo = supports8KVideo,
                supports4KVideo = supports4KVideo,
                maxVideoResolution = maxVideoRes,
                supportedVideoResolutions = supportedVideoResolutions,
                supportsHevc = supportsHevc,
                minIso = minIso,
                maxIso = maxIso,
                minExposureNanos = minExposure,
                maxExposureNanos = maxExposure,
                minFocusDistance = minFocusDist,
                supportsConcurrentCamera = supportsConcurrent,
                hasFrontCamera = cameraManager.cameraIdList.any {
                    cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
                },
                hasBackCamera = cameraManager.cameraIdList.any {
                    cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                }
            )
        } catch (e: Exception) {
            // Safe fallback
            HardwareCameraCapabilities()
        }
    }
}
