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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelsTab() {
    var channel by remember { mutableStateOf("RGB") }
    val channels = listOf("RGB" to CG.AccL, "R" to CG.Red, "G" to CG.Green, "B" to Color(0xFF4A90D9))
    var inputBlack by remember { mutableStateOf(0) }
    var inputGamma by remember { mutableStateOf(128) }
    var inputWhite by remember { mutableStateOf(255) }
    var outputBlack by remember { mutableStateOf(0) }
    var outputWhite by remember { mutableStateOf(255) }
    var autoMode by remember { mutableStateOf("手动") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("色阶", fontSize = 9.sp, color = CG.T4, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            channels.forEach { (ch, _) -> OptionChip(ch, channel == ch) { channel = ch } }
        }
        Spacer(modifier = Modifier.height(10.dp))

        val curveColor = channels.find { it.first == channel }?.second ?: CG.AccL
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width; val h = size.height
                for (i in 1..7) {
                    val x = w * i / 8f
                    drawLine(Color.White.copy(alpha = 0.04f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                }
                for (i in 1..3) {
                    val y = h * i / 4f
                    drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }
                val histData = listOf(0.1f, 0.2f, 0.35f, 0.6f, 0.8f, 1f, 0.9f, 0.7f, 0.5f, 0.8f, 1f, 0.85f, 0.6f, 0.4f, 0.25f, 0.15f)
                val barW = w / histData.size
                histData.forEachIndexed { i, v ->
                    val barH = v * h * 0.85f
                    drawRect(curveColor.copy(alpha = 0.5f), Offset(i * barW, h - barH), Size(barW - 1, barH))
                }
                val inStart = inputBlack / 255f * w
                val inEnd = inputWhite / 255f * w
                drawLine(Color.White.copy(alpha = 0.6f), Offset(inStart, 0f), Offset(inStart, h), strokeWidth = 2f)
                drawLine(Color.White.copy(alpha = 0.6f), Offset(inEnd, 0f), Offset(inEnd, h), strokeWidth = 2f)
                val gammaX = inputGamma / 255f * w
                drawCircle(Color.Yellow, 5f, Offset(gammaX, h / 2f))
                val outStart = outputBlack / 255f * w
                val outEnd = outputWhite / 255f * w
                drawLine(Color.Cyan.copy(alpha = 0.4f), Offset(outStart, 0f), Offset(outStart, h), strokeWidth = 1.5f)
                drawLine(Color.Cyan.copy(alpha = 0.4f), Offset(outEnd, 0f), Offset(outEnd, h), strokeWidth = 1.5f)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("输入色阶", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("黑场", 0, inputBlack, 255) { inputBlack = it }
        CgSlider("灰场", 1, inputGamma, 254) { inputGamma = it }
        CgSlider("白场", 0, inputWhite, 255) { inputWhite = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("输出色阶", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("最暗", 0, outputBlack, 255) { outputBlack = it }
        CgSlider("最亮", 0, outputWhite, 255) { outputWhite = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("自动调整", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("手动", "自动对比度", "自动色阶", "自动颜色").forEach {
                OptionChip(it, autoMode == it) { autoMode = it }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("重置", fontSize = 9.sp, color = CG.Acc, modifier = Modifier.clickable {
            inputBlack = 0; inputGamma = 128; inputWhite = 255
            outputBlack = 0; outputWhite = 255; autoMode = "手动"
        })
    }
}
