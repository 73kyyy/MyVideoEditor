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
fun AISuperResPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var scale by remember { mutableStateOf("2x") }
    var qualityPreset by remember { mutableStateOf("均衡") }
    var compareEnabled by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var sourceWidth by remember { mutableStateOf(1920) }
    var sourceHeight by remember { mutableStateOf(1080) }

    val scaleFactor = if (scale == "4x") 4 else 2
    val outputWidth = sourceWidth * scaleFactor
    val outputHeight = sourceHeight * scaleFactor

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI超分辨率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("RealESRGAN画质增强，让视频更清晰", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("放大倍率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("2x", "4x").forEach { s ->
                OptionChip(
                    when (s) { "2x" -> "2x (推荐)"; else -> "4x (极致)" },
                    scale == s
                ) { scale = s }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("质量预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("速度", "均衡", "质量").forEach { p ->
                OptionChip(p, qualityPreset == p) { qualityPreset = p }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(6.dp))
            .padding(8.dp)) {
            Text(
                when (qualityPreset) {
                    "速度" -> "快速处理，适合预览和实时播放"
                    "均衡" -> "画质与速度平衡，推荐日常使用"
                    "质量" -> "最高画质输出，处理时间较长"
                    else -> ""
                },
                fontSize = 8.sp, color = CG.T3
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("源分辨率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "480p" to Pair(854, 480),
                "720p" to Pair(1280, 720),
                "1080p" to Pair(1920, 1080)
            ).forEach { (label, res) ->
                OptionChip(label, sourceWidth == res.first) {
                    sourceWidth = res.first
                    sourceHeight = res.second
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("前后对比", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (compareEnabled) CG.Acc else CG.Line)
                .clickable { compareEnabled = !compareEnabled },
                contentAlignment = if (compareEnabled) Alignment.CenterEnd else Alignment.CenterStart) {
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
                ResInfoRow("输入分辨率", "${sourceWidth}×${sourceHeight}")
                ResInfoRow("放大倍率", "${scaleFactor}x")
                ResInfoRow("输出分辨率", "${outputWidth}×${outputHeight}")
                ResInfoRow("质量预设", qualityPreset)
                ResInfoRow("预计耗时", when {
                    isProcessing -> "处理中..."
                    qualityPreset == "速度" -> "~${scaleFactor * 2}分钟"
                    qualityPreset == "均衡" -> "~${scaleFactor * 4}分钟"
                    else -> "~${scaleFactor * 8}分钟"
                })
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

        ApplyButton(if (isProcessing) "处理中..." else "开始超分") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            progress = 0f
            bridge.aiSuperRes(vm, true,
                onComplete = {
                    isProcessing = false
                    progress = 1f
                    vm.showToast("超分完成: ${sourceWidth}×${sourceHeight} → ${outputWidth}×${outputHeight}")
                },
                onError = {
                    isProcessing = false
                    progress = 0f
                    vm.showToast("超分失败: $it")
                })
            onClose()
        }
    }
}

@Composable
private fun ResInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 9.sp, color = CG.T3)
        Text(value, fontSize = 9.sp, color = CG.T1)
    }
}
