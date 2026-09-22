package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CameraColorScheme = darkColorScheme(
  primary = CameraYellow,
  onPrimary = Color.Black,
  primaryContainer = CameraAmber,
  secondary = CameraCyan,
  onSecondary = Color.Black,
  tertiary = CameraRed,
  background = CameraBlack,
  onBackground = CameraWhite,
  surface = CameraDarkGrey,
  onSurface = CameraWhite,
  surfaceVariant = CameraSurface,
  onSurfaceVariant = CameraMutedWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CameraColorScheme,
    typography = Typography,
    content = content
  )
}
