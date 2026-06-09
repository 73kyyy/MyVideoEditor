package com.myvideo.editor.ui.audio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

private object AC {
    val Bg = Color(0xFF0A0A0A); val Surf = Color(0xFF111111); val Card = Color(0xFF181818)
    val CardH = Color(0xFF222222); val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val Acc2 = Color(0xFF6EC850); val Gold = Color(0xFFE8A820)
    val Green = Color(0xFF6EC850); val Red = Color(0xFFE84848)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4); val T3 = Color(0xFF6A6660)
    val T4 = Color(0xFF3A3A3A); val Line = Color(0xFF222222)
}

@Composable
fun AudioScreen(onBack: () -> Unit = {}) {
    var selectedTrack by remember { mutableStateOf("原声") }
    var playing by remember { mutableStateOf(false) }
    var playProgress by remember { mutableFloatStateOf(0.35f) }

    // Per-track state
    var originalVolume by remember { mutableFloatStateOf(80f) }
    var bgMusicVolume by remember { mutableFloatStateOf(60f) }
    var sfxVolume by remember { mutableFloatStateOf(70f) }
    var originalFadeIn by remember { mutableFloatStateOf(0f) }
    var originalFadeOut by remember { mutableFloatStateOf(10f) }
    var bgFadeIn by remember { mutableFloatStateOf(5f) }
    var bgFadeOut by remember { mutableFloatStateOf(15f) }
    var originalSpeed by remember { mutableFloatStateOf(1.0f) }
    var bgSpeed by remember { mutableFloatStateOf(1.0f) }

    // Volume envelope points
    var envelopePoints by remember { mutableStateOf(listOf(0f to 80f, 0.3f to 90f, 0.6f to 60f, 1f to 75f)) }

    // Effects
    var selectedEqPreset by remember { mutableStateOf("平坦") }
    var reverbAmount by remember { mutableFloatStateOf(0f) }
    var echoAmount by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize().background(AC.Bg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AC.Card)
                .clickable { onBack() }, contentAlignment = Alignment.Center) { Text("‹", fontSize = 20.sp, color = AC.T2) }
            Text("音频编辑", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AC.T1)
            Text("应用", fontSize = 12.sp, color = AC.Acc, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(AC.Acc.copy(0.12f))
                    .clickable { onBack() }.padding(horizontal = 12.dp, vertical = 6.dp))
        }

        // Video preview (small)
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp)).background(Color(0xFF050505)),
            contentAlignment = Alignment.Center) {
            Text("1920×1080 · 30fps", fontSize = 9.sp, color = AC.T3, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Audio waveform display
        Box(modifier = Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp)).background(AC.Card)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0 until 250) {
                    val h = (sin(i * 0.06) * 0.35 + 0.4 + sin(i * 0.18) * 0.2).coerceIn(0.05, 1.0)
                    val progress = i / 250f
                    val color = when {
                        progress < playProgress -> AC.Acc
                        else -> Color(0xFF444444)
                    }
                    drawRect(color, Offset(i * size.width / 250, size.height * (1 - h.toFloat()) / 2),
                        Size(size.width / 250 - 1, size.height * h.toFloat()))
                }
                // Playhead
                drawLine(Color.White, Offset(size.width * playProgress, 0f),
                    Offset(size.width * playProgress, size.height), strokeWidth = 2f)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Volume envelope
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(6.dp)).background(AC.Card)) {
            Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val nx = (change.position.x / size.width).coerceIn(0f, 1f)
                    val ny = (1f - change.position.y / size.height).coerceIn(0f, 1f) * 100f
                    val closest = envelopePoints.indices.minByOrNull {
                        val p = envelopePoints[it]; abs(p.first - nx)
                    } ?: return@detectDragGestures
                    envelopePoints = envelopePoints.toMutableList().also { it[closest] = nx to ny }
                }
            }) {
                val w = size.width; val h = size.height
                // Grid
                for (i in 1..3) {
                    val y = h * i / 4f
                    drawLine(AC.T4.copy(alpha = 0.3f), Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
                }
                // Envelope curve
                val path = Path()
                envelopePoints.forEachIndexed { i, p ->
                    val x = p.first * w; val y = h * (1f - p.second / 100f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, AC.Green, style = Stroke(width = 2.dp.toPx()))
                // Points
                envelopePoints.forEach { p ->
                    drawCircle(Color.White, 4.dp.toPx(), Offset(p.first * w, h * (1f - p.second / 100f)))
                }
            }
            Text("音量包络", fontSize = 7.sp, color = AC.T3, modifier = Modifier.padding(4.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Track selection: 原声 / 背景音乐 / 音效
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("原声", "背景音乐", "音效").forEach { track ->
                val sel = selectedTrack == track
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp))
                    .background(if (sel) AC.Acc.copy(0.15f) else AC.Card)
                    .then(if (sel) Modifier.border(1.dp, AC.Acc, RoundedCornerShape(6.dp)) else Modifier)
                    .clickable { selectedTrack = track }
                    .padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(track, fontSize = 11.sp, color = if (sel) AC.Acc else AC.T3,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Per-track controls
        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                when (selectedTrack) {
                    "原声" -> {
                        TrackSection("原声控制") {
                            VolumeSlider("音量", originalVolume) { originalVolume = it }
                            FadeSlider("淡入", originalFadeIn) { originalFadeIn = it }
                            FadeSlider("淡出", originalFadeOut) { originalFadeOut = it }
                            SpeedSlider("速度", originalSpeed) { originalSpeed = it }
                        }
                    }
                    "背景音乐" -> {
                        TrackSection("背景音乐控制") {
                            VolumeSlider("音量", bgMusicVolume) { bgMusicVolume = it }
                            FadeSlider("淡入", bgFadeIn) { bgFadeIn = it }
                            FadeSlider("淡出", bgFadeOut) { bgFadeOut = it }
                            SpeedSlider("速度", bgSpeed) { bgSpeed = it }
                        }
                    }
                    "音效" -> {
                        TrackSection("音效控制") {
                            VolumeSlider("音量", sfxVolume) { sfxVolume = it }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Sep()

                // AI features section
                SectionTitle("AI 功能")
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AiButton("降噪", AC.Green) { /* denoise */ }
                    AiButton("人声分离", AC.Acc) { /* separate vocals */ }
                    AiButton("语音转字幕", AC.Gold) { /* speech to text */ }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Sep()

                // Audio effects
                SectionTitle("音频效果")
                Spacer(modifier = Modifier.height(6.dp))
                // EQ presets
                Text("均衡器预设", fontSize = 9.sp, color = AC.T4)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("平坦", "流行", "摇滚", "古典", "低音增强", "人声增强").forEach { preset ->
                        val sel = selectedEqPreset == preset
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(if (sel) AC.Acc.copy(0.15f) else AC.Card)
                            .clickable { selectedEqPreset = preset }
                            .padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(preset, fontSize = 9.sp, color = if (sel) AC.Acc else AC.T2)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                EffectSlider("混响", reverbAmount) { reverbAmount = it }
                EffectSlider("回声", echoAmount) { echoAmount = it }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Playback bar
        Row(modifier = Modifier.fillMaxWidth().height(40.dp).background(AC.Surf).border(1.dp, AC.Line)
            .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text("00:12", fontSize = 10.sp, color = AC.T2, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.width(8.dp))
            listOf("[<<]", "[<]", if (playing) "||" else ">", "[>]", "[>>]").forEach { i ->
                val main = i == ">" || i == "||"
                Box(modifier = Modifier.size(if (main) 36.dp else 30.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (main) AC.Acc.copy(0.2f) else Color.Transparent)
                    .clickable { if (main) playing = !playing },
                    contentAlignment = Alignment.Center) {
                    Text(i, fontSize = if (main) 16.sp else 11.sp, color = AC.T1)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("01:24", fontSize = 10.sp, color = AC.T3, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 10.sp, color = AC.T3, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun Sep() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AC.Line))
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun TrackSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AC.Card).padding(10.dp)) {
        Text(title, fontSize = 10.sp, color = AC.T3, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun VolumeSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    var pos by remember { mutableStateOf(value) }
    LaunchedEffect(value) { pos = value }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = AC.T2, modifier = Modifier.width(48.dp))
        Canvas(modifier = Modifier.weight(1f).height(20.dp).pointerInput(Unit) {
            detectDragGestures { c, _ -> c.consume(); pos = (c.position.x / size.width * 100).coerceIn(0f, 100f); onValueChange(pos) }
        }) {
            val cy = size.height / 2; val w = size.width
            drawRoundRect(AC.T4, Offset(0f, cy - 2.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawRoundRect(Brush.linearGradient(listOf(AC.Green, AC.Gold, AC.Red)),
                Offset(0f, cy - 2.dp.toPx()), Size(w * pos / 100, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawCircle(AC.T1, 5.dp.toPx(), Offset(w * pos / 100, cy))
        }
        Text("${pos.toInt()}%", fontSize = 10.sp, color = AC.T3, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun FadeSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    var pos by remember { mutableStateOf(value) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = AC.T2, modifier = Modifier.width(48.dp))
        Canvas(modifier = Modifier.weight(1f).height(20.dp).pointerInput(Unit) {
            detectDragGestures { c, _ -> c.consume(); pos = (c.position.x / size.width * 30).coerceIn(0f, 30f); onValueChange(pos) }
        }) {
            val cy = size.height / 2; val w = size.width
            drawRoundRect(AC.T4, Offset(0f, cy - 2.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawRoundRect(AC.Acc2, Offset(0f, cy - 2.dp.toPx()), Size(w * pos / 30, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawCircle(AC.T1, 5.dp.toPx(), Offset(w * pos / 30, cy))
        }
        Text("${pos.toInt()}秒", fontSize = 10.sp, color = AC.T3, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun SpeedSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    var pos by remember { mutableStateOf(value) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = AC.T2, modifier = Modifier.width(48.dp))
        Canvas(modifier = Modifier.weight(1f).height(20.dp).pointerInput(Unit) {
            detectDragGestures { c, _ -> c.consume(); pos = (0.25f + c.position.x / size.width * 3.75f).coerceIn(0.25f, 4f); onValueChange(pos) }
        }) {
            val cy = size.height / 2; val w = size.width
            val norm = (pos - 0.25f) / 3.75f
            drawRoundRect(AC.T4, Offset(0f, cy - 2.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawRoundRect(AC.Gold, Offset(0f, cy - 2.dp.toPx()), Size(w * norm, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawCircle(AC.T1, 5.dp.toPx(), Offset(w * norm, cy))
        }
        Text("${"%.1f".format(pos)}x", fontSize = 10.sp, color = AC.T3, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun EffectSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    var pos by remember { mutableStateOf(value) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = AC.T2, modifier = Modifier.width(48.dp))
        Canvas(modifier = Modifier.weight(1f).height(20.dp).pointerInput(Unit) {
            detectDragGestures { c, _ -> c.consume(); pos = (c.position.x / size.width * 100).coerceIn(0f, 100f); onValueChange(pos) }
        }) {
            val cy = size.height / 2; val w = size.width
            drawRoundRect(AC.T4, Offset(0f, cy - 2.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawRoundRect(AC.Acc, Offset(0f, cy - 2.dp.toPx()), Size(w * pos / 100, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
            drawCircle(AC.T1, 5.dp.toPx(), Offset(w * pos / 100, cy))
        }
        Text("${pos.toInt()}%", fontSize = 10.sp, color = AC.T3, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun AiButton(label: String, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(color.copy(0.1f))
        .border(1.dp, color.copy(0.3f), RoundedCornerShape(8.dp))
        .clickable { onClick() }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}
