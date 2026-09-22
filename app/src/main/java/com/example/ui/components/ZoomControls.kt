package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraYellow

@Composable
fun ZoomControls(
    currentZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomOptions = listOf(0.5f, 1.0f, 2.0f, 5.0f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x77000000))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("zoom_controls")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            zoomOptions.forEach { zoom ->
                val isSelected = kotlin.math.abs(currentZoom - zoom) < 0.2f
                val label = when (zoom) {
                    0.5f -> ".5"
                    1.0f -> "1x"
                    2.0f -> "2"
                    5.0f -> "5"
                    else -> "${zoom.toInt()}x"
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) CameraYellow else Color.Transparent)
                        .clickable { onZoomSelected(zoom) }
                        .testTag("zoom_$label"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }
    }
}
