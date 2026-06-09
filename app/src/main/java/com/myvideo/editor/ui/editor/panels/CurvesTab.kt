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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun CurvesTab(vm: EditorViewModel, bridge: EditorBridge) {
    val trackId = vm.selectedClipId ?: "default"
    var selectedChannel by remember { mutableStateOf("RGB") }
    val channels = listOf("RGB" to CG.AccL, "R" to CG.Red, "G" to CG.Green, "B" to Color(0xFF4A90D9))

    // 4 control points: start, shadows, highlights, end
    var p0 by remember { mutableStateOf(Offset(0f, 1f)) }
    var p1 by remember { mutableStateOf(Offset(0.25f, 0.75f)) }   // shadows
    var p2 by remember { mutableStateOf(Offset(0.5f, 0.5f)) }     // midtones
    var p3 by remember { mutableStateOf(Offset(0.75f, 0.25f)) }   // highlights
    var p4 by remember { mutableStateOf(Offset(1f, 0f)) }
    var activePreset by remember { mutableStateOf("线性") }

    // Push curve changes to EditorBridge
    fun pushCurve() {
        val prefix = when (selectedChannel) {
            "R" -> "curves_r"
            "G" -> "curves_g"
            "B" -> "curves_b"
            else -> "curves_rgb"
        }
        bridge.setTrackProperty(trackId, "${prefix}_s_y", p1.y)
        bridge.setTrackProperty(trackId, "${prefix}_m_y", p2.y)
        bridge.setTrackProperty(trackId, "${prefix}_h_y", p3.y)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("RGB 曲线", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            channels.forEach { (ch, _) -> OptionChip(ch, selectedChannel == ch) { selectedChannel = ch } }
        }
        Spacer(modifier = Modifier.height(10.dp))

        val curveColor = channels.find { it.first == selectedChannel }?.second ?: CG.AccL
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .pointerInput(selectedChannel) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                    val y = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    // Find nearest draggable point (exclude endpoints)
                    val distances = listOf(
                        Triple(1, kotlin.math.abs(x - p1.x), Offset(x.coerceIn(0f, 0.49f), y)),
                        Triple(2, kotlin.math.abs(x - p2.x), Offset(x.coerceIn(0.01f, 0.99f), y)),
                        Triple(3, kotlin.math.abs(x - p3.x), Offset(x.coerceIn(0.51f, 1f), y))
                    )
                    val nearest = distances.minByOrNull { it.second }!!
                    when (nearest.first) {
                        1 -> p1 = nearest.third
                        2 -> p2 = nearest.third
                        3 -> p3 = nearest.third
                    }
                    activePreset = "自定义"
                    pushCurve()
                }
            }) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val w = size.width; val h = size.height
                // Grid
                for (i in 1..3) {
                    drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, h * i / 4f), Offset(w, h * i / 4f), strokeWidth = 1f)
                    drawLine(Color.White.copy(alpha = 0.06f), Offset(w * i / 4f, 0f), Offset(w * i / 4f, h), strokeWidth = 1f)
                }
                // Diagonal reference line
                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, h), Offset(w, 0f), strokeWidth = 1f)
                // Curve path using cubic bezier through control points
                drawPath(Path().apply {
                    moveTo(p0.x * w, (1f - p0.y) * h)
                    cubicTo(p1.x * w, (1f - p1.y) * h, p2.x * w, (1f - p2.y) * h, p4.x * w, (1f - p4.y) * h)
                }, color = curveColor, style = Stroke(3f))
                // Control points
                listOf(p1, p2, p3).forEach { p ->
                    val cp = Offset(p.x * w, (1f - p.y) * h)
                    drawCircle(Color.White, 7f, cp)
                    drawCircle(curveColor, 5f, cp)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("曲线预设", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("线性", "S曲线", "高对比", "低对比", "反相").forEach { preset ->
                OptionChip(preset, activePreset == preset) {
                    activePreset = preset
                    when (preset) {
                        "线性" -> { p1 = Offset(0.25f, 0.75f); p2 = Offset(0.5f, 0.5f); p3 = Offset(0.75f, 0.25f) }
                        "S曲线" -> { p1 = Offset(0.25f, 0.85f); p2 = Offset(0.5f, 0.5f); p3 = Offset(0.75f, 0.15f) }
                        "高对比" -> { p1 = Offset(0.2f, 0.9f); p2 = Offset(0.5f, 0.5f); p3 = Offset(0.8f, 0.1f) }
                        "低对比" -> { p1 = Offset(0.25f, 0.65f); p2 = Offset(0.5f, 0.5f); p3 = Offset(0.75f, 0.35f) }
                        "反相" -> { p1 = Offset(0.25f, 0.25f); p2 = Offset(0.5f, 0.5f); p3 = Offset(0.75f, 0.75f) }
                    }
                    pushCurve()
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Per-point value display
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("暗部: ${(p1.y * 255).toInt()}", fontSize = 8.sp, color = CG.T3)
            Text("中间调: ${(p2.y * 255).toInt()}", fontSize = 8.sp, color = CG.T3)
            Text("高光: ${(p3.y * 255).toInt()}", fontSize = 8.sp, color = CG.T3)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("重置", fontSize = 9.sp, color = CG.Acc, fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable {
                p0 = Offset(0f, 1f); p1 = Offset(0.25f, 0.75f)
                p2 = Offset(0.5f, 0.5f); p3 = Offset(0.75f, 0.25f)
                p4 = Offset(1f, 0f); activePreset = "线性"
                pushCurve()
            })
    }
}
