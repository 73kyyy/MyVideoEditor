package com.myvideo.editor.ui.export

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private object EC {
    val Bg = Color(0xFF080808); val Surf = Color(0xFF111111); val Card = Color(0xFF161616)
    val CardAlt = Color(0xFF1E1E1E); val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val AccS = Color(0x1F4A90D9); val Green = Color(0xFF6EC850); val Gold = Color(0xFFE8A820)
    val Red = Color(0xFFE84848)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4); val T3 = Color(0xFF6A6660)
    val T4 = Color(0xFF3A3A3A); val Line = Color(0xFF1A1A1A); val Line2 = Color(0xFF242424)
}

@Composable
fun ExportSettingsScreen(onBack: () -> Unit = {}, onExport: () -> Unit = {}) {
    var resolution by remember { mutableStateOf("1080p") }
    var fps by remember { mutableStateOf("30") }
    var codec by remember { mutableStateOf("H.264") }
    var crf by remember { mutableStateOf(23f) }
    var audioCodec by remember { mutableStateOf("AAC") }
    var audioBitrate by remember { mutableStateOf("320k") }
    var format by remember { mutableStateOf("MP4") }
    var showAdvanced by remember { mutableStateOf(false) }
    var keyframeInterval by remember { mutableStateOf("2") }
    var bFrames by remember { mutableStateOf(true) }
    var hwEncoding by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isExporting) {
        if (isExporting) {
            exportProgress = 0f
            while (exportProgress < 1f) {
                delay(80)
                exportProgress = (exportProgress + 0.02f).coerceAtMost(1f)
            }
            delay(300)
            isExporting = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(EC.Bg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(EC.Card)
                .clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("‹", fontSize = 20.sp, color = EC.T2)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("导出设置", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EC.T1)
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            SectionTitle("分辨率")
            ChipRow(listOf("480p", "720p", "1080p", "2K", "4K"), resolution) { resolution = it }
            Spacer(modifier = Modifier.height(14.dp))

            SectionTitle("帧率")
            ChipRow(listOf("24", "25", "30", "50", "60"), fps, suffix = " fps") { fps = it }
            Spacer(modifier = Modifier.height(14.dp))

            SectionTitle("视频编码")
            ChipRow(listOf("H.264", "H.265"), codec) { codec = it }
            Spacer(modifier = Modifier.height(14.dp))

            // 质量 (CRF)
            SectionTitle("质量 (CRF)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("18", fontSize = 9.sp, color = EC.T3, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(4.dp))
                CrfSlider(crf) { crf = it }
                Spacer(modifier = Modifier.width(4.dp))
                Text("28", fontSize = 9.sp, color = EC.T3, fontFamily = FontFamily.Monospace)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("高质量", fontSize = 9.sp, color = EC.T3)
                Text("CRF ${crf.roundToInt()}", fontSize = 10.sp, color = EC.AccL, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                Text("高压缩", fontSize = 9.sp, color = EC.T3)
            }
            Spacer(modifier = Modifier.height(14.dp))

            // 预估文件大小
            val estimatedSize = estimateFileSize(resolution, fps, crf)
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(EC.Card)
                .padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("预估文件大小", fontSize = 10.sp, color = EC.T3)
                        Text("每分钟视频", fontSize = 8.sp, color = EC.T4)
                    }
                    Text("~${estimatedSize}MB/分钟", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EC.AccL, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // 音频设置
            SectionTitle("音频")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("编码格式", fontSize = 9.sp, color = EC.T4)
                    Spacer(modifier = Modifier.height(4.dp))
                    ChipRow(listOf("AAC", "MP3"), audioCodec) { audioCodec = it }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("码率", fontSize = 9.sp, color = EC.T4)
                    Spacer(modifier = Modifier.height(4.dp))
                    ChipRow(listOf("128k", "192k", "256k", "320k"), audioBitrate) { audioBitrate = it }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            SectionTitle("导出格式")
            ChipRow(listOf("MP4", "MOV", "WebM"), format) { format = it }
            Spacer(modifier = Modifier.height(14.dp))

            // 高级设置
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(EC.Card)
                .clickable { showAdvanced = !showAdvanced }.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("高级设置", fontSize = 12.sp, color = EC.T2, fontWeight = FontWeight.Medium)
                    Text(if (showAdvanced) "▾" else "▸", fontSize = 14.sp, color = EC.T3)
                }
            }

            AnimatedVisibility(visible = showAdvanced, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
                Column(modifier = Modifier.padding(top = 8.dp).clip(RoundedCornerShape(10.dp)).background(EC.CardAlt).padding(12.dp)) {
                    Text("关键帧间隔", fontSize = 10.sp, color = EC.T3)
                    Spacer(modifier = Modifier.height(6.dp))
                    ChipRow(listOf("1", "2", "5", "10"), keyframeInterval, suffix = " 秒") { keyframeInterval = it }
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleRow("B-frames", bFrames) { bFrames = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    ToggleRow("硬件编码", hwEncoding) { hwEncoding = it }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 导出按钮 + 进度条
        Column(modifier = Modifier.fillMaxWidth().background(EC.Surf).padding(16.dp)) {
            if (isExporting) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("正在导出...", fontSize = 11.sp, color = EC.T2)
                        Text("${(exportProgress * 100).roundToInt()}%", fontSize = 11.sp, color = EC.AccL, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(EC.T4)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(exportProgress)
                            .clip(RoundedCornerShape(3.dp)).background(Brush.linearGradient(listOf(EC.Acc, EC.Green))))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(EC.Acc, EC.AccL)))
                    .clickable { isExporting = true; onExport() }, contentAlignment = Alignment.Center) {
                    Text("导出", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 11.sp, color = EC.T3, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun ChipRow(options: List<String>, selected: String, suffix: String = "", onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { opt ->
            val sel = opt == selected
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(if (sel) EC.AccS else EC.Card)
                .then(if (sel) Modifier.border(1.5.dp, EC.Acc, RoundedCornerShape(8.dp)) else Modifier)
                .clickable { onSelect(opt) }
                .padding(horizontal = 12.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
                Text(opt + suffix, fontSize = 11.sp, color = if (sel) EC.AccL else EC.T2,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CrfSlider(value: Float, onValueChange: (Float) -> Unit) {
    var pos by remember { mutableStateOf((value - 18f) / 10f) }
    Canvas(modifier = Modifier.weight(1f).height(24.dp).pointerInput(Unit) {
        detectDragGestures { change, _ ->
            change.consume()
            val newPos = (change.position.x / size.width).coerceIn(0f, 1f)
            pos = newPos
            onValueChange(18f + newPos * 10f)
        }
    }) {
        val cy = size.height / 2
        val w = size.width
        drawRoundRect(EC.T4, Offset(0f, cy - 2.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
        drawRoundRect(Brush.linearGradient(listOf(EC.Green, EC.Gold, EC.Red)),
            Offset(0f, cy - 2.dp.toPx()), Size(w * pos, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
        drawCircle(EC.T1, 6.dp.toPx(), Offset(w * pos, cy))
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = EC.T2)
        Box(modifier = Modifier.width(44.dp).height(24.dp).clip(RoundedCornerShape(12.dp))
            .background(if (checked) EC.Acc else EC.T4).clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(modifier = Modifier.padding(2.dp).size(20.dp).clip(RoundedCornerShape(10.dp)).background(Color.White))
        }
    }
}

private fun estimateFileSize(resolution: String, fps: String, crf: Float): Int {
    val resFactor = when (resolution) {
        "480p" -> 0.25f; "720p" -> 0.5f; "1080p" -> 1f; "2K" -> 1.8f; "4K" -> 3.5f; else -> 1f
    }
    val fpsFactor = fps.toFloatOrNull()?.div(30f) ?: 1f
    val crfFactor = (28f - crf) / 10f * 1.5f + 0.5f
    return (60f * resFactor * fpsFactor * crfFactor).roundToInt().coerceIn(5, 800)
}
