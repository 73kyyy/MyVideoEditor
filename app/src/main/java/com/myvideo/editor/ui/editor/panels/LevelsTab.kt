package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun LevelsTab(vm: EditorViewModel, bridge: EditorBridge) {
    val trackId = vm.selectedClipId ?: "default"
    var channel by remember { mutableStateOf("RGB") }
    val channels = listOf("RGB" to CG.AccL, "R" to CG.Red, "G" to CG.Green, "B" to Color(0xFF4A90D9))
    var inputBlack by remember { mutableStateOf(0) }
    var inputGamma by remember { mutableStateOf(128) }
    var inputWhite by remember { mutableStateOf(255) }
    var outputBlack by remember { mutableStateOf(0) }
    var outputWhite by remember { mutableStateOf(255) }

    // Generate pseudo-histogram data that changes with channel
    val histData = remember(channel) {
        val r = Random(channel.hashCode())
        val peak = when (channel) {
            "R" -> 0.3f; "G" -> 0.5f; "B" -> 0.7f; else -> 0.4f
        }
        (0 until 32).map { i ->
            val x = i / 31f
            val gaussian = 0.6f * kotlin.math.exp(-((x - peak).pow(2)) / 0.08f)
            val base = 0.1f + 0.15f * kotlin.math.sin(x * Math.PI.toFloat())
            (gaussian + base + r.nextFloat() * 0.08f).coerceIn(0f, 1f)
        }
    }

    fun pushAll() {
        bridge.setTrackProperty(trackId, "input_black", inputBlack.toFloat())
        bridge.setTrackProperty(trackId, "input_gamma", inputGamma.toFloat())
        bridge.setTrackProperty(trackId, "input_white", inputWhite.toFloat())
        bridge.setTrackProperty(trackId, "output_black", outputBlack.toFloat())
        bridge.setTrackProperty(trackId, "output_white", outputWhite.toFloat())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("色阶", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            channels.forEach { (ch, _) -> OptionChip(ch, channel == ch) { channel = ch } }
        }
        Spacer(modifier = Modifier.height(10.dp))

        val curveColor = channels.find { it.first == channel }?.second ?: CG.AccL
        Box(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width; val h = size.height
                // Grid lines
                for (i in 1..7) {
                    val x = w * i / 8f
                    drawLine(Color.White.copy(alpha = 0.04f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                }
                for (i in 1..3) {
                    val y = h * i / 4f
                    drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }
                // Histogram bars
                val barW = w / histData.size
                histData.forEachIndexed { i, v ->
                    val barH = v * h * 0.85f
                    drawRect(curveColor.copy(alpha = 0.45f), Offset(i * barW, h - barH), Size(barW - 1, barH))
                }
                // Input level markers (white lines)
                val inStart = inputBlack / 255f * w
                val inEnd = inputWhite / 255f * w
                drawLine(Color.White.copy(alpha = 0.7f), Offset(inStart, 0f), Offset(inStart, h), strokeWidth = 2f)
                drawLine(Color.White.copy(alpha = 0.7f), Offset(inEnd, 0f), Offset(inEnd, h), strokeWidth = 2f)
                // Gamma marker (yellow triangle)
                val gammaX = inputGamma / 255f * w
                drawPath(Path().apply {
                    moveTo(gammaX, h - 8f)
                    lineTo(gammaX - 5f, h)
                    lineTo(gammaX + 5f, h)
                    close()
                }, color = Color.Yellow)
                // Output level markers (cyan lines)
                val outStart = outputBlack / 255f * w
                val outEnd = outputWhite / 255f * w
                drawLine(Color.Cyan.copy(alpha = 0.5f), Offset(outStart, 0f), Offset(outStart, h), strokeWidth = 1.5f)
                drawLine(Color.Cyan.copy(alpha = 0.5f), Offset(outEnd, 0f), Offset(outEnd, h), strokeWidth = 1.5f)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("输入色阶", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$inputBlack", fontSize = 8.sp, color = CG.T3)
            Text("${inputGamma / 100.0}", fontSize = 8.sp, color = Color.Yellow.copy(alpha = 0.7f))
            Text("$inputWhite", fontSize = 8.sp, color = CG.T3)
        }
        CgSlider("黑场", 0, inputBlack, 255) { inputBlack = it; pushAll() }
        CgSlider("灰场 (Gamma)", 1, inputGamma, 254) { inputGamma = it; pushAll() }
        CgSlider("白场", 1, inputWhite, 255) { inputWhite = it; pushAll() }
        Spacer(modifier = Modifier.height(10.dp))

        Text("输出色阶", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$outputBlack", fontSize = 8.sp, color = CG.T3)
            Text("$outputWhite", fontSize = 8.sp, color = CG.T3)
        }
        CgSlider("最暗", 0, outputBlack, 255) { outputBlack = it; pushAll() }
        CgSlider("最亮", 0, outputWhite, 255) { outputWhite = it; pushAll() }
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("自动色阶", fontSize = 9.sp, color = CG.Acc, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    // Auto levels: set black/white to histogram 1st/99th percentile
                    val sorted = histData.mapIndexed { i, v -> i to v }.sortedBy { it.second }
                    val p1Idx = sorted[(sorted.size * 0.05).toInt()].first
                    val p99Idx = sorted[(sorted.size * 0.95).toInt()].first
                    inputBlack = (p1Idx / 31f * 255).toInt().coerceIn(0, 255)
                    inputWhite = (p99Idx / 31f * 255).toInt().coerceIn(1, 255)
                    inputGamma = 128
                    pushAll()
                })
            Text("重置", fontSize = 9.sp, color = CG.Acc, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    inputBlack = 0; inputGamma = 128; inputWhite = 255
                    outputBlack = 0; outputWhite = 255
                    pushAll()
                })
        }
    }
}
