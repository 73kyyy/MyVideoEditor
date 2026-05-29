package com.myvideo.editor.feature.tracking.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun PointSelector(onPointSelected: (Float, Float) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures { offset -> onPointSelected(offset.x, offset.y) }
    })
}
