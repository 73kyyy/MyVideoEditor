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

@Composable
fun AIInterpolationPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var mode by remember { mutableStateOf("2x") }
    var customMultiplier by remember { mutableStateOf(30) } // 1.5x-8x mapped to 15-80
    var previewEnabled by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var sourceFps by remember { mutableStateOf(30) }

    val multiplier = when (mode) {
        "2x" -> 2f; "4x" -> 4f; "自定义" -> customMultiplier / 10f; else -> 2f
    }
    val outputFps = (sourceFps * multiplier).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI智能补帧", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("RIFE光流插帧，让画面更流畅丝滑", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("补帧模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("2x", "4x", "自定义").forEach { m ->
                OptionChip(
                    when (m) { "2x" -> "2x (30→60fps)"; "4x" -> "4x (30→120fps)"; else -> "自定义" },
                    mode == m
                ) { mode = m }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (mode == "自定义") {
            Text("自定义倍率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            CgSlider("插帧倍率", 15, customMultiplier, 80) { customMultiplier = it }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("1.5x" to 15, "2x" to 20, "3x" to 30, "4x" to 40, "6x" to 60, "8x" to 80).forEach { (label, v) ->
                    OptionChip(label, customMultiplier == v) { customMultiplier = v }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        Text("源帧率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(24, 25, 30, 50, 60).forEach { fps ->
                OptionChip("${fps}fps", sourceFps == fps) { sourceFps = fps }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("实时预览", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (previewEnabled) CG.Acc else CG.Line)
                .clickable { previewEnabled = !previewEnabled },
                contentAlignment = if (previewEnabled) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("输出信息", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .padding(12.dp)) {
            Column {
                InfoRow("源帧率", "${sourceFps}fps")
                InfoRow("插帧倍率", "${"%.1f".format(multiplier)}x")
                InfoRow("输出帧率", "${outputFps}fps")
                InfoRow("新增帧数", "${((multiplier - 1) * 100).toInt()}%")
                InfoRow("状态", if (isProcessing) "处理中..." else "就绪")
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

        ApplyButton(if (isProcessing) "处理中..." else "开始补帧") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            progress = 0f
            bridge.aiInterpolate(vm, true,
                onComplete = {
                    isProcessing = false
                    progress = 1f
                    vm.showToast("补帧完成: ${sourceFps}fps → ${outputFps}fps")
                },
                onError = {
                    isProcessing = false
                    progress = 0f
                    vm.showToast("补帧失败: $it")
                })
            onClose()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 9.sp, color = CG.T3)
        Text(value, fontSize = 9.sp, color = CG.T1)
    }
}
