package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CapturedMedia
import com.example.data.model.MediaType
import com.example.ui.theme.CameraAmber
import com.example.ui.theme.CameraDarkGrey
import com.example.ui.theme.CameraYellow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySheet(
    mediaList: List<CapturedMedia>,
    selectedMedia: CapturedMedia?,
    onMediaSelected: (CapturedMedia) -> Unit,
    onDeleteMedia: (CapturedMedia) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CameraDarkGrey,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("gallery_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Captured Moments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            if (selectedMedia != null) {
                // Main Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedMedia.uri,
                        contentDescription = "Media Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video Play overlay button
                    if (selectedMedia.type == MediaType.VIDEO) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(selectedMedia.uri, "video/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore if no video player
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .border(2.dp, CameraYellow, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play Video",
                                tint = CameraYellow,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Resolution badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xBB000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (selectedMedia.type == MediaType.VIDEO) "VIDEO • ${selectedMedia.resolution.ifEmpty { "4K UHD" }}" else "PHOTO • ${selectedMedia.resolution.ifEmpty { "HIGH RES" }}",
                            color = CameraYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Share & Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val dateFormatted = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                            .format(Date(selectedMedia.timestamp))
                        Text(
                            text = selectedMedia.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = dateFormatted,
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Open in Default Gallery App
                        IconButton(
                            onClick = {
                                com.example.camera.GalleryHelper.openDefaultGallery(
                                    context = context,
                                    latestMedia = selectedMedia,
                                    onFallbackToInternal = {}
                                )
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = "Open in Default Gallery",
                                tint = CameraYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Share
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = if (selectedMedia.type == MediaType.VIDEO) "video/*" else "image/*"
                                    putExtra(Intent.EXTRA_STREAM, selectedMedia.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        ) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        // Delete
                        IconButton(
                            onClick = { onDeleteMedia(selectedMedia) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FF3B30))
                        ) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thumbnail Strip
                if (mediaList.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(mediaList) { item ->
                            val isCurrent = item.uri == selectedMedia.uri
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        if (isCurrent) CameraYellow else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onMediaSelected(item) }
                            ) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (item.type == MediaType.VIDEO) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x88000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No captured photos or videos yet",
                        color = Color(0x88FFFFFF),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
