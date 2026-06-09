package com.myvideo.editor.ui.editor.panels

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun AIDenoisePanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var strength by remember { mutableStateOf(60) }
    var previewEnabled by remember { mutableStateOf(false) }
    var voiceEnhance by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Simulated real-time audio level meter
    val infiniteTransition = rememberInfiniteTransition(label = "audioMeter")
    val animatedLevel by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
        ),
        label = "levelAnim"
    )
    val audioLevels = remember { mutableStateListOf(0.3f, 0.5f, 0.7f, 0.4f, 0.6f, 0.8f, 0.5f, 0.3f, 0.6f, 0.4f, 0.7f, 0.5f, 0.3f, 0.6f, 0.4f, 0.8f) }

    LaunchedEffect(animatedLevel) {
        for (i in audioLevels.indices) {
            audioLevels[i] = (animatedLevel + kotlin.random.Random.nextFloat() * 0.2f).coerceIn(0f, 1f)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI智能降噪", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("RNNoise深度降噪，去除环境噪音", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("降噪强度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("强度", 0, strength, 100) { strength = it }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("轻微" to 20, "适中" to 50, "强力" to 75, "极致" to 95).forEach { (label, v) ->
                OptionChip(label, strength == v) { strength = v }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(6.dp))
            .padding(8.dp)) {
            Text(
                when {
                    strength < 20 -> "轻微降噪，保留环境氛围音"
                    strength < 50 -> "适度降噪，去除明显噪音"
                    strength < 75 -> "强力降噪，人声清晰突出"
                    else -> "极致降噪，仅保留人声"
                },
                fontSize = 8.sp, color = CG.T3
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("前后对比预览", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (previewEnabled) CG.Acc else CG.Line)
                .clickable { previewEnabled = !previewEnabled },
                contentAlignment = if (previewEnabled) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("人声增强", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (voiceEnhance) CG.Acc else CG.Line)
                .clickable { voiceEnhance = !voiceEnhance },
                contentAlignment = if (voiceEnhance) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("实时音频电平", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .padding(8.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().height(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom) {
                    Text("降噪前", fontSize = 7.sp, color = CG.T3,
                        modifier = Modifier.width(32.dp).align(Alignment.CenterVertically))
                    audioLevels.forEachIndexed { i, level ->
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(level)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                when {
                                    level > 0.8f -> Color(0xFFE85050)
                                    level > 0.5f -> Color(0xFFE8A820)
                                    else -> Color(0xFF4A90D9)
                                }
                            ))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth().height(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom) {
                    Text("降噪后", fontSize = 7.sp, color = CG.T3,
                        modifier = Modifier.width(32.dp).align(Alignment.CenterVertically))
                    audioLevels.forEachIndexed { i, level ->
                        val reducedLevel = (level * (1f - strength / 150f)).coerceIn(0.05f, 1f)
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(reducedLevel)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                when {
                                    reducedLevel > 0.7f -> Color(0xFFE85050)
                                    reducedLevel > 0.4f -> Color(0xFF7EC850)
                                    else -> Color(0xFF4A90D9)
                                }
                            ))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0dB", fontSize = 7.sp, color = CG.T4, fontFamily = FontFamily.Monospace)
            Text("-6dB", fontSize = 7.sp, color = CG.T4, fontFamily = FontFamily.Monospace)
            Text("-12dB", fontSize = 7.sp, color = CG.T4, fontFamily = FontFamily.Monospace)
            Text("-24dB", fontSize = 7.sp, color = CG.T4, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton(if (isProcessing) "处理中..." else "应用降噪") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            bridge.aiDenoise(vm, true,
                onComplete = {
                    isProcessing = false
                    vm.showToast("降噪完成 (强度${strength}%)")
                },
                onError = {
                    isProcessing = false
                    vm.showToast("降噪失败: $it")
                })
            onClose()
        }
    }
}
