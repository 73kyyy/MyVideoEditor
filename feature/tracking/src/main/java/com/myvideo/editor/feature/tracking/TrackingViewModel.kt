package com.myvideo.editor.feature.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TrackingViewModel {
    var isTracking by mutableStateOf(false); private set
    var trackingProgress by mutableStateOf(0f); private set
    var selectedPointX by mutableStateOf(0f)
    var selectedPointY by mutableStateOf(0f)

    fun startTracking(x: Float, y: Float) { selectedPointX = x; selectedPointY = y; isTracking = true }
    fun stopTracking() { isTracking = false; trackingProgress = 0f }
    fun updateProgress(p: Float) { trackingProgress = p }
}
