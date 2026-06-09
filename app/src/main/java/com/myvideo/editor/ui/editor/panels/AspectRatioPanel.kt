package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun AspectRatioPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    val ratioPresets = listOf("16:9", "9:16", "1:1", "4:3", "3:4", "21:9", "4:5", "5:4")
    var selectedRatio by remember { mutableStateOf("16:9") }
    var customWidth by remember { mutableStateOf("1920") }
    var customHeight by remember { mutableStateOf("1080") }
    var useCustom by remember { mutableStateOf(false) }

    val fillModes = listOf("模糊", "黑色", "颜色", "图片")
    var selectedFill by remember { mutableStateOf("模糊") }
    var customColor by remember { mutableStateOf("#000000") }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Ratio presets
        Text("画面比例", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ratioPresets.forEach { ratio ->
                OptionChip(ratio, !useCustom && selectedRatio == ratio) {
                    useCustom = false; selectedRatio = ratio
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Custom ratio input
        ToggleRow("自定义比例", useCustom) { useCustom = it }
        if (useCustom) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CG.Card)
                    .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart) {
                    Text(customWidth.ifEmpty { "宽" }, fontSize = 11.sp, color = CG.T1)
                }
                Text("×", fontSize = 14.sp, color = CG.T3)
                Box(modifier = Modifier.weight(1f).height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CG.Card)
                    .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart) {
                    Text(customHeight.ifEmpty { "高" }, fontSize = 11.sp, color = CG.T1)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Preview showing how content fits
        Text("预览", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, CG.Line, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center) {
            AspectRatioPreviewCanvas(
                ratio = if (useCustom) {
                    val w = customWidth.toFloatOrNull() ?: 1920f
                    val h = customHeight.toFloatOrNull() ?: 1080f
                    if (h > 0) w / h else 16f / 9f
                } else {
                    parseRatio(selectedRatio)
                },
                fillMode = selectedFill
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Background fill mode
        Text("背景填充", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            fillModes.forEach { mode ->
                OptionChip(mode, selectedFill == mode) { selectedFill = mode }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Apply button
        ApplyButton("应用画面比例") {
            val clip = vm.selectedClip()
            val ratio = if (useCustom) {
                val w = customWidth.toFloatOrNull() ?: 1920f
                val h = customHeight.toFloatOrNull() ?: 1080f
                if (h > 0) w / h else 16f / 9f
            } else {
                parseRatio(selectedRatio)
            }
            val params = mapOf<String, Any>(
                "ratio" to selectedRatio,
                "ratioFloat" to ratio,
                "customWidth" to (customWidth.toIntOrNull() ?: 1920),
                "customHeight" to (customHeight.toIntOrNull() ?: 1080),
                "fillMode" to selectedFill,
                "customColor" to customColor
            )
            val result = bridge.applyEffect("aspect_ratio", params)
            if (result) {
                vm.canvasRatio = if (useCustom) "自定义" else selectedRatio
                if (useCustom) {
                    vm.customWidth = customWidth
                    vm.customHeight = customHeight
                }
                vm.showToast("画面比例已应用: ${if (useCustom) "${customWidth}×${customHeight}" else selectedRatio}")
            } else {
                vm.showToast("应用画面比例失败")
            }
            onClose()
        }
    }
}

private fun parseRatio(ratio: String): Float = when (ratio) {
    "16:9" -> 16f / 9f; "9:16" -> 9f / 16f; "1:1" -> 1f
    "4:3" -> 4f / 3f; "3:4" -> 3f / 4f; "21:9" -> 21f / 9f
    "4:5" -> 4f / 5f; "5:4" -> 5f / 4f
    else -> 16f / 9f
}

@Composable
private fun AspectRatioPreviewCanvas(ratio: Float, fillMode: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width; val canvasH = size.height
        val padding = 16f

        // Available area
        val availW = canvasW - padding * 2; val availH = canvasH - padding * 2

        // Calculate preview rect to fit the ratio
        val canvasRatio = availW / availH
        val previewW: Float; val previewH: Float
        if (ratio > canvasRatio) {
            previewW = availW; previewH = availW / ratio
        } else {
            previewH = availH; previewW = availH * ratio
        }
        val offsetX = padding + (availW - previewW) / 2f
        val offsetY = padding + (availH - previewH) / 2f

        // Draw background fill
        when (fillMode) {
            "模糊" -> {
                drawRect(CG.Acc.copy(alpha = 0.15f), Offset(offsetX, offsetY), Size(previewW, previewH))
            }
            "黑色" -> {
                drawRect(Color.Black, Offset(offsetX, offsetY), Size(previewW, previewH))
            }
            "颜色" -> {
                drawRect(Color(0xFF333355), Offset(offsetX, offsetY), Size(previewW, previewH))
            }
            "图片" -> {
                drawRect(Color(0xFF2A3A2A), Offset(offsetX, offsetY), Size(previewW, previewH))
            }
        }

        // Draw content area (simulating 16:9 original content within the new ratio)
        val contentRatio = 16f / 9f
        val contentW: Float; val contentH: Float
        if (contentRatio > ratio) {
            contentW = previewW; contentH = previewW / contentRatio
        } else {
            contentH = previewH; contentW = previewH * contentRatio
        }
        val contentOffsetX = offsetX + (previewW - contentW) / 2f
        val contentOffsetY = offsetY + (previewH - contentH) / 2f

        drawRect(CG.Acc.copy(alpha = 0.3f), Offset(contentOffsetX, contentOffsetY), Size(contentW, contentH))
        drawRect(Color.White.copy(alpha = 0.5f), Offset(contentOffsetX, contentOffsetY), Size(contentW, contentH))

        // Border
        drawRect(CG.T3, Offset(offsetX, offsetY), Size(previewW, previewH))
    }
}
