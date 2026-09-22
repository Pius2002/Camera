package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraGreen
import com.example.ui.theme.CameraYellow
import kotlin.math.abs

@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val lineColor = Color(0x33FFFFFF)
        val strokeWidth = 1.dp.toPx()

        // Two vertical lines
        drawLine(
            color = lineColor,
            start = Offset(width / 3f, 0f),
            end = Offset(width / 3f, height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = lineColor,
            start = Offset(width * 2f / 3f, 0f),
            end = Offset(width * 2f / 3f, height),
            strokeWidth = strokeWidth
        )

        // Two horizontal lines
        drawLine(
            color = lineColor,
            start = Offset(0f, height / 3f),
            end = Offset(width, height / 3f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, height * 2f / 3f),
            end = Offset(width, height * 2f / 3f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun LevelIndicatorOverlay(
    rollAngle: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val lineColor = if (isLevel) CameraYellow else Color(0x77FFFFFF)
        val centerDotColor = if (isLevel) CameraYellow else Color.White

        // Rotating horizon line
        Canvas(
            modifier = Modifier
                .size(160.dp, 60.dp)
                .graphicsLayer(rotationZ = -rollAngle)
        ) {
            val centerY = size.height / 2f
            val stroke = 2.dp.toPx()

            // Left wing
            drawLine(
                color = lineColor,
                start = Offset(10f, centerY),
                end = Offset(size.width * 0.38f, centerY),
                strokeWidth = stroke
            )

            // Right wing
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.62f, centerY),
                end = Offset(size.width - 10f, centerY),
                strokeWidth = stroke
            )

            // Center target circle
            drawCircle(
                color = centerDotColor,
                radius = 3.dp.toPx(),
                center = Offset(size.width / 2f, centerY)
            )
        }

        // Degree label
        val formattedAngle = String.format("%.1f°", abs(rollAngle))
        Box(
            modifier = Modifier
                .offset(y = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isLevel) CameraYellow.copy(alpha = 0.2f) else Color(0x55000000))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isLevel) "LEVEL 0°" else formattedAngle,
                color = if (isLevel) CameraYellow else Color(0xDDFFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TapToFocusIndicator(
    x: Float,
    y: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "focus_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .offset(
                x = (x - 40).coerceAtLeast(0f).dp,
                y = (y - 40).coerceAtLeast(0f).dp
            )
            .size(80.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1.5.dp.toPx()
            val bracketLen = 14.dp.toPx()

            // Outer ring
            drawCircle(
                color = CameraYellow,
                radius = size.width / 2f - 4.dp.toPx(),
                style = Stroke(width = stroke)
            )

            // Center crosshair dot
            drawCircle(
                color = CameraYellow,
                radius = 2.dp.toPx()
            )
        }

        // Small sun icon to adjust exposure indicator
        Icon(
            imageVector = Icons.Filled.WbSunny,
            contentDescription = null,
            tint = CameraYellow,
            modifier = Modifier
                .size(16.dp)
                .offset(x = 36.dp)
        )
    }
}

@Composable
fun TimerCountdownOverlay(
    secondsRemaining: Int,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1.4f) }

    LaunchedEffect(secondsRemaining) {
        scale.snapTo(1.5f)
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x33000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                .clip(CircleShape)
                .background(Color(0x99000000))
                .border(3.dp, CameraYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = secondsRemaining.toString(),
                color = CameraYellow,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
