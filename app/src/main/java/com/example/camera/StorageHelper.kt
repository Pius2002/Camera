package com.example.camera

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.model.ResolutionOption
import com.example.data.model.StorageInfo
import java.io.File
import java.util.Locale

object StorageHelper {

    private const val LOW_STORAGE_THRESHOLD_BYTES = 1_500_000_000L // 1.5 GB
    private const val CRITICAL_STORAGE_THRESHOLD_BYTES = 400_000_000L // 400 MB

    fun queryStorageInfo(
        context: Context,
        resolution: ResolutionOption,
        isHevc: Boolean = true
    ): StorageInfo {
        return try {
            val storageDir = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
            val statFs = StatFs(storageDir.path)

            val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
            val totalBytes = statFs.blockCountLong * statFs.blockSizeLong

            val isLow = availableBytes < LOW_STORAGE_THRESHOLD_BYTES
            val isCritical = availableBytes < CRITICAL_STORAGE_THRESHOLD_BYTES

            // Base bitrate in Mbps adjusted for HEVC compression (HEVC saves ~35-40% storage)
            val effectiveBitrateMbps = if (isHevc) {
                (resolution.approxBitrateMbps * 0.65f).toInt().coerceAtLeast(4)
            } else {
                resolution.approxBitrateMbps
            }

            // Bytes per second: (Mbps * 1,000,000) / 8
            val bytesPerSecond = (effectiveBitrateMbps * 1_000_000L) / 8L
            val estimatedSeconds = if (bytesPerSecond > 0 && availableBytes > 0) {
                // Reserve 200MB safety buffer
                val usableBytes = (availableBytes - 200_000_000L).coerceAtLeast(0L)
                usableBytes / bytesPerSecond
            } else {
                0L
            }

            val formattedTime = formatDuration(estimatedSeconds)
            val formattedAvailable = formatFileSize(availableBytes)

            StorageInfo(
                availableBytes = availableBytes,
                totalBytes = totalBytes,
                availableFormatted = formattedAvailable,
                isLowStorage = isLow,
                isCriticalStorage = isCritical,
                estimatedTimeRemainingSeconds = estimatedSeconds,
                estimatedTimeFormatted = formattedTime
            )
        } catch (e: Exception) {
            StorageInfo(
                availableBytes = 5_000_000_000L,
                totalBytes = 64_000_000_000L,
                availableFormatted = "5.0 GB Free",
                isLowStorage = false,
                isCriticalStorage = false,
                estimatedTimeRemainingSeconds = 3600L,
                estimatedTimeFormatted = "1h 00m"
            )
        }
    }

    fun isSafeToRecord(context: Context): Boolean {
        return try {
            val storageDir = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
            val statFs = StatFs(storageDir.path)
            val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
            availableBytes > CRITICAL_STORAGE_THRESHOLD_BYTES
        } catch (e: Exception) {
            true
        }
    }

    fun formatFileSize(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format(Locale.US, "%.1f GB Free", gb)
        } else {
            val mb = bytes / (1024.0 * 1024.0)
            String.format(Locale.US, "%.0f MB Free", mb)
        }
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "0m"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            String.format(Locale.US, "%dh %02dm", hours, minutes)
        } else {
            String.format(Locale.US, "%dm", minutes)
        }
    }
}
