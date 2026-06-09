package com.myvideo.editor.ui.subtitle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SubtitleItem(
    val id: Int,
    val startTimeMs: Int,
    val endTimeMs: Int,
    val text: String,
    val font: String = "默认",
    val size: Int = 24,
    val color: String = "#FFFFFF",
    val position: String = "底部",
    val bgColor: String = "透明"
)

private object SubC {
    val Bg = Color(0xFF0A0A0A); val Surf = Color(0xFF111111); val Card = Color(0xFF181818)
    val CardH = Color(0xFF222222); val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val AccS = Color(0x1F4A90D9); val Green = Color(0xFF6EC850); val Gold = Color(0xFFE8A820)
    val Red = Color(0xFFE84848)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4); val T3 = Color(0xFF6A6660)
    val T4 = Color(0xFF3A3A3A); val Line = Color(0xFF222222)
}

private fun Int.toTimeStr(): String {
    val s = this / 1000
    val m = s / 60
    val sec = s % 60
    val ms = this % 1000 / 10
    return "%02d:%02d.%02d".format(m, sec, ms)
}

@Composable
fun SubtitleEditorScreen(onBack: () -> Unit = {}) {
    var subtitles by remember {
        mutableStateOf(listOf(
            SubtitleItem(1, 1000, 5000, "这是第一句字幕"),
            SubtitleItem(2, 6000, 10000, "这是第二句字幕"),
            SubtitleItem(3, 12000, 16000, "第三句字幕内容"),
            SubtitleItem(4, 18000, 22000, "第四句字幕，用于测试"),
            SubtitleItem(5, 24000, 28000, "最后一句字幕")
        ))
    }
    var selectedIdx by remember { mutableIntStateOf(-1) }
    var editingIdx by remember { mutableIntStateOf(-1) }
    var editText by remember { mutableStateOf("") }
    var showStylePanel by remember { mutableStateOf(false) }
    var totalDurationMs by remember { mutableIntStateOf(60000) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize().background(SubC.Bg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(SubC.Card)
                .clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("‹", fontSize = 20.sp, color = SubC.T2)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("字幕编辑", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SubC.T1)
            Spacer(modifier = Modifier.weight(1f))
            // AI 自动生成
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SubC.Green.copy(0.12f))
                .clickable { /* AI whisper */ }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                Text("AI 生成", fontSize = 10.sp, color = SubC.Green, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            // Import SRT
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SubC.Acc.copy(0.12f))
                .clickable { /* import */ }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                Text("导入SRT", fontSize = 10.sp, color = SubC.Acc, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(4.dp))
            // Export SRT
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SubC.Gold.copy(0.12f))
                .clickable { /* export */ }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                Text("导出SRT", fontSize = 10.sp, color = SubC.Gold, fontWeight = FontWeight.Medium)
            }
        }

        // Video preview (small)
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp)).background(Color(0xFF050505)),
            contentAlignment = Alignment.Center) {
            Text("1920×1080 · 30fps", fontSize = 9.sp, color = SubC.T3, fontFamily = FontFamily.Monospace)
            // Simulated subtitle overlay
            if (selectedIdx >= 0 && selectedIdx < subtitles.size) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(4.dp)).background(Color(0xAA000000))
                    .padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(subtitles[selectedIdx].text, fontSize = 14.sp, color = Color.White,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle timeline
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(6.dp)).background(SubC.Card)) {
            // Time ruler
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Time marks
                for (i in 0..6) {
                    val x = w * i / 6f
                    drawLine(SubC.T4, Offset(x, h - 10f), Offset(x, h), strokeWidth = 1f)
                }
                // Subtitle blocks
                subtitles.forEachIndexed { idx, sub ->
                    val start = sub.startTimeMs.toFloat() / totalDurationMs
                    val end = sub.endTimeMs.toFloat() / totalDurationMs
                    val blockW = (end - start) * w
                    val blockX = start * w
                    val color = if (idx == selectedIdx) SubC.Acc.copy(0.6f) else SubC.Acc.copy(0.25f)
                    drawRoundRect(color, Offset(blockX, 4f), Size(blockW.coerceAtLeast(4f), h - 16f), CornerRadius(4.dp.toPx()))
                    // Drag handles
                    drawCircle(Color.White.copy(alpha = 0.6f), 3.dp.toPx(), Offset(blockX, h / 2))
                    drawCircle(Color.White.copy(alpha = 0.6f), 3.dp.toPx(), Offset(blockX + blockW, h / 2))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Style options bar
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("字体" to "Aa", "大小" to "24", "颜色" to "■", "位置" to "↕", "背景" to "▢").forEach { (label, icon) ->
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SubC.Card)
                    .clickable { showStylePanel = !showStylePanel }
                    .padding(horizontal = 8.dp, vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text("$icon $label", fontSize = 9.sp, color = SubC.T2)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            // Add subtitle button
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SubC.Acc.copy(0.15f))
                .clickable {
                    val newId = (subtitles.maxOfOrNull { it.id } ?: 0) + 1
                    val lastEnd = subtitles.lastOrNull()?.endTimeMs ?: 0
                    subtitles = subtitles + SubtitleItem(newId, lastEnd + 500, lastEnd + 3500, "新字幕")
                }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text("+ 添加字幕", fontSize = 10.sp, color = SubC.AccL, fontWeight = FontWeight.SemiBold)
            }
        }

        // Style panel (expandable)
        if (showStylePanel && selectedIdx >= 0 && selectedIdx < subtitles.size) {
            val sub = subtitles[selectedIdx]
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp)).background(SubC.CardH).padding(10.dp)) {
                Column {
                    Text("样式设置", fontSize = 10.sp, color = SubC.T3, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("默认", "黑体", "宋体", "楷体").forEach { f ->
                            val sel = sub.font == f
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                .background(if (sel) SubC.AccS else SubC.Card)
                                .clickable { subtitles = subtitles.toMutableList().also { it[selectedIdx] = sub.copy(font = f) } }
                                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text(f, fontSize = 9.sp, color = if (sel) SubC.AccL else SubC.T2)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("18", "24", "32", "40").forEach { s ->
                            val sel = sub.size == s.toInt()
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                .background(if (sel) SubC.AccS else SubC.Card)
                                .clickable { subtitles = subtitles.toMutableList().also { it[selectedIdx] = sub.copy(size = s.toInt()) } }
                                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text(s, fontSize = 9.sp, color = if (sel) SubC.AccL else SubC.T2)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("#FFFFFF" to "白", "#FFFF00" to "黄", "#00FF00" to "绿", "#FF6600" to "橙").forEach { (c, l) ->
                            val sel = sub.color == c
                            Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                                .background(Color(android.graphics.Color.parseColor(c)))
                                .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp)) else Modifier)
                                .clickable { subtitles = subtitles.toMutableList().also { it[selectedIdx] = sub.copy(color = c) } },
                                contentAlignment = Alignment.Center) {
                                if (sel) Text("✓", fontSize = 10.sp, color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("顶部", "中部", "底部").forEach { p ->
                            val sel = sub.position == p
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                .background(if (sel) SubC.AccS else SubC.Card)
                                .clickable { subtitles = subtitles.toMutableList().also { it[selectedIdx] = sub.copy(position = p) } }
                                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text(p, fontSize = 9.sp, color = if (sel) SubC.AccL else SubC.T2)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle list
        if (subtitles.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无字幕", fontSize = 14.sp, color = SubC.T3)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("点击「+ 添加字幕」或「AI 生成」", fontSize = 11.sp, color = SubC.T4)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(subtitles) { idx, sub ->
                    val sel = idx == selectedIdx
                    val editing = idx == editingIdx
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(if (sel) SubC.Acc.copy(0.08f) else SubC.Card)
                        .then(if (sel) Modifier.border(1.dp, SubC.Acc.copy(0.3f), RoundedCornerShape(8.dp)) else Modifier)
                        .clickable { selectedIdx = idx; editingIdx = -1 }
                        .padding(10.dp)) {
                        // Time row with drag handles
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${idx + 1}", fontSize = 9.sp, color = SubC.T4, fontFamily = FontFamily.Monospace, modifier = Modifier.width(20.dp))
                            // Start time with drag handle
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SubC.CardH)
                                .clickable { /* adjust start time */ }.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(sub.startTimeMs.toTimeStr(), fontSize = 10.sp, color = SubC.AccL, fontFamily = FontFamily.Monospace)
                            }
                            Text(" → ", fontSize = 10.sp, color = SubC.T4)
                            // End time with drag handle
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SubC.CardH)
                                .clickable { /* adjust end time */ }.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(sub.endTimeMs.toTimeStr(), fontSize = 10.sp, color = SubC.AccL, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Edit / Delete
                            Text("编辑", fontSize = 9.sp, color = SubC.Acc,
                                modifier = Modifier.clickable { editingIdx = idx; editText = sub.text })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("删除", fontSize = 9.sp, color = SubC.Red,
                                modifier = Modifier.clickable {
                                    subtitles = subtitles.toMutableList().also { it.removeAt(idx) }
                                    if (selectedIdx >= subtitles.size) selectedIdx = subtitles.lastIndex.coerceAtLeast(-1)
                                    if (editingIdx == idx) editingIdx = -1
                                })
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        if (editing) {
                            // Inline editing
                            BasicTextField(value = editText, onValueChange = { editText = it },
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                    .clip(RoundedCornerShape(6.dp)).background(SubC.CardH)
                                    .padding(horizontal = 8.dp),
                                textStyle = TextStyle(fontSize = 13.sp, color = SubC.T1), singleLine = true,
                                cursorBrush = SolidColor(SubC.Acc))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SubC.Acc.copy(0.15f))
                                    .clickable {
                                        subtitles = subtitles.toMutableList().also { it[idx] = sub.copy(text = editText) }
                                        editingIdx = -1
                                    }.padding(horizontal = 10.dp, vertical = 4.dp)) {
                                    Text("保存", fontSize = 10.sp, color = SubC.AccL)
                                }
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SubC.CardH)
                                    .clickable { editingIdx = -1 }.padding(horizontal = 10.dp, vertical = 4.dp)) {
                                    Text("取消", fontSize = 10.sp, color = SubC.T3)
                                }
                            }
                        } else {
                            Text(sub.text, fontSize = 13.sp, color = SubC.T1, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
