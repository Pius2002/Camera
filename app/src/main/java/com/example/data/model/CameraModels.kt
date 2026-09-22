package com.example.data.model

import android.net.Uri

enum class CameraMode(val title: String) {
    PHOTO("PHOTO"),
    VIDEO("VIDEO"),
    PRO("PRO"),
    DUAL("DUAL")
}

enum class ResolutionOption(
    val label: String,
    val description: String,
    val width: Int,
    val height: Int,
    val approxBitrateMbps: Int = 45,
    val storagePerMinuteMb: Int = 340
) {
    RES_8K("8K", "7680×4320 (33MP / Ultra High)", 7680, 4320, approxBitrateMbps = 80, storagePerMinuteMb = 600),
    RES_4K("4K", "3840×2160 (UHD High Quality)", 3840, 2160, approxBitrateMbps = 45, storagePerMinuteMb = 340),
    RES_FHD("FHD", "1920×1080 (1080p Full HD)", 1920, 1080, approxBitrateMbps = 17, storagePerMinuteMb = 130),
    RES_HD("HD", "1280×720 (720p High Def)", 1280, 720, approxBitrateMbps = 8, storagePerMinuteMb = 60)
}

enum class AspectRatioOption(val label: String, val ratio: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("FULL", 20f / 9f)
}

enum class FlashModeOption(val label: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class TimerOption(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_10(10, "10s")
}

enum class PipLayout(val label: String) {
    FLOATING_RECT("PIP Rect"),
    FLOATING_ROUND("PIP Circle"),
    SPLIT_SCREEN("Split 50/50")
}

enum class PipSizePreset(val label: String, val widthDp: Float, val heightDp: Float) {
    SMALL("Small (100dp)", 105f, 140f),
    MEDIUM("Medium (140dp)", 140f, 185f),
    LARGE("Large (180dp)", 180f, 240f),
    XLARGE("Max (220dp)", 220f, 290f)
}

enum class PipPositionCorner(val label: String) {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_RIGHT("Bottom Right"),
    FREE("Custom Drag")
}

enum class WhiteBalanceOption(val label: String, val value: Int) {
    AUTO("Auto", 1),
    DAYLIGHT("Daylight", 5),
    CLOUDY("Cloudy", 6),
    FLUORESCENT("Fluorescent", 3),
    INCANDESCENT("Tungsten", 2)
}

enum class MediaType {
    PHOTO,
    VIDEO
}

data class CapturedMedia(
    val uri: Uri,
    val type: MediaType,
    val title: String,
    val timestamp: Long,
    val sizeBytes: Long = 0L,
    val durationSeconds: Int = 0,
    val resolution: String = ""
)

data class ProSettings(
    val isIsoManual: Boolean = false,
    val iso: Int = 100, // 50 to 6400
    val isShutterManual: Boolean = false,
    val shutterNanos: Long = 16_666_666L, // 1/60s default
    val shutterDisplay: String = "1/60",
    val isFocusManual: Boolean = false,
    val focusDistance: Float = 0.0f, // 0.0 = infinity, higher = closer (macro)
    val evCompensation: Int = 0, // -6 to +6 steps
    val whiteBalance: WhiteBalanceOption = WhiteBalanceOption.AUTO
)

data class StorageInfo(
    val availableBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val availableFormatted: String = "Calculating...",
    val isLowStorage: Boolean = false,
    val isCriticalStorage: Boolean = false,
    val estimatedTimeRemainingSeconds: Long = 0L,
    val estimatedTimeFormatted: String = ""
)

data class HardwareCameraCapabilities(
    val maxPhotoWidth: Int = 3840,
    val maxPhotoHeight: Int = 2160,
    val maxMegaPixels: Float = 12.0f,
    val supports8K: Boolean = false,
    val supports4K: Boolean = true,
    val supports8KVideo: Boolean = false,
    val supports4KVideo: Boolean = true,
    val maxVideoResolution: ResolutionOption = ResolutionOption.RES_4K,
    val supportedVideoResolutions: List<ResolutionOption> = listOf(ResolutionOption.RES_4K, ResolutionOption.RES_FHD, ResolutionOption.RES_HD),
    val supportsHevc: Boolean = true,
    val minIso: Int = 50,
    val maxIso: Int = 3200,
    val minExposureNanos: Long = 100_000L,
    val maxExposureNanos: Long = 1_000_000_000L,
    val minFocusDistance: Float = 10.0f,
    val supportsConcurrentCamera: Boolean = false,
    val hasFrontCamera: Boolean = true,
    val hasBackCamera: Boolean = true,
    val hasRearFlash: Boolean = true
)
