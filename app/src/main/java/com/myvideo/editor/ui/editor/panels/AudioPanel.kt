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
fun AudioPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    var volume by remember { mutableStateOf(100) }
    var fadeIn by remember { mutableStateOf(0) }
    var fadeOut by remember { mutableStateOf(0) }
    var eqPreset by remember { mutableStateOf("无") }
    var pan by remember { mutableStateOf(0) }
    var speed by remember { mutableStateOf(100) }
    var showAIDenoise by remember { mutableStateOf(false) }
    var showAISeparation by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 音量控制
        Text("音量控制", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("音量", 0, volume, 200) { volume = it }
        Text("${volume}%", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // 淡入淡出
        Text("淡入淡出", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("淡入时长", 0, fadeIn, 5000) { fadeIn = it }
        Text("${String.format("%.1f", fadeIn / 1000f)}s", fontSize = 8.sp, color = CG.T3)
        CgSlider("淡出时长", 0, fadeOut, 5000) { fadeOut = it }
        Text("${String.format("%.1f", fadeOut / 1000f)}s", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // 音频波形显示
        Text("音频波形", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center) {
            // 波形渐变占位
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                val barHeights = listOf(12, 20, 28, 35, 22, 40, 30, 18, 25, 38, 15, 32, 26, 20, 34, 12, 28, 36, 22, 14)
                barHeights.forEach { h ->
                    Box(modifier = Modifier.width(4.dp).height(h.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.verticalGradient(
                            listOf(CG.Acc.copy(alpha = 0.8f), CG.AccL.copy(alpha = 0.3f)))))
                }
            }
            Text("波形预览", fontSize = 8.sp, color = CG.T3,
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))

        // AI功能按钮
        Text("AI音频", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CG.Card).border(1.dp, CG.Acc.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { showAIDenoise = true },
                contentAlignment = Alignment.Center) {
                Text("AI 降噪", fontSize = 10.sp, color = CG.AccL, fontWeight = FontWeight.Medium)
            }
            Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CG.Card).border(1.dp, CG.Acc.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { showAISeparation = true },
                contentAlignment = Alignment.Center) {
                Text("AI 人声分离", fontSize = 10.sp, color = CG.AccL, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 均衡器预设
        Text("均衡器预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("无", "人声", "音乐", "低音增强", "高音增强").forEach { preset ->
                OptionChip(preset, eqPreset == preset) { eqPreset = preset }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 声道控制
        Text("声道控制", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("声相 (左-右)", -100, pan, 100) { pan = it }
        Text(if (pan < 0) "左 ${-pan}%" else if (pan > 0) "右 ${pan}%" else "居中",
            fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // 变速控制
        Text("变速控制", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("速度", 50, speed, 200) { speed = it }
        Text("${String.format("%.2f", speed / 100f)}x", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("0.5x" to 50, "0.75x" to 75, "1.0x" to 100, "1.5x" to 150, "2.0x" to 200).forEach { (label, v) ->
                OptionChip(label, speed == v) { speed = v }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 应用按钮
        ApplyButton(if (isProcessing) "处理中..." else "应用音频设置") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            val params = mutableMapOf<String, Any>(
                "volume" to (volume / 100f),
                "fadeIn" to fadeIn,
                "fadeOut" to fadeOut,
                "eqPreset" to eqPreset,
                "pan" to (pan / 100f),
                "speed" to (speed / 100f)
            )
            bridge.applyEffect("audio_adjust", params)
            if (volume != 100) {
                bridge.applyAudioVolume(vm, volume / 100f,
                    onComplete = { isProcessing = false; vm.showToast("音量调整完成") },
                    onError = { isProcessing = false; vm.showToast("音量调整失败: $it") })
            }
            if (speed != 100) {
                bridge.applySpeed(vm, speed / 100f,
                    onComplete = { isProcessing = false; vm.showToast("变速完成") },
                    onError = { isProcessing = false; vm.showToast("变速失败: $it") })
            }
            vm.showToast("音频设置已应用")
            isProcessing = false
            onClose()
        }
    }

    // AI降噪面板
    if (showAIDenoise) {
        AIDenoisePanel(vm) { showAIDenoise = false }
    }

    // AI人声分离面板
    if (showAISeparation) {
        AISeparationPanel(vm) { showAISeparation = false }
    }
}

@Composable
private fun AIDenoisePanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        .background(CG.Surf).border(1.dp, CG.Line, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text("AI 降噪", fontSize = 11.sp, color = CG.T1, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("使用AI模型自动去除背景噪声，保留清晰人声", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(12.dp))
        ApplyButton(if (isProcessing) "处理中..." else "开始降噪") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            bridge.aiDenoise(vm, true,
                onComplete = { isProcessing = false; vm.showToast("AI降噪完成") },
                onError = { isProcessing = false; vm.showToast("AI降噪失败: $it") })
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(28.dp).clip(RoundedCornerShape(6.dp))
            .background(CG.Card).clickable { onClose() },
            contentAlignment = Alignment.Center) {
            Text("关闭", fontSize = 10.sp, color = CG.T2)
        }
    }
}

@Composable
private fun AISeparationPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        .background(CG.Surf).border(1.dp, CG.Line, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text("AI 人声分离", fontSize = 11.sp, color = CG.T1, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("将音频中的人声与伴奏分离为独立轨道", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(12.dp))
        ApplyButton(if (isProcessing) "处理中..." else "开始分离") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            bridge.aiSeparate(vm, true,
                onComplete = { isProcessing = false; vm.showToast("人声分离完成") },
                onError = { isProcessing = false; vm.showToast("人声分离失败: $it") })
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(28.dp).clip(RoundedCornerShape(6.dp))
            .background(CG.Card).clickable { onClose() },
            contentAlignment = Alignment.Center) {
            Text("关闭", fontSize = 10.sp, color = CG.T2)
        }
    }
}
