package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class SubtitleEntry(
    val id: Int,
    var text: String,
    val startTime: String,
    val endTime: String,
    val confidence: Float
)

@Composable
fun AISpeechPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var language by remember { mutableStateOf("自动") }
    var autoSubtitle by remember { mutableStateOf(true) }
    var timestampPrecision by remember { mutableStateOf("句子") }
    var confidenceThreshold by remember { mutableStateOf(60) }
    var isProcessing by remember { mutableStateOf(false) }
    var hasResult by remember { mutableStateOf(false) }

    var subtitles by remember {
        mutableStateOf(
            listOf(
                SubtitleEntry(0, "大家好，欢迎来到这个频道", "00:00.00", "00:02.50", 0.95f),
                SubtitleEntry(1, "今天我们要介绍一款全新的视频编辑工具", "00:02.80", "00:06.20", 0.91f),
                SubtitleEntry(2, "它拥有强大的AI功能", "00:06.50", "00:08.30", 0.88f),
                SubtitleEntry(3, "可以一键完成语音转字幕", "00:08.60", "00:11.00", 0.93f),
                SubtitleEntry(4, "支持多种语言自动识别", "00:11.30", "00:13.80", 0.87f),
                SubtitleEntry(5, "Let's get started with the demo", "00:14.10", "00:16.50", 0.82f),
                SubtitleEntry(6, "まずは基本機能からご紹介します", "00:16.80", "00:19.20", 0.79f),
                SubtitleEntry(7, "点击下方按钮开始体验", "00:19.50", "00:21.80", 0.94f)
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI语音转字幕", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Whisper语音识别，自动生成时间轴字幕", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("识别语言", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("自动", "中文", "英语", "日语", "韩语").forEach { lang ->
                OptionChip(lang, language == lang) { language = lang }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("自动生成字幕", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (autoSubtitle) CG.Acc else CG.Line)
                .clickable { autoSubtitle = !autoSubtitle },
                contentAlignment = if (autoSubtitle) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("时间戳精度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("句子", "词语").forEach { p ->
                OptionChip(
                    when (p) { "句子" -> "句子级"; else -> "词语级" },
                    timestampPrecision == p
                ) { timestampPrecision = p }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("置信度阈值", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("最低置信度", 0, confidenceThreshold, 100) { confidenceThreshold = it }
        Spacer(modifier = Modifier.height(14.dp))

        if (hasResult) {
            Text("识别结果", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("点击文字可编辑 · ${subtitles.size}条字幕",
                fontSize = 8.sp, color = CG.T3)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(subtitles) { index, entry ->
                    val isHighConfidence = entry.confidence * 100 >= confidenceThreshold
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isHighConfidence) CG.Card else Color(0xFF2A2020))
                            .border(
                                1.dp,
                                if (isHighConfidence) CG.Line else Color(0xFF4A3030),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { vm.showToast("编辑字幕: ${entry.text}") }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.text, fontSize = 10.sp, color = CG.T1,
                                fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${entry.startTime} → ${entry.endTime}",
                                fontSize = 8.sp, color = CG.T3,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(modifier = Modifier.width(40.dp).height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    entry.confidence >= 0.9f -> Color(0xFF2A4A2A)
                                    entry.confidence >= 0.7f -> Color(0xFF4A4A2A)
                                    else -> Color(0xFF4A2A2A)
                                }
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${(entry.confidence * 100).toInt()}%",
                                fontSize = 7.sp, color = CG.T1,
                                fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (!hasResult) {
            ApplyButton(if (isProcessing) "识别中..." else "开始识别") {
                if (isProcessing) return@ApplyButton
                isProcessing = true
                bridge.aiWhisper(vm, true,
                    onComplete = {
                        isProcessing = false
                        hasResult = true
                        vm.showToast("语音识别完成")
                    },
                    onError = {
                        isProcessing = false
                        vm.showToast("识别失败: $it")
                    })
            }
        } else {
            ApplyButton("应用到时间轴") {
                vm.showToast("已添加${subtitles.size}条字幕到时间轴")
                onClose()
            }
            Spacer(modifier = Modifier.height(6.dp))
            ApplyButton("重新识别") {
                hasResult = false
                isProcessing = false
            }
        }
    }
}
