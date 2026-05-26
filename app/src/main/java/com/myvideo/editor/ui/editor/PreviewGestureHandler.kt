package com.myvideo.editor.ui.editor

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 预览区手势处理：双指缩放/旋转/平移，单指拖拽，双击重置
 */
@Composable
fun Modifier.previewGestures(
    scale: Float,
    rotation: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onDoubleTap: () -> Unit
): Modifier {
    return this
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, rotationDelta ->
                val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                onScaleChange(newScale)
                val newRotation = rotation + rotationDelta
                onRotationChange(newRotation)
                onOffsetChange(offsetX + pan.x, offsetY + pan.y)
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    onScaleChange(1f)
                    onRotationChange(0f)
                    onOffsetChange(0f, 0f)
                    onDoubleTap()
                }
            )
        }
}

/**
 * EditorViewModel的预览区状态扩展
 */
class PreviewTransformState {
    var scale by mutableStateOf(1f)
    var rotation by mutableStateOf(0f)
    var offsetX by mutableStateOf(0f)
    var offsetY by mutableStateOf(0f)
    var isAnimating by mutableStateOf(false)

    fun reset() {
        scale = 1f; rotation = 0f; offsetX = 0f; offsetY = 0f
    }

    fun zoomIn() { scale = (scale * 1.2f).coerceIn(0.5f, 5f) }
    fun zoomOut() { scale = (scale / 1.2f).coerceIn(0.5f, 5f) }
    fun fitToScreen() { reset() }

    fun panLeft() { offsetX -= 20f }
    fun panRight() { offsetX += 20f }
    fun panUp() { offsetY -= 20f }
    fun panDown() { offsetY += 20f }
}
