package com.myvideo.editor.ui.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

private object CC {
    val Bg = Color(0xFF0A0A0A); val Surf = Color(0xFF111111); val Card = Color(0xFF181818)
    val CardH = Color(0xFF222222); val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val Acc2 = Color(0xFF6EC850); val Gold = Color(0xFFE8A820)
    val Green = Color(0xFF6EC850); val Red = Color(0xFFE84848)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4); val T3 = Color(0xFF6A6660)
    val T4 = Color(0xFF3A3A3A); val Line = Color(0xFF222222); val Line2 = Color(0xFF2A2A2A)
}

@Composable
fun ColorScreen(onBack: () -> Unit = {}) {
    var tab by remember { mutableStateOf("色轮") }
    var playing by remember { mutableStateOf(false) }
    var comparing by remember { mutableStateOf(false) }
    var splitPosition by remember { mutableFloatStateOf(0.5f) }
    var selectedPreset by remember { mutableStateOf("原图") }

    // Color wheel state
    var shadowOffset by remember { mutableStateOf(Offset.Zero) }
    var midOffset by remember { mutableStateOf(Offset.Zero) }
    var highOffset by remember { mutableStateOf(Offset.Zero) }
    var exposure by remember { mutableFloatStateOf(50f) }
    var contrast by remember { mutableFloatStateOf(50f) }
    var highlights by remember { mutableFloatStateOf(50f) }
    var shadows by remember { mutableFloatStateOf(50f) }
    var temperature by remember { mutableFloatStateOf(50f) }
    var tint by remember { mutableFloatStateOf(50f) }
    var saturation by remember { mutableFloatStateOf(50f) }

    // Curves state
    var curveChannel by remember { mutableStateOf("RGB") }
    var curvePoints by remember { mutableStateOf(listOf(Offset(0f, 1f), Offset(0.33f, 0.67f), Offset(0.66f, 0.33f), Offset(1f, 0f))) }

    // HSL state
    var hslChannel by remember { mutableStateOf("红") }
    var hslHue by remember { mutableFloatStateOf(50f) }
    var hslSat by remember { mutableFloatStateOf(50f) }
    var hslLum by remember { mutableFloatStateOf(50f) }

    // Levels state
    var inputBlack by remember { mutableFloatStateOf(0f) }
    var inputGamma by remember { mutableFloatStateOf(50f) }
    var inputWhite by remember { mutableFloatStateOf(100f) }

    Column(modifier = Modifier.fillMaxSize().background(CC.Bg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(CC.Card)
                .clickable { onBack() }, contentAlignment = Alignment.Center) { Text("‹", fontSize = 20.sp, color = CC.T2) }
            Text("调色", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CC.T1)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Compare button
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(if (comparing) CC.Acc.copy(0.2f) else CC.Card)
                    .clickable { comparing = !comparing }
                    .padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text("对比", fontSize = 10.sp, color = if (comparing) CC.Acc else CC.T3)
                }
                // Reset button
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CC.Card)
                    .clickable {
                        shadowOffset = Offset.Zero; midOffset = Offset.Zero; highOffset = Offset.Zero
                        exposure = 50f; contrast = 50f; highlights = 50f; shadows = 50f
                        temperature = 50f; tint = 50f; saturation = 50f
                        curvePoints = listOf(Offset(0f, 1f), Offset(0.33f, 0.67f), Offset(0.66f, 0.33f), Offset(1f, 0f))
                        inputBlack = 0f; inputGamma = 50f; inputWhite = 100f
                        selectedPreset = "原图"
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text("重置", fontSize = 10.sp, color = CC.T3)
                }
                // Apply button
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CC.Acc.copy(0.12f))
                    .clickable { onBack() }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text("应用", fontSize = 10.sp, color = CC.Acc, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Video preview with before/after split
        Box(modifier = Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp)).background(Color(0xFF050505))) {
            // Before (left side - desaturated)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                val splitX = w * splitPosition

                // "Before" side (left) - grayish
                drawRect(Color(0xFF2A2A2A), Offset(0f, 0f), Size(splitX, h))
                // "After" side (right) - with color tint
                val afterColor = when {
                    temperature > 55f -> Color(0xFF3A2A1A) // warm
                    temperature < 45f -> Color(0xFF1A2A3A) // cool
                    else -> Color(0xFF2A2A2A)
                }
                drawRect(afterColor, Offset(splitX, 0f), Size(w - splitX, h))

                // Split line
                drawLine(Color.White.copy(alpha = 0.8f), Offset(splitX, 0f), Offset(splitX, h), strokeWidth = 2f)
                // Split handle
                drawCircle(Color.White, 8.dp.toPx(), Offset(splitX, h / 2))
                drawLine(Color.White.copy(alpha = 0.5f), Offset(splitX - 4.dp.toPx(), h / 2), Offset(splitX + 4.dp.toPx(), h / 2), strokeWidth = 2f)

                // Labels
                if (comparing) {
                    // Full before view
                    drawRect(Color(0xFF2A2A2A), Offset(0f, 0f), Size(w, h))
                }
            }
            // Labels
            Text("原图", fontSize = 8.sp, color = CC.T3, modifier = Modifier.padding(4.dp))
            Text("效果", fontSize = 8.sp, color = CC.T3, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            Text("1920×1080", fontSize = 7.sp, color = CC.T4, fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))

            // Draggable split
            if (!comparing) {
                Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        splitPosition = (change.position.x / size.width).coerceIn(0.1f, 0.9f)
                    }
                })
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Tab row: 色轮 / 曲线 / HSL / 色阶 / 滤镜
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf("色轮", "曲线", "HSL", "色阶", "滤镜").forEach { t ->
                val on = tab == t
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(if (on) CC.Acc.copy(0.15f) else Color.Transparent)
                    .clickable { tab = t }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(t, fontSize = 11.sp, color = if (on) CC.Acc else CC.T3,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }

        // Content area
        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                when (tab) {
                    "色轮" -> WheelsPanel(shadowOffset, midOffset, highOffset, exposure, contrast, highlights, shadows, temperature, tint, saturation,
                        onShadowOffset = { shadowOffset = it }, onMidOffset = { midOffset = it }, onHighOffset = { highOffset = it },
                        onExposure = { exposure = it }, onContrast = { contrast = it }, onHighlights = { highlights = it }, onShadows = { shadows = it },
                        onTemperature = { temperature = it }, onTint = { tint = it }, onSaturation = { saturation = it })
                    "曲线" -> CurvesPanel(curveChannel, curvePoints,
                        onChannelChange = { curveChannel = it }, onPointsChange = { curvePoints = it })
                    "HSL" -> HslPanel(hslChannel, hslHue, hslSat, hslLum,
                        onChannelChange = { hslChannel = it }, onHueChange = { hslHue = it }, onSatChange = { hslSat = it }, onLumChange = { hslLum = it })
                    "色阶" -> LevelsPanel(inputBlack, inputGamma, inputWhite,
                        onInputBlack = { inputBlack = it }, onInputGamma = { inputGamma = it }, onInputWhite = { inputWhite = it })
                    "滤镜" -> FilterPanel(selectedPreset) { selectedPreset = it }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Preset strip at bottom
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .background(CC.Surf).padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("原图" to Color(0xFF666666), "暖阳" to Color(0xFFE8A830), "冷调" to Color(0xFF4A90D9),
                "胶片" to Color(0xFFA08060), "黑白" to Color(0xFF606060), "鲜艳" to Color(0xFFE84848),
                "复古" to Color(0xFFC0A060), "清透" to Color(0xFF80C8E0), "夜色" to Color(0xFF203040),
                "日系" to Color(0xFFE8E0D0), "黑金" to Color(0xFFE8A820), "青橙" to Color(0xFFE86A20)
            ).forEach { (name, clr) ->
                val sel = selectedPreset == name
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedPreset = name }) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(clr)
                        .then(if (sel) Modifier.border(2.dp, CC.Acc, RoundedCornerShape(6.dp)) else Modifier))
                    Text(name, fontSize = 7.sp, color = if (sel) CC.Acc else CC.T3,
                        modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

// ===== 色轮面板 =====
@Composable
private fun WheelsPanel(
    shadowOffset: Offset, midOffset: Offset, highOffset: Offset,
    exposure: Float, contrast: Float, highlights: Float, shadows: Float,
    temperature: Float, tint: Float, saturation: Float,
    onShadowOffset: (Offset) -> Unit, onMidOffset: (Offset) -> Unit, onHighOffset: (Offset) -> Unit,
    onExposure: (Float) -> Unit, onContrast: (Float) -> Unit, onHighlights: (Float) -> Unit, onShadows: (Float) -> Unit,
    onTemperature: (Float) -> Unit, onTint: (Float) -> Unit, onSaturation: (Float) -> Unit
) {
    Section("色轮") {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf("阴影" to shadowOffset to onShadowOffset, "中间调" to midOffset to onMidOffset, "高光" to highOffset to onHighOffset).forEach { (pair, onMove) ->
                val (label, offset) = pair
                var dotPos by remember(offset) { mutableStateOf(offset) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val c = Offset(size.width / 2f, size.height / 2f)
                                val dx = change.position.x - c.x; val dy = change.position.y - c.y
                                val r = size.width / 2f - 8.dp.toPx()
                                val dist = sqrt(dx * dx + dy * dy)
                                dotPos = if (dist <= r) Offset(dx, dy) else Offset(dx * r / dist, dy * r / dist)
                                onMove(dotPos)
                            }
                        }) {
                            val c = Offset(size.width / 2f, size.height / 2f)
                            val r = size.width / 2f - 8.dp.toPx()
                            for (angle in 0 until 360 step 5) {
                                val rad = Math.toRadians(angle.toDouble())
                                drawLine(Color.hsl(angle.toFloat(), 0.6f, 0.5f), c,
                                    Offset(c.x + r * cos(rad).toFloat(), c.y + r * sin(rad).toFloat()), strokeWidth = 6.dp.toPx())
                            }
                            drawCircle(Color(0xFF1A1A1A), r - 6.dp.toPx(), c)
                            drawCircle(Color.White.copy(0.7f), 4.dp.toPx(), Offset(c.x + dotPos.x, c.y + dotPos.y))
                        }
                    }
                    Text(label, fontSize = 9.sp, color = CC.T3, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
    Section("基础校正") {
        CcSlider("曝光", exposure, onExposure)
        CcSlider("对比度", contrast, onContrast)
        CcSlider("高光", highlights, onHighlights)
        CcSlider("阴影", shadows, onShadows)
    }
    Section("白平衡") {
        CcSlider("色温", temperature, onTemperature, listOf(Color(0xFF4488CC), Color(0xFFCC8844)))
        CcSlider("色调", tint, onTint, listOf(Color(0xFF44CC44), Color(0xFFCC44CC)))
    }
    Section("饱和度") {
        CcSlider("饱和度", saturation, onSaturation, listOf(Color(0xFF666666), CC.Acc2))
    }
}

// ===== 曲线面板 =====
@Composable
private fun CurvesPanel(channel: String, points: List<Offset>, onChannelChange: (String) -> Unit, onPointsChange: (List<Offset>) -> Unit) {
    var pts by remember(points) { mutableStateOf(points.toMutableList()) }
    Section("曲线") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("RGB" to Color.White, "R" to CC.Red, "G" to CC.Green, "B" to CC.Acc).forEach { (ch, clr) ->
                val on = channel == ch
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(if (on) CC.Acc.copy(0.15f) else CC.Card)
                    .clickable { onChannelChange(ch) }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(ch, fontSize = 10.sp, color = if (on) clr else CC.T3)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).background(CC.Card)) {
            Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val nx = (change.position.x / size.width).coerceIn(0f, 1f)
                    val ny = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    val closest = pts.indices.minByOrNull { val p = pts[it]; sqrt((p.x - nx).pow(2) + (p.y - ny).pow(2)) } ?: return@detectDragGestures
                    pts[closest] = Offset(nx, ny)
                    onPointsChange(pts.toList())
                }
            }) {
                val w = size.width; val h = size.height
                for (i in 1..3) { val x = w * i / 4; drawLine(CC.Line2, Offset(x, 0f), Offset(x, h), 1f)
                    val y = h * i / 4; drawLine(CC.Line2, Offset(0f, y), Offset(w, y), 1f) }
                drawLine(Color.White.copy(0.15f), Offset(0f, h), Offset(w, 0f), 1f)
                val path = Path()
                pts.forEachIndexed { i, p ->
                    val sx = p.x * w; val sy = (1f - p.y) * h
                    if (i == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
                }
                drawPath(path, CC.Acc, style = Stroke(width = 2.dp.toPx()))
                pts.forEach { p -> drawCircle(Color.White, 5.dp.toPx(), Offset(p.x * w, (1f - p.y) * h)) }
            }
        }
    }
    Section("预设") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("线性", "S曲线", "高对比", "低对比", "反相").forEach { preset ->
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CC.Card)
                    .clickable {
                        val newPts = when (preset) {
                            "线性" -> listOf(Offset(0f, 1f), Offset(0.33f, 0.67f), Offset(0.66f, 0.33f), Offset(1f, 0f))
                            "S曲线" -> listOf(Offset(0f, 1f), Offset(0.33f, 0.8f), Offset(0.66f, 0.2f), Offset(1f, 0f))
                            "高对比" -> listOf(Offset(0f, 1f), Offset(0.25f, 0.85f), Offset(0.75f, 0.15f), Offset(1f, 0f))
                            "低对比" -> listOf(Offset(0f, 1f), Offset(0.33f, 0.6f), Offset(0.66f, 0.4f), Offset(1f, 0f))
                            "反相" -> listOf(Offset(0f, 0f), Offset(0.33f, 0.33f), Offset(0.66f, 0.67f), Offset(1f, 1f))
                            else -> pts.toList()
                        }
                        pts = newPts.toMutableList()
                        onPointsChange(newPts)
                    }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(preset, fontSize = 10.sp, color = CC.T2)
                }
            }
        }
    }
}

