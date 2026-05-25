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
    var selectedAudio by remember { mutableStateOf<String?>(null) }
    var effect by remember { mutableStateOf("无") }
    var volume by remember { mutableStateOf(80) }
    var fadeIn by remember { mutableStateOf(0) }
    var fadeOut by remember { mutableStateOf(0) }
    var eqPreset by remember { mutableStateOf("平坦") }
    var denoiseStrength by remember { mutableStateOf(0) }
    var pitchShift by remember { mutableStateOf(0) }
    var reverbRoom by remember { mutableStateOf(50) }
    var echoDelay by remember { mutableStateOf(0) }
    var echoFeedback by remember { mutableStateOf(30) }

    val audios = listOf(
        Triple("背景音乐 01", "0:15", "轻松"),
        Triple("背景音乐 02", "0:23", "动感"),
        Triple("环境音", "0:31", "自然"),
        Triple("鼓点节奏", "0:39", "节拍"),
        Triple("钢琴旋律", "0:47", "古典"),
        Triple("电子氛围", "0:55", "电子"),
        Triple("嘻哈节拍", "0:28", "嘻哈"),
        Triple("摇滚吉他", "0:35", "摇滚"),
        Triple("爵士萨克斯", "0:42", "爵士"),
        Triple("民谣吉他", "0:38", "民谣")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("音频素材", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        audios.forEach { (name, dur, tag) ->
            val sel = selectedAudio == name
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (sel) CG.AccS else CG.Card)
                .then(if (sel) Modifier.border(1.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                .clickable { selectedAudio = name }
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("▶", fontSize = 12.sp, color = CG.T3)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontSize = 11.sp, color = CG.T1, fontWeight = FontWeight.Medium)
                    Text("$dur · $tag · 免费", fontSize = 8.sp, color = CG.T3)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(CG.AccS)
                    .clickable { vm.showToast("已选择: $name") }
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("使用", fontSize = 8.sp, color = CG.AccL, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("音量控制", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("主音量", 0, volume, 150) { volume = it }
        CgSlider("淡入(ms)", 0, fadeIn, 3000) { fadeIn = it }
        CgSlider("淡出(ms)", 0, fadeOut, 3000) { fadeOut = it }
        Spacer(modifier = Modifier.height(14.dp))
        Text("音效", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("无", "混响", "回声", "均衡器", "降噪", "变速不变调").forEach { e ->
                OptionChip(e, effect == e) { effect = e }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        when (effect) {
            "混响" -> {
                Text("混响设置", fontSize = 9.sp, color = CG.T4)
                CgSlider("房间大小", 0, reverbRoom, 100) { reverbRoom = it }
                CgSlider("衰减", 0, 50, 100)
                CgSlider("湿信号", 0, 30, 100)
            }
            "回声" -> {
                Text("回声设置", fontSize = 9.sp, color = CG.T4)
                CgSlider("延迟(ms)", 50, echoDelay, 1000) { echoDelay = it }
                CgSlider("反馈", 0, echoFeedback, 90) { echoFeedback = it }
                CgSlider("混合", 0, 50, 100)
            }
            "均衡器" -> {
                Text("EQ预设", fontSize = 9.sp, color = CG.T4)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("平坦", "低音增强", "高音增强", "人声", "摇滚", "流行", "古典").forEach { p ->
                        OptionChip(p, eqPreset == p) { eqPreset = p }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                CgSlider("60Hz", -12, 0, 12)
                CgSlider("230Hz", -12, 0, 12)
                CgSlider("910Hz", -12, 0, 12)
                CgSlider("3.6kHz", -12, 0, 12)
                CgSlider("14kHz", -12, 0, 12)
            }
            "降噪" -> {
                Text("降噪设置", fontSize = 9.sp, color = CG.T4)
                CgSlider("降噪强度", 0, denoiseStrength, 100) { denoiseStrength = it }
                CgSlider("阈值", -80, -40, 0)
                CgSlider("衰减", 0, 50, 100)
            }
            "变速不变调" -> {
                Text("时间拉伸", fontSize = 9.sp, color = CG.T4)
                CgSlider("速度", 25, 100, 400)
                CgSlider("音调", -12, pitchShift, 12) { pitchShift = it }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ApplyButton("添加到时间轴") {
            if (effect == "降噪" && denoiseStrength > 0) {
                bridge.applyAudioDenoise(vm,
                    onComplete = { vm.showToast("降噪完成") },
                    onError = { vm.showToast("降噪失败: $it") })
            }
            if (volume != 100) {
                bridge.applyAudioVolume(vm, volume / 100f,
                    onComplete = { vm.showToast("音量调整完成") },
                    onError = { vm.showToast("音量调整失败: $it") })
            }
            vm.showToast("已添加到时间轴: ${selectedAudio ?: effect}")
            onClose()
        }
    }
}
