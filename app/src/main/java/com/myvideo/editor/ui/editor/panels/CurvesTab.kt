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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CurvesTab() {
    var selectedChannel by remember { mutableStateOf("RGB") }
    val channels = listOf("RGB" to CG.AccL, "R" to CG.Red, "G" to CG.Green, "B" to Color(0xFF4A90D9))
    var p0 by remember { mutableStateOf(Offset(0f, 1f)) }
    var p1 by remember { mutableStateOf(Offset(0.3f, 0.7f)) }
    var p2 by remember { mutableStateOf(Offset(0.65f, 0.3f)) }
    var p3 by remember { mutableStateOf(Offset(1f, 0f)) }
    var sCurve by remember { mutableStateOf("无") }
    var gamma by remember { mutableStateOf(10) }
    var highlightFade by remember { mutableStateOf(0) }
    var shadowLift by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("RGB 曲线", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            channels.forEach { (ch, _) -> OptionChip(ch, selectedChannel == ch) { selectedChannel = ch } }
        }
        Spacer(modifier = Modifier.height(10.dp))
        val curveColor = channels.find { it.first == selectedChannel }?.second ?: CG.AccL
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                    val y = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    val dist1 = kotlin.math.abs(x - p1.x)
                    val dist2 = kotlin.math.abs(x - p2.x)
                    if (dist1 < dist2) p1 = Offset(x, y) else p2 = Offset(x, y)
                }
            }) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val w = size.width; val h = size.height
                for (i in 1..3) {
                    drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, h * i / 4f), Offset(w, h * i / 4f), strokeWidth = 1f)
                    drawLine(Color.White.copy(alpha = 0.05f), Offset(w * i / 4f, 0f), Offset(w * i / 4f, h), strokeWidth = 1f)
                }
                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, h), Offset(w, 0f), strokeWidth = 1f)
                drawPath(Path().apply {
                    moveTo(p0.x * w, (1f - p0.y) * h)
                    cubicTo(p1.x * w, (1f - p1.y) * h, p2.x * w, (1f - p2.y) * h, p3.x * w, (1f - p3.y) * h)
                }, color = curveColor, style = Stroke(3f))
                listOf(p0, p1, p2, p3).forEach { p ->
                    val cp = Offset(p.x * w, (1f - p.y) * h)
                    drawCircle(Color.White, 7f, cp)
                    drawCircle(curveColor, 5f, cp)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("S曲线预设", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("无", "柔和", "中等", "强烈", "胶片", "高对比").forEach {
                OptionChip(it, sCurve == it) {
                    sCurve = it
                    when (it) {
                        "柔和" -> { p1 = Offset(0.3f, 0.65f); p2 = Offset(0.65f, 0.35f) }
                        "中等" -> { p1 = Offset(0.25f, 0.7f); p2 = Offset(0.7f, 0.25f) }
                        "强烈" -> { p1 = Offset(0.2f, 0.8f); p2 = Offset(0.8f, 0.15f) }
                        "胶片" -> { p1 = Offset(0.15f, 0.55f); p2 = Offset(0.75f, 0.3f) }
                        "高对比" -> { p1 = Offset(0.35f, 0.8f); p2 = Offset(0.75f, 0.1f) }
                        else -> { p1 = Offset(0.3f, 0.7f); p2 = Offset(0.65f, 0.3f) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        CgSlider("Gamma", 1, gamma, 30) { gamma = it }
        CgSlider("高光衰减", 0, highlightFade, 100) { highlightFade = it }
        CgSlider("暗部提升", 0, shadowLift, 100) { shadowLift = it }
        Spacer(modifier = Modifier.height(8.dp))
        Text("重置", fontSize = 9.sp, color = CG.Acc, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            modifier = Modifier.clickable {
                p0 = Offset(0f, 1f); p1 = Offset(0.3f, 0.7f)
                p2 = Offset(0.65f, 0.3f); p3 = Offset(1f, 0f)
                sCurve = "无"; gamma = 10; highlightFade = 0; shadowLift = 0
            })
    }
}
