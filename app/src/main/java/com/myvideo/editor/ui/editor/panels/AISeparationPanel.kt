package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class StemInfo(
    val id: String,
    val label: String,
    val color: Color,
    var volume: Int = 100,
    var selected: Boolean = true
)

@Composable
fun AISeparationPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var separationMode by remember { mutableStateOf("人声") }
    var exportSelected by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    var stems by remember {
        mutableStateOf(
            listOf(
                StemInfo("vocals", "人声", Color(0xFF4A90D9), 100, true),
                StemInfo("drums", "鼓点", Color(0xFFE8A820), 100, true),
                StemInfo("bass", "贝斯", Color(0xFF7EC850), 100, true),
                StemInfo("other", "其他", Color(0xFFE85050), 100, true)
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI人声分离", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Demucs音轨分离，提取人声和乐器", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("分离模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("人声", "全部音轨").forEach { m ->
                OptionChip(
                    when (m) { "人声" -> "仅人声"; else -> "全部音轨" },
                    separationMode == m
                ) { separationMode = m }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("音轨选择", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            stems.forEach { stem ->
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (stem.selected) stem.color.copy(alpha = 0.2f) else CG.Card)
                    .border(
                        1.dp,
                        if (stem.selected) stem.color else CG.Line,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        stems = stems.map {
                            if (it.id == stem.id) it.copy(selected = !it.selected) else it
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(stem.label, fontSize = 9.sp,
                        color = if (stem.selected) stem.color else CG.T3,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("音量控制", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        stems.forEach { stem ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp))
                    .background(stem.color))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stem.label, fontSize = 9.sp, color = CG.T3,
                    modifier = Modifier.width(32.dp))
                SliderCompact(stem.volume) { newVol ->
                    stems = stems.map {
                        if (it.id == stem.id) it.copy(volume = newVol) else it
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("${stem.volume}%", fontSize = 8.sp, color = CG.T2,
                    modifier = Modifier.width(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("仅导出选中音轨", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (exportSelected) CG.Acc else CG.Line)
                .clickable { exportSelected = !exportSelected },
                contentAlignment = if (exportSelected) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (isProcessing) {
            Text("处理进度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = CG.Acc,
                trackColor = CG.Line
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${(progress * 100).toInt()}%", fontSize = 8.sp, color = CG.T3)
            Spacer(modifier = Modifier.height(10.dp))
        }

        ApplyButton(if (isProcessing) "处理中..." else "开始分离") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            progress = 0f
            bridge.aiSeparate(vm, true,
                onComplete = {
                    isProcessing = false
                    progress = 1f
                    val selectedStems = stems.filter { it.selected }.map { it.label }
                    vm.showToast("分离完成: ${selectedStems.joinToString("、")}")
                },
                onError = {
                    isProcessing = false
                    progress = 0f
                    vm.showToast("分离失败: $it")
                })
            onClose()
        }
    }
}

@Composable
private fun SliderCompact(value: Int, onValueChange: (Int) -> Unit) {
    Box(modifier = Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(3.dp))
        .background(CG.Line).clickable { },
        contentAlignment = Alignment.CenterStart) {
        Box(modifier = Modifier.fillMaxHeight()
            .fillMaxWidth(value / 100f)
            .clip(RoundedCornerShape(3.dp))
            .background(CG.Acc.copy(alpha = 0.4f)))
        androidx.compose.material3.Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = CG.AccL,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}
