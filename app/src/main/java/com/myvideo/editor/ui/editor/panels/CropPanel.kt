package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun CropPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    val ratioPresets = listOf("自由", "1:1", "16:9", "9:16", "4:3", "3:4", "21:9")
    var selectedRatio by remember { mutableStateOf("自由") }
    var cropLeft by remember { mutableStateOf(0.1f) }
    var cropTop by remember { mutableStateOf(0.1f) }
    var cropRight by remember { mutableStateOf(0.9f) }
    var cropBottom by remember { mutableStateOf(0.9f) }
    var rotation by remember { mutableStateOf(0) }
    var flipH by remember { mutableStateOf(false) }
    var flipV by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Aspect ratio presets
        Text("画面比例", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ratioPresets.forEach { ratio ->
                OptionChip(ratio, selectedRatio == ratio) {
                    selectedRatio = ratio
                    when (ratio) {
                        "1:1" -> { cropLeft = 0.15f; cropTop = 0f; cropRight = 0.85f; cropBottom = 1f }
                        "16:9" -> { cropLeft = 0f; cropTop = 0.11f; cropRight = 1f; cropBottom = 0.89f }
                        "9:16" -> { cropLeft = 0.22f; cropTop = 0f; cropRight = 0.78f; cropBottom = 1f }
                        "4:3" -> { cropLeft = 0.06f; cropTop = 0f; cropRight = 0.94f; cropBottom = 1f }
                        "3:4" -> { cropLeft = 0.18f; cropTop = 0f; cropRight = 0.82f; cropBottom = 1f }
                        "21:9" -> { cropLeft = 0f; cropTop = 0.19f; cropRight = 1f; cropBottom = 0.81f }
                        else -> { cropLeft = 0.1f; cropTop = 0.1f; cropRight = 0.9f; cropBottom = 0.9f }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Crop region canvas with draggable handles
        Text("裁剪区域", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, CG.Line, RoundedCornerShape(8.dp))
        ) {
            CropOverlayCanvas(
                cropLeft = cropLeft,
                cropTop = cropTop,
                cropRight = cropRight,
                cropBottom = cropBottom,
                onCropChange = { l, t, r, b ->
                    cropLeft = l; cropTop = t; cropRight = r; cropBottom = b
                }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Rotation slider
        Text("旋转", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("角度", -180, rotation, 180) { rotation = it }
        Spacer(modifier = Modifier.height(8.dp))

        // 90° increment buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(-90, -45, 0, 45, 90).forEach { angle ->
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (rotation == angle) CG.AccS else CG.Card)
                    .then(if (rotation == angle) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                    .clickable { rotation = angle }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center) {
                    Text("${angle}°", fontSize = 10.sp, fontWeight = FontWeight.Medium,
                        color = if (rotation == angle) CG.AccL else CG.T2)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Flip buttons
        Text("翻转", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (flipH) CG.AccS else CG.Card)
                .then(if (flipH) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                .clickable { flipH = !flipH },
                contentAlignment = Alignment.Center) {
                Text("↔ 水平翻转", fontSize = 10.sp, color = if (flipH) CG.AccL else CG.T2)
            }
            Box(modifier = Modifier.weight(1f).height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (flipV) CG.AccS else CG.Card)
                .then(if (flipV) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                .clickable { flipV = !flipV },
                contentAlignment = Alignment.Center) {
                Text("↕ 垂直翻转", fontSize = 10.sp, color = if (flipV) CG.AccL else CG.T2)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Reset and Apply buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CG.Card)
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable {
                    cropLeft = 0f; cropTop = 0f; cropRight = 1f; cropBottom = 1f
                    rotation = 0; flipH = false; flipV = false; selectedRatio = "自由"
                    vm.showToast("已重置")
                },
                contentAlignment = Alignment.Center) {
                Text("重置", fontSize = 12.sp, color = CG.T2, fontWeight = FontWeight.Medium)
            }
            Box(modifier = Modifier.weight(2f).height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CG.Acc)
                .clickable {
                    val clip = vm.selectedClip()
                    if (clip != null) {
                        val params = mapOf<String, Any>(
                            "cropLeft" to cropLeft, "cropTop" to cropTop,
                            "cropRight" to cropRight, "cropBottom" to cropBottom,
                            "rotation" to rotation.toFloat(),
                            "flipH" to flipH, "flipV" to flipV,
                            "ratio" to selectedRatio
                        )
                        val result = bridge.applyEffect("crop", params)
                        if (result) {
                            vm.showToast("裁剪已应用: $selectedRatio 旋转${rotation}°")
                        } else {
                            vm.showToast("裁剪应用失败")
                        }
                    } else {
                        vm.showToast("请先选择片段")
                    }
                    onClose()
                },
                contentAlignment = Alignment.Center) {
                Text("应用裁剪", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CropOverlayCanvas(
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    onCropChange: (Float, Float, Float, Float) -> Unit
) {
    var activeHandle by remember { mutableStateOf<String?>(null) }

    Canvas(modifier = Modifier.fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    val w = size.width; val h = size.height
                    val l = cropLeft * w; val t = cropTop * h
                    val r = cropRight * w; val b = cropBottom * h
                    val handleSize = 20f
                    when {
                        offset.x < l + handleSize && offset.y < t + handleSize -> activeHandle = "tl"
                        offset.x > r - handleSize && offset.y < t + handleSize -> activeHandle = "tr"
                        offset.x < l + handleSize && offset.y > b - handleSize -> activeHandle = "bl"
                        offset.x > r - handleSize && offset.y > b - handleSize -> activeHandle = "br"
                        offset.x in l..r && offset.y < t + handleSize -> activeHandle = "t"
                        offset.x in l..r && offset.y > b - handleSize -> activeHandle = "b"
                        offset.y in t..b && offset.x < l + handleSize -> activeHandle = "l"
                        offset.y in t..b && offset.x > r - handleSize -> activeHandle = "r"
                        else -> activeHandle = null
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val w = size.width; val h = size.height
                    val dx = (dragAmount.x / w).coerceIn(-0.05f, 0.05f)
                    val dy = (dragAmount.y / h).coerceIn(-0.05f, 0.05f)
                    when (activeHandle) {
                        "tl" -> onCropChange(
                            (cropLeft + dx).coerceIn(0f, cropRight - 0.05f),
                            (cropTop + dy).coerceIn(0f, cropBottom - 0.05f),
                            cropRight, cropBottom)
                        "tr" -> onCropChange(cropLeft,
                            (cropTop + dy).coerceIn(0f, cropBottom - 0.05f),
                            (cropRight + dx).coerceIn(cropLeft + 0.05f, 1f), cropBottom)
                        "bl" -> onCropChange(
                            (cropLeft + dx).coerceIn(0f, cropRight - 0.05f),
                            cropTop, cropRight,
                            (cropBottom + dy).coerceIn(cropTop + 0.05f, 1f))
                        "br" -> onCropChange(cropLeft, cropTop,
                            (cropRight + dx).coerceIn(cropLeft + 0.05f, 1f),
                            (cropBottom + dy).coerceIn(cropTop + 0.05f, 1f))
                        "t" -> onCropChange(cropLeft,
                            (cropTop + dy).coerceIn(0f, cropBottom - 0.05f),
                            cropRight, cropBottom)
                        "b" -> onCropChange(cropLeft, cropTop, cropRight,
                            (cropBottom + dy).coerceIn(cropTop + 0.05f, 1f))
                        "l" -> onCropChange(
                            (cropLeft + dx).coerceIn(0f, cropRight - 0.05f),
                            cropTop, cropRight, cropBottom)
                        "r" -> onCropChange(cropLeft, cropTop,
                            (cropRight + dx).coerceIn(cropLeft + 0.05f, 1f),
                            cropBottom)
                    }
                },
                onDragEnd = { activeHandle = null },
                onDragCancel = { activeHandle = null }
            )
        }
    ) {
        val w = size.width; val h = size.height
        val l = cropLeft * w; val t = cropTop * h
        val r = cropRight * w; val b = cropBottom * h

        // Dim outside crop area
        val dimColor = Color(0xAA000000)
        // Top
        drawRect(dimColor, Offset.Zero, Size(w, t))
        // Bottom
        drawRect(dimColor, Offset(0f, b), Size(w, h - b))
        // Left
        drawRect(dimColor, Offset(0f, t), Size(l, b - t))
        // Right
        drawRect(dimColor, Offset(r, t), Size(w - r, b - t))

        // Crop border
        drawRect(Color.White, Offset(l, t), Size(r - l, b - t), style = Stroke(width = 2f))

        // Grid lines (rule of thirds)
        val thirdW = (r - l) / 3f
        val thirdH = (b - t) / 3f
        for (i in 1..2) {
            drawLine(Color.White.copy(alpha = 0.3f),
                Offset(l + thirdW * i, t), Offset(l + thirdW * i, b), 1f)
            drawLine(Color.White.copy(alpha = 0.3f),
                Offset(l, t + thirdH * i), Offset(r, t + thirdH * i), 1f)
        }

        // Corner handles
        val handleLen = 16f
        val handleColor = CG.AccL
        // Top-left
        drawLine(handleColor, Offset(l, t), Offset(l + handleLen, t), 3f)
        drawLine(handleColor, Offset(l, t), Offset(l, t + handleLen), 3f)
        // Top-right
        drawLine(handleColor, Offset(r, t), Offset(r - handleLen, t), 3f)
        drawLine(handleColor, Offset(r, t), Offset(r, t + handleLen), 3f)
        // Bottom-left
        drawLine(handleColor, Offset(l, b), Offset(l + handleLen, b), 3f)
        drawLine(handleColor, Offset(l, b), Offset(l, b - handleLen), 3f)
        // Bottom-right
        drawLine(handleColor, Offset(r, b), Offset(r - handleLen, b), 3f)
        drawLine(handleColor, Offset(r, b), Offset(r, b - handleLen), 3f)
    }
}
