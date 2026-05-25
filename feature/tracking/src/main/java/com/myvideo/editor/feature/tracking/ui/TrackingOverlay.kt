package com.myvideo.editor.feature.tracking.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun TrackingOverlay(x: Float, y: Float, isTracking: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (isTracking) {
            drawCircle(Color.Yellow, 20f, Offset(x, y), style = Stroke(3f))
            drawCircle(Color.Red, 5f, Offset(x, y))
            drawLine(Color.Yellow.copy(alpha = 0.5f), Offset(x - 40, y), Offset(x + 40, y), 1f)
            drawLine(Color.Yellow.copy(alpha = 0.5f), Offset(x, y - 40), Offset(x, y + 40), 1f)
        }
    }
}
