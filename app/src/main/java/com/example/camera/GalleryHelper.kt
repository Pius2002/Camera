package com.example.camera

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.data.model.CapturedMedia
import com.example.data.model.MediaType

object GalleryHelper {

    private const val TAG = "GalleryHelper"

    /**
     * Opens the device's default gallery app to view the last captured media or gallery roll.
     * Falls back gracefully if no external viewer is available.
     */
    fun openDefaultGallery(
        context: Context,
        latestMedia: CapturedMedia?,
        onFallbackToInternal: () -> Unit
    ) {
        val uri = latestMedia?.uri

        // 1. Try opening the specific photo or video in default gallery app
        if (uri != null) {
            try {
                val mimeType = if (latestMedia.type == MediaType.VIDEO) "video/*" else "image/*"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Direct ACTION_VIEW failed for uri: $uri, trying generic gallery", e)
            }
        }

        // 2. Try launching system gallery app
        try {
            val galleryIntent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(galleryIntent)
            return
        } catch (e: Exception) {
            Log.w(TAG, "ACTION_VIEW image/* failed", e)
        }

        // 3. Try standard Android CATEGORY_APP_GALLERY
        try {
            val appGalleryIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_GALLERY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appGalleryIntent)
            return
        } catch (e: Exception) {
            Log.w(TAG, "CATEGORY_APP_GALLERY intent failed", e)
        }

        // 4. Try opening MediaStore collection
        try {
            val mediaStoreIntent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mediaStoreIntent)
            return
        } catch (e: Exception) {
            Log.w(TAG, "MediaStore external uri intent failed", e)
        }

        // 5. Fallback to internal gallery viewer
        Toast.makeText(context, "Opening built-in gallery preview", Toast.LENGTH_SHORT).show()
        onFallbackToInternal()
    }

    /**
     * Loads recently captured camera media from MediaStore so the thumbnail displays immediately
     */
    fun queryLatestMedia(context: Context): List<CapturedMedia> {
        val result = mutableListOf<CapturedMedia>()
        try {
            val contentResolver = context.contentResolver

            // Query latest photos
            val imageProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE
            )
            val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            contentResolver.query(imageUri, imageProjection, null, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

                var count = 0
                while (cursor.moveToNext() && count < 15) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Photo"
                    val dateAdded = cursor.getLong(dateCol) * 1000L
                    val size = cursor.getLong(sizeCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    result.add(
                        CapturedMedia(
                            uri = contentUri,
                            type = MediaType.PHOTO,
                            title = name,
                            timestamp = dateAdded,
                            sizeBytes = size,
                            resolution = ""
                        )
                    )
                    count++
                }
            }

            // Query latest videos
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE
            )
            val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val videoSort = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            contentResolver.query(videoUri, videoProjection, null, null, videoSort)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                var count = 0
                while (cursor.moveToNext() && count < 10) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Video"
                    val dateAdded = cursor.getLong(dateCol) * 1000L
                    val size = cursor.getLong(sizeCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    result.add(
                        CapturedMedia(
                            uri = contentUri,
                            type = MediaType.VIDEO,
                            title = name,
                            timestamp = dateAdded,
                            sizeBytes = size,
                            resolution = ""
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying media store", e)
        }

        // Sort descending by date
        return result.sortedByDescending { it.timestamp }
    }
}