// ===== HSL面板 =====
@Composable
private fun HslPanel(channel: String, hue: Float, sat: Float, lum: Float,
    onChannelChange: (String) -> Unit, onHueChange: (Float) -> Unit, onSatChange: (Float) -> Unit, onLumChange: (Float) -> Unit) {
    val colorEntries = listOf("红" to Color(0xFFEF5350), "橙" to Color(0xFFFF7043), "黄" to Color(0xFFFFCA28),
        "绿" to Color(0xFF66BB6A), "青" to Color(0xFF26C6DA), "蓝" to Color(0xFF42A5F5),
        "紫" to Color(0xFFAB47BC), "洋红" to Color(0xFFEC407A))
    Section("HSL 调整") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            colorEntries.forEach { (name, color) ->
                val sel = channel == name
                Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = if (sel) 0.8f else 0.2f))
                    .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(6.dp)) else Modifier)
                    .clickable { onChannelChange(name) },
                    contentAlignment = Alignment.Center) {
                    Text(name, fontSize = 8.sp, color = if (sel) Color.White else color)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        CcSlider("色相", hue, onHueChange)
        CcSlider("饱和度", sat, onSatChange)
        CcSlider("明度", lum, onLumChange)
    }
}

// ===== 色阶面板 =====
@Composable
private fun LevelsPanel(inputBlack: Float, inputGamma: Float, inputWhite: Float,
    onInputBlack: (Float) -> Unit, onInputGamma: (Float) -> Unit, onInputWhite: (Float) -> Unit) {
    Section("色阶") {
        // Histogram
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(6.dp)).background(CC.Card)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0 until 64) {
                    val h = (sin(i * 0.15) * 0.3 + 0.4 + sin(i * 0.3) * 0.2).coerceIn(0.05, 1.0)
                    drawRect(CC.T3, Offset(i * size.width / 64, size.height * (1 - h.toFloat())),
                        Size(size.width / 64 - 1, size.height * h.toFloat()))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            .background(Brush.linearGradient(listOf(Color.Black, Color(0xFF444444), Color.White))))
        Spacer(modifier = Modifier.height(8.dp))
        CcSlider("输入黑场", inputBlack, onInputBlack)
        CcSlider("灰点", inputGamma, onInputGamma)
        CcSlider("输入白场", inputWhite, onInputWhite)
    }
    Section("通道") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("总", "红", "绿", "蓝").forEach { ch ->
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CC.Card)
                    .clickable { }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(ch, fontSize = 10.sp, color = CC.T2)
                }
            }
        }
    }
}

