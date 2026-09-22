package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pro Camera", appName)
  }

  @Test
  fun `dual camera pip size presets and position snapping logic`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.CameraViewModel(app)

    // Set Dual mode
    viewModel.setMode(com.example.data.model.CameraMode.DUAL)
    assertEquals(com.example.data.model.CameraMode.DUAL, viewModel.uiState.value.currentMode)

    // Verify default PIP layout
    assertEquals(com.example.data.model.PipLayout.FLOATING_RECT, viewModel.uiState.value.pipLayout)

    // Change PIP size preset to LARGE
    viewModel.setPipSizePreset(com.example.data.model.PipSizePreset.LARGE)
    assertEquals(com.example.data.model.PipSizePreset.LARGE, viewModel.uiState.value.pipSizePreset)
    assertEquals(180f, viewModel.uiState.value.pipWidthDp)

    // Snap to TOP_LEFT
    viewModel.snapPipToCorner(com.example.data.model.PipPositionCorner.TOP_LEFT, 400f, 800f)
    assertEquals(com.example.data.model.PipPositionCorner.TOP_LEFT, viewModel.uiState.value.pipPositionCorner)
    assertEquals(16f, viewModel.uiState.value.pipOffsetX)

    // Swap dual cameras
    val initialFrontInPip = viewModel.uiState.value.isFrontInPip
    viewModel.swapDualCameras()
    assertEquals(!initialFrontInPip, viewModel.uiState.value.isFrontInPip)
  }

  @Test
  fun `video resolution selection and storage calculations`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.CameraViewModel(app)

    // Switch to video mode
    viewModel.setMode(com.example.data.model.CameraMode.VIDEO)
    assertEquals(com.example.data.model.CameraMode.VIDEO, viewModel.uiState.value.currentMode)

    // Select 4K video resolution
    viewModel.setVideoResolution(com.example.data.model.ResolutionOption.RES_4K)
    assertEquals(com.example.data.model.ResolutionOption.RES_4K, viewModel.uiState.value.currentVideoResolution)
    assertEquals(3840, viewModel.uiState.value.currentVideoResolution.width)
    assertEquals(2160, viewModel.uiState.value.currentVideoResolution.height)

    // Select 8K video resolution
    viewModel.setVideoResolution(com.example.data.model.ResolutionOption.RES_8K)
    assertEquals(com.example.data.model.ResolutionOption.RES_8K, viewModel.uiState.value.currentVideoResolution)
    assertEquals(7680, viewModel.uiState.value.currentVideoResolution.width)
    assertEquals(4320, viewModel.uiState.value.currentVideoResolution.height)

    // Test HEVC toggle
    viewModel.setUseHevcCodec(false)
    assertEquals(false, viewModel.uiState.value.useHevcCodec)
    viewModel.setUseHevcCodec(true)
    assertEquals(true, viewModel.uiState.value.useHevcCodec)

    // Test storage calculation utility
    val storageInfo = com.example.camera.StorageHelper.queryStorageInfo(
        context = app,
        resolution = com.example.data.model.ResolutionOption.RES_4K,
        isHevc = true
    )
    org.junit.Assert.assertNotNull(storageInfo.availableFormatted)
    org.junit.Assert.assertNotNull(storageInfo.estimatedTimeFormatted)
  }
}
