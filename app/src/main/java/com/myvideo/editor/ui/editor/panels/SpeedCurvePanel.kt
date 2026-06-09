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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class ControlPoint(val x: Float, val y: Float)

@Composable
fun SpeedCurvePanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    val presetCurves = listOf("匀速", "渐快", "渐慢", "快-慢-快", "慢-快-慢", "弹跳")
    var selectedPreset by remember { mutableStateOf("匀速") }
    var speedMin by remember { mutableStateOf(25) }  // 0.25x = 25
    var speedMax by remember { mutableStateOf(400) }  // 4x = 400
    var reversePlayback by remember { mutableStateOf(false) }
    var controlPoints by remember {
        mutableStateOf(
            listOf(
                ControlPoint(0f, 0.5f),   // start
                ControlPoint(0.25f, 0.5f),
                ControlPoint(0.5f, 0.5f),
                ControlPoint(0.75f, 0.5f),
                ControlPoint(1f, 0.5f)    // end
            )
        )
    }

    // Apply preset curves
    fun applyPreset(preset: String) {
        selectedPreset = preset
        controlPoints = when (preset) {
            "匀速" -> listOf(ControlPoint(0f, 0.5f), ControlPoint(0.25f, 0.5f), ControlPoint(0.5f, 0.5f), ControlPoint(0.75f, 0.5f), ControlPoint(1f, 0.5f))
            "渐快" -> listOf(ControlPoint(0f, 0.2f), ControlPoint(0.25f, 0.3f), ControlPoint(0.5f, 0.45f), ControlPoint(0.75f, 0.65f), ControlPoint(1f, 0.85f))
            "渐慢" -> listOf(ControlPoint(0f, 0.85f), ControlPoint(0.25f, 0.65f), ControlPoint(0.5f, 0.45f), ControlPoint(0.75f, 0.3f), ControlPoint(1f, 0.2f))
            "快-慢-快" -> listOf(ControlPoint(0f, 0.8f), ControlPoint(0.2f, 0.7f), ControlPoint(0.5f, 0.2f), ControlPoint(0.8f, 0.7f), ControlPoint(1f, 0.8f))
            "慢-快-慢" -> listOf(ControlPoint(0f, 0.2f), ControlPoint(0.2f, 0.35f), ControlPoint(0.5f, 0.85f), ControlPoint(0.8f, 0.35f), ControlPoint(1f, 0.2f))
            "弹跳" -> listOf(ControlPoint(0f, 0.5f), ControlPoint(0.15f, 0.9f), ControlPoint(0.3f, 0.2f), ControlPoint(0.5f, 0.85f), ControlPoint(0.7f, 0.25f), ControlPoint(0.85f, 0.75f), ControlPoint(1f, 0.5f))
            else -> controlPoints
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Speed curve canvas
        Text("变速曲线", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, CG.Line, RoundedCornerShape(8.dp))
        ) {
            SpeedCurveCanvas(
                controlPoints = controlPoints,
                onControlPointDrag = { index, newPos ->
                    val newPoints = controlPoints.toMutableList()
                    newPoints[index] = ControlPoint(
                        if (index == 0) 0f else if (index == controlPoints.size - 1) 1f
                        else newPos.x.coerceIn(0.05f, 0.95f),
                        newPos.y.coerceIn(0f, 1f)
                    )
                    controlPoints = newPoints
                    selectedPreset = "自定义"
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Speed labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${(speedMin / 100f).formatSpeed()}x", fontSize = 8.sp, color = CG.T3)
            Text("1.0x", fontSize = 8.sp, color = CG.T3)
            Text("${(speedMax / 100f).formatSpeed()}x", fontSize = 8.sp, color = CG.T3)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Preset curves
        Text("预设曲线", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presetCurves.forEach { preset ->
                OptionChip(preset, selectedPreset == preset) { applyPreset(preset) }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Speed range
        Text("速度范围", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("最低速度", 25, speedMin, 400) { speedMin = it.coerceAtMost(speedMax) }
        CgSlider("最高速度", 25, speedMax, 400) { speedMax = it.coerceAtLeast(speedMin) }
        Spacer(modifier = Modifier.height(14.dp))

        // Reverse playback toggle
        ToggleRow("倒放", reversePlayback) { reversePlayback = it }
        Spacer(modifier = Modifier.height(16.dp))

        // Apply button
        ApplyButton("应用变速曲线") {
            val clip = vm.selectedClip()
            if (clip != null) {
                val curvePoints = controlPoints.map { "${it.x}:${it.y}" }.joinToString(",")
                val params = mapOf<String, Any>(
                    "curve" to (selectedPreset),
                    "controlPoints" to curvePoints,
                    "speedMin" to speedMin / 100f,
                    "speedMax" to speedMax / 100f,
                    "reverse" to reversePlayback
                )
                val result = bridge.applyEffect("speed_curve", params)
                if (result) {
                    vm.showToast("变速曲线已应用: $selectedPreset")
                } else {
                    vm.showToast("应用变速曲线失败")
                }
            } else {
                vm.showToast("请先选择片段")
            }
            onClose()
        }
    }
}

private fun Float.formatSpeed(): String = "%.2f".format(this)

@Composable
private fun SpeedCurveCanvas(
    controlPoints: List<ControlPoint>,
    onControlPointDrag: (Int, ControlPoint) -> Unit
) {
    var dragIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(modifier = Modifier.fillMaxSize()
        .pointerInput(controlPoints) {
            detectDragGestures(
                onDragStart = { offset ->
                    val w = size.width; val h = size.height
                    controlPoints.forEachIndexed { idx, cp ->
                        val px = cp.x * w; val py = (1f - cp.y) * h
                        val dist = kotlin.math.sqrt((offset.x - px) * (offset.x - px) + (offset.y - py) * (offset.y - py))
                        if (dist < 20f) {
                            dragIndex = idx
                            return@detectDragGestures
                        }
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (dragIndex != null) {
                        val w = size.width; val h = size.height
                        val cp = controlPoints[dragIndex!!]
                        val newX = (cp.x + dragAmount.x / w)
                        val newY = (cp.y - dragAmount.y / h)
                        onControlPointDrag(dragIndex!!, ControlPoint(newX, newY))
                    }
                },
                onDragEnd = { dragIndex = null },
                onDragCancel = { dragIndex = null }
            )
        }
    ) {
        val w = size.width; val h = size.height

        // Grid lines
        for (i in 1..4) {
            val y = h * i / 5f
            drawLine(CG.Line.copy(alpha = 0.5f), Offset(0f, y), Offset(w, y), 1f)
        }
        for (i in 1..4) {
            val x = w * i / 5f
            drawLine(CG.Line.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, h), 1f)
        }

        // 1x baseline
        val baselineY = h * 0.5f
        drawLine(CG.T4, Offset(0f, baselineY), Offset(w, baselineY), 1f)

        // Draw speed curve
        if (controlPoints.size >= 2) {
            val sorted = controlPoints.sortedBy { it.x }
            val path = Path()
            path.moveTo(sorted[0].x * w, (1f - sorted[0].y) * h)
            for (i in 1 until sorted.size) {
                val prev = sorted[i - 1]; val curr = sorted[i]
                val prevX = prev.x * w; val prevY = (1f - prev.y) * h
                val currX = curr.x * w; val currY = (1f - curr.y) * h
                val midX = (prevX + currX) / 2f
                path.cubicTo(midX, prevY, midX, currY, currX, currY)
            }
            drawPath(path, CG.AccL, style = Stroke(width = 2.5f))

            // Fill under curve
            val fillPath = Path().apply {
                addPath(path)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(fillPath, CG.Acc.copy(alpha = 0.1f))
        }

        // Draw control points
        controlPoints.forEachIndexed { idx, cp ->
            val px = cp.x * w; val py = (1f - cp.y) * h
            val radius = if (idx == dragIndex) 8f else 6f
            drawCircle(CG.AccL, radius, Offset(px, py))
            drawCircle(Color.White, radius - 2f, Offset(px, py))
        }
    }
}