// ===== 滤镜面板 =====
@Composable
private fun FilterPanel(selectedPreset: String, onPresetChange: (String) -> Unit) {
    var intensity by remember { mutableFloatStateOf(75f) }
    val luts = listOf("原图" to Color(0xFF888888), "暖阳" to Color(0xFFE8A830), "冷调" to Color(0xFF4A90D9),
        "胶片" to Color(0xFFA08060), "黑白" to Color(0xFF606060), "鲜艳" to Color(0xFFE84848),
        "复古" to Color(0xFFC0A060), "清透" to Color(0xFF80C8E0), "夜色" to Color(0xFF203040),
        "日系" to Color(0xFFE8E0D0), "黑金" to Color(0xFFE8A820), "青橙" to Color(0xFFE86A20))
    Section("滤镜预设") {
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            luts.forEach { (name, clr) ->
                val on = selectedPreset == name
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onPresetChange(name) }) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(clr)
                        .then(if (on) Modifier.border(2.dp, CC.Acc, RoundedCornerShape(6.dp)) else Modifier))
                    Text(name, fontSize = 8.sp, color = if (on) CC.Acc else CC.T3, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
    CcSlider("强度", intensity) { intensity = it }
}

// ===== Shared components =====
@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(title, fontSize = 10.sp, color = CC.T3, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        content()
    }
    Spacer(Modifier.height(4.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(CC.Line))
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun CcSlider(label: String, position: Float, onValueChange: (Float) -> Unit, colors: List<Color>? = null) {
    val cs = colors ?: listOf(CC.Acc, CC.Acc2)
    var pos by remember(position) { mutableStateOf(position) }
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = CC.T2, modifier = Modifier.width(56.dp))
        Canvas(Modifier.weight(1f).height(20.dp).pointerInput(Unit) {
            detectDragGestures { c, _ -> c.consume(); pos = (c.position.x / size.width * 100).coerceIn(0f, 100f); onValueChange(pos) }
        }) {
            val cy = size.height / 2; val w = size.width
            drawRoundRect(CC.Card, Offset(0f, cy - 2.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawRoundRect(Brush.linearGradient(cs), Offset(0f, cy - 2.dp.toPx()), Size(w * pos / 100, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawCircle(CC.T1, 5.dp.toPx(), Offset(w * pos / 100, cy))
        }
        Text("${((pos - 50f) * 2).toInt()}", fontSize = 10.sp, color = CC.T3, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}
