package com.myvideo.editor.ui.editor.panels

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun FilmPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var selectedPreset by remember { mutableStateOf("无") }
    var grainAmount by remember { mutableStateOf(30) }
    var grainSize by remember { mutableStateOf(50) }
    var grainSoftness by remember { mutableStateOf(50) }
    var flickerIntensity by remember { mutableStateOf(0) }
    var flickerSpeed by remember { mutableStateOf(50) }
    var dustAmount by remember { mutableStateOf(0) }
    var scratchAmount by remember { mutableStateOf(0) }
    var vignetteAmount by remember { mutableStateOf(20) }
    var fadeAmount by remember { mutableStateOf(0) }
    var colorShift by remember { mutableStateOf(0) }
    var gateWeave by remember { mutableStateOf(0) }
    var bleachBypass by remember { mutableStateOf(0) }

    data class FilmStock(val name: String, val c1: Color, val c2: Color)
    val stocks = listOf(
        FilmStock("无", Color(0xFF2C2C2C), Color(0xFF2C2C2C)),
        FilmStock("柯达5219", Color(0xFF8B6914), Color(0xFFD4A54A)),
        FilmStock("富士Eterna", Color(0xFF1A4A3A), Color(0xFF4A8A7A)),
        FilmStock("柯达Vision3", Color(0xFF6B3A0A), Color(0xFFAA6A2A)),
        FilmStock("依尔福HP5", Color(0xFF333333), Color(0xFF666666)),
        FilmStock("柯达Tri-X", Color(0xFF2A2A1A), Color(0xFF5A5A3A)),
        FilmStock("富士Provia", Color(0xFF1A2A4A), Color(0xFF4A6A9A)),
        FilmStock("柯达Portra", Color(0xFFAA7A5A), Color(0xFFD4AA8A)),
        FilmStock("Lomography", Color(0xFF6A1A0A), Color(0xFFEA6A2A)),
        FilmStock("超8mm", Color(0xFF4A3A0A), Color(0xFF8A7A3A)),
        FilmStock("16mm新闻", Color(0xFF2A2A2A), Color(0xFF5A4A2A)),
        FilmStock("35mm经典", Color(0xFF3A2A1A), Color(0xFF7A5A3A)),
        FilmStock("宝丽来", Color(0xFFE8D8C0), Color(0xFFF0E8D8)),
        FilmStock("达盖尔", Color(0xFF1A1A0A), Color(0xFF4A3A1A)),
        FilmStock("湿版", Color(0xFF2A1A0A), Color(0xFF5A4A2A)),
        FilmStock("银版", Color(0xFF8A8A8A), Color(0xFFB0B0B0))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("胶片颗粒", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("模拟真实胶片的质感与颗粒", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(10.dp))

        Text("胶片型号", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        stocks.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { stock ->
                    val sel = selectedPreset == stock.name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(stock.c1, stock.c2)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { selectedPreset = stock.name },
                            contentAlignment = Alignment.Center) {
                            if (stock.name == "无") Text("无", fontSize = 8.sp, color = CG.T2)
                        }
                        Text(stock.name, fontSize = 6.sp, color = if (sel) CG.AccL else CG.T3,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                }
                repeat(4 - row.size) {
                    Column { Spacer(modifier = Modifier.weight(1f).fillMaxWidth().height(40.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("颗粒参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("颗粒量", 0, grainAmount, 100) { grainAmount = it }
        CgSlider("颗粒大小", 10, grainSize, 100) { grainSize = it }
        CgSlider("颗粒柔和", 0, grainSoftness, 100) { grainSoftness = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("闪烁与抖动", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("闪烁强度", 0, flickerIntensity, 100) { flickerIntensity = it }
        CgSlider("闪烁速度", 1, flickerSpeed, 100) { flickerSpeed = it }
        CgSlider("画面抖动", 0, gateWeave, 50) { gateWeave = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("划痕与灰尘", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("灰尘量", 0, dustAmount, 100) { dustAmount = it }
        CgSlider("划痕量", 0, scratchAmount, 100) { scratchAmount = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("色彩衰减", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("褪色", 0, fadeAmount, 100) { fadeAmount = it }
        CgSlider("色偏", 0, colorShift, 100) { colorShift = it }
        CgSlider("漂白效果", 0, bleachBypass, 100) { bleachBypass = it }
        CgSlider("暗角", 0, vignetteAmount, 100) { vignetteAmount = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("预设", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("轻微", "中等", "重度", "超8复古", "默片时代").forEach { preset ->
                OptionChip(preset, false) {
                    when (preset) {
                        "轻微" -> { grainAmount = 15; flickerIntensity = 5; dustAmount = 5; scratchAmount = 3 }
                        "中等" -> { grainAmount = 40; flickerIntensity = 15; dustAmount = 15; scratchAmount = 10 }
                        "重度" -> { grainAmount = 70; flickerIntensity = 30; dustAmount = 30; scratchAmount = 25 }
                        "超8复古" -> { grainAmount = 60; flickerIntensity = 40; dustAmount = 25; scratchAmount = 20; fadeAmount = 30; colorShift = 20 }
                        "默片时代" -> { grainAmount = 80; flickerIntensity = 50; dustAmount = 40; scratchAmount = 35; fadeAmount = 60; gateWeave = 15 }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用胶片效果") {
            if (grainAmount > 0) {
                bridge.applyFilmGrain(vm, grainAmount.toFloat(),
                    onComplete = { vm.showToast("胶片颗粒已应用: $selectedPreset") },
                    onError = { vm.showToast("应用失败: $it") })
            } else {
                vm.showToast("请调整颗粒参数")
            }
            onClose()
        }
    }
}
