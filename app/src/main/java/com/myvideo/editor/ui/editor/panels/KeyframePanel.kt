package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class KeyframeData(
    val id: String,
    val position: Float,
    var value: Float,
    var easing: String = "线性"
)

@Composable
fun KeyframePanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    val properties = listOf("位置X", "位置Y", "缩放", "旋转", "透明度")
    var selectedProperty by remember { mutableStateOf("位置X") }
    var keyframes by remember { mutableStateOf(mutableListOf<KeyframeData>()) }
    var selectedKfId by remember { mutableStateOf<String?>(null) }
    var clipboardKf by remember { mutableStateOf<KeyframeData?>(null) }

    // Initialize some demo keyframes
    LaunchedEffect(selectedProperty) {
        if (keyframes.isEmpty()) {
            keyframes = mutableListOf(
                KeyframeData("kf1", 0f, 0f, "线性"),
                KeyframeData("kf2", 0.5f, 100f, "缓入缓出"),
                KeyframeData("kf3", 1f, 0f, "缓出")
            )
            selectedKfId = null
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Property selector
        Text("属性", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            properties.forEach { prop ->
                OptionChip(prop, selectedProperty == prop) {
                    selectedProperty = prop
                    keyframes = mutableListOf(
                        KeyframeData("kf_${prop}_1", 0f, 0f, "线性"),
                        KeyframeData("kf_${prop}_2", 0.5f, 100f, "缓入缓出"),
                        KeyframeData("kf_${prop}_3", 1f, 0f, "缓出")
                    )
                    selectedKfId = null
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Keyframe timeline canvas
        Text("关键帧时间线", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, CG.Line, RoundedCornerShape(8.dp))
        ) {
            KeyframeTimelineCanvas(
                keyframes = keyframes,
                selectedKfId = selectedKfId,
                onKeyframeTap = { kfId -> selectedKfId = kfId },
                onKeyframeDrag = { kfId, newPos ->
                    val idx = keyframes.indexOfFirst { it.id == kfId }
                    if (idx >= 0) {
                        keyframes[idx] = keyframes[idx].copy(position = newPos.coerceIn(0f, 1f))
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Navigation and action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Prev keyframe
            Box(modifier = Modifier.weight(1f).height(32.dp)
                .clip(RoundedCornerShape(8.dp)).background(CG.Card)
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable {
                    val currentIdx = keyframes.indexOfFirst { it.id == selectedKfId }
                    if (currentIdx > 0) {
                        selectedKfId = keyframes[currentIdx - 1].id
                    }
                }, contentAlignment = Alignment.Center) {
                Text("◀ 上一帧", fontSize = 10.sp, color = CG.T2)
            }
            // Add keyframe
            Box(modifier = Modifier.weight(1f).height(32.dp)
                .clip(RoundedCornerShape(8.dp)).background(CG.Acc.copy(alpha = 0.15f))
                .border(1.dp, CG.Acc.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable {
                    val newId = "kf_${System.currentTimeMillis()}"
                    val newPos = (vm.playheadPosition - vm.rulerStartPx) /
                            (vm.clips.firstOrNull()?.widthPx ?: 140f).coerceAtLeast(1f)
                    keyframes.add(KeyframeData(newId, newPos.coerceIn(0f, 1f), 50f, "线性"))
                    keyframes.sortBy { it.position }
                    selectedKfId = newId
                    vm.showToast("已添加关键帧")
                }, contentAlignment = Alignment.Center) {
                Text("+ 添加", fontSize = 10.sp, color = CG.AccL)
            }
            // Delete keyframe
            Box(modifier = Modifier.weight(1f).height(32.dp)
                .clip(RoundedCornerShape(8.dp)).background(CG.Red.copy(alpha = 0.15f))
                .border(1.dp, CG.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable {
                    if (selectedKfId != null) {
                        keyframes.removeAll { it.id == selectedKfId }
                        selectedKfId = null
                        vm.showToast("已删除关键帧")
                    }
                }, contentAlignment = Alignment.Center) {
                Text("删除", fontSize = 10.sp, color = CG.Red)
            }
            // Next keyframe
            Box(modifier = Modifier.weight(1f).height(32.dp)
                .clip(RoundedCornerShape(8.dp)).background(CG.Card)
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable {
                    val currentIdx = keyframes.indexOfFirst { it.id == selectedKfId }
                    if (currentIdx < keyframes.size - 1) {
                        selectedKfId = keyframes[currentIdx + 1].id
                    }
                }, contentAlignment = Alignment.Center) {
                Text("下一帧 ▶", fontSize = 10.sp, color = CG.T2)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Value editor for selected keyframe
        val selectedKf = keyframes.find { it.id == selectedKfId }
        if (selectedKf != null) {
            Text("关键帧数值", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            val valueRange = when (selectedProperty) {
                "位置X", "位置Y" -> -1000 to 1000
                "缩放" -> 0 to 500
                "旋转" -> -360 to 360
                "透明度" -> 0 to 100
                else -> 0 to 100
            }
            CgSlider(
                selectedProperty,
                valueRange.first,
                selectedKf.value.toInt().coerceIn(valueRange.first, valueRange.second),
                valueRange.second
            ) { newVal ->
                val idx = keyframes.indexOfFirst { it.id == selectedKfId }
                if (idx >= 0) {
                    keyframes[idx] = keyframes[idx].copy(value = newVal.toFloat())
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Easing type
        Text("缓动类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("线性", "缓入", "缓出", "缓入缓出", "弹性").forEach { easing ->
                val isSelected = selectedKf?.easing == easing
                OptionChip(easing, isSelected) {
                    if (selectedKfId != null) {
                        val idx = keyframes.indexOfFirst { it.id == selectedKfId }
                        if (idx >= 0) {
                            keyframes[idx] = keyframes[idx].copy(easing = easing)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Copy / Paste
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).height(32.dp)
                .clip(RoundedCornerShape(8.dp)).background(CG.Card)
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable {
                    val kf = keyframes.find { it.id == selectedKfId }
                    if (kf != null) {
                        clipboardKf = kf
                        vm.showToast("已复制关键帧")
                    } else {
                        vm.showToast("请先选择关键帧")
                    }
                }, contentAlignment = Alignment.Center) {
                Text("复制关键帧", fontSize = 10.sp, color = CG.T2)
            }
            Box(modifier = Modifier.weight(1f).height(32.dp)
                .clip(RoundedCornerShape(8.dp)).background(CG.Card)
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable {
                    val copied = clipboardKf
                    if (copied != null) {
                        val newId = "kf_${System.currentTimeMillis()}"
                        val newPos = (vm.playheadPosition - vm.rulerStartPx) /
                                (vm.clips.firstOrNull()?.widthPx ?: 140f).coerceAtLeast(1f)
                        keyframes.add(copied.copy(id = newId, position = newPos.coerceIn(0f, 1f)))
                        keyframes.sortBy { it.position }
                        selectedKfId = newId
                        vm.showToast("已粘贴关键帧")
                    } else {
                        vm.showToast("请先复制关键帧")
                    }
                }, contentAlignment = Alignment.Center) {
                Text("粘贴关键帧", fontSize = 10.sp, color = CG.T2)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Apply button
        ApplyButton("应用关键帧") {
            val propertyMap = mapOf(
                "位置X" to "position_x", "位置Y" to "position_y",
                "缩放" to "scale", "旋转" to "rotation", "透明度" to "opacity"
            )
            val clip = vm.selectedClip()
            if (clip != null) {
                keyframes.forEach { kf ->
                    val params = mapOf<String, Any>(
                        "property" to (propertyMap[selectedProperty] ?: "position_x"),
                        "position" to kf.position,
                        "value" to kf.value,
                        "easing" to kf.easing
                    )
                    bridge.applyEffect("keyframe", params)
                }
                bridge.setTrackProperty(clip.id, propertyMap[selectedProperty] ?: "position_x",
                    keyframes.firstOrNull()?.value ?: 0f)
                vm.showToast("关键帧已应用: $selectedProperty (${keyframes.size}帧)")
            } else {
                vm.showToast("请先选择片段")
            }
            onClose()
        }
    }
}

@Composable
private fun KeyframeTimelineCanvas(
    keyframes: List<KeyframeData>,
    selectedKfId: String?,
    onKeyframeTap: (String) -> Unit,
    onKeyframeDrag: (String, Float) -> Unit
) {
    var dragKfId by remember { mutableStateOf<String?>(null) }

    Canvas(modifier = Modifier.fillMaxSize()
        .pointerInput(keyframes) {
            detectTapGestures { offset ->
                val timelineWidth = size.width - 32f
                keyframes.forEach { kf ->
                    val kfX = 16f + kf.position * timelineWidth
                    val kfY = size.height / 2f
                    val dist = kotlin.math.sqrt((offset.x - kfX) * (offset.x - kfX) +
                            (offset.y - kfY) * (offset.y - kfY))
                    if (dist < 16f) {
                        onKeyframeTap(kf.id)
                        return@detectTapGestures
                    }
                }
            }
        }
        .pointerInput(keyframes) {
            detectDragGestures(
                onDragStart = { offset ->
                    val timelineWidth = size.width - 32f
                    keyframes.forEach { kf ->
                        val kfX = 16f + kf.position * timelineWidth
                        val kfY = size.height / 2f
                        val dist = kotlin.math.sqrt((offset.x - kfX) * (offset.x - kfX) +
                                (offset.y - kfY) * (offset.y - kfY))
                        if (dist < 20f) {
                            dragKfId = kf.id
                            onKeyframeTap(kf.id)
                            return@detectDragGestures
                        }
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (dragKfId != null) {
                        val timelineWidth = size.width - 32f
                        val delta = dragAmount.x / timelineWidth
                        val kf = keyframes.find { it.id == dragKfId }
                        if (kf != null) {
                            onKeyframeDrag(kf.id, kf.position + delta)
                        }
                    }
                },
                onDragEnd = { dragKfId = null },
                onDragCancel = { dragKfId = null }
            )
        }
    ) {
        val timelineWidth = size.width - 32f
        val timelineY = size.height / 2f

        // Draw timeline track
        drawLine(CG.Line, Offset(16f, timelineY), Offset(size.width - 16f, timelineY), 2f)

        // Draw time markers
        for (i in 0..10) {
            val x = 16f + (i / 10f) * timelineWidth
            drawLine(CG.T4, Offset(x, timelineY - 6f), Offset(x, timelineY + 6f), 1f)
        }

        // Draw value curve
        if (keyframes.size >= 2) {
            val sortedKfs = keyframes.sortedBy { it.position }
            val path = Path()
            val maxVal = sortedKfs.maxOfOrNull { kotlin.math.abs(it.value) }.coerceAtLeast(1f)
            for ((idx, kf) in sortedKfs.withIndex()) {
                val x = 16f + kf.position * timelineWidth
                val y = timelineY - (kf.value / maxVal) * (size.height / 2f - 16f)
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, CG.Acc.copy(alpha = 0.5f), style = Stroke(width = 2f))
        }

        // Draw keyframe diamonds
        keyframes.forEach { kf ->
            val x = 16f + kf.position * timelineWidth
            val y = timelineY
            val isSelected = kf.id == selectedKfId
            val diamondSize = if (isSelected) 10f else 7f

            val diamondPath = Path().apply {
                moveTo(x, y - diamondSize)
                lineTo(x + diamondSize, y)
                lineTo(x, y + diamondSize)
                lineTo(x - diamondSize, y)
                close()
            }
            drawPath(diamondPath, if (isSelected) CG.AccL else CG.Acc)
            if (isSelected) {
                drawPath(diamondPath, CG.AccL, style = Stroke(width = 2f))
            }
        }
    }
}
