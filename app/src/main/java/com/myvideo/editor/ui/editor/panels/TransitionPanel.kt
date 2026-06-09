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
fun TransitionPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    var selected by remember { mutableStateOf("淡入淡出") }
    var duration by remember { mutableStateOf(500) } // ms
    var easing by remember { mutableStateOf("线性") }
    var previewOn by remember { mutableStateOf(false) }

    data class TransitionItem(val name: String, val color1: Color, val color2: Color)

    val transitions = listOf(
        TransitionItem("无", Color(0xFF3A3A3A), Color(0xFF4A4A4A)),
        TransitionItem("淡入淡出", Color(0xFF4A4A4A), Color(0xFF888888)),
        TransitionItem("交叉溶解", Color(0xFF5C6BC0), Color(0xFF7986CB)),
        TransitionItem("向左擦除", Color(0xFF26A69A), Color(0xFF4DB6AC)),
        TransitionItem("向右擦除", Color(0xFF26A69A), Color(0xFF80CBC4)),
        TransitionItem("向上擦除", Color(0xFF42A5F5), Color(0xFF64B5F6)),
        TransitionItem("向下擦除", Color(0xFF42A5F5), Color(0xFF90CAF9)),
        TransitionItem("滑动", Color(0xFF5C6BC0), Color(0xFF9FA8DA)),
        TransitionItem("缩放", Color(0xFFFFCA28), Color(0xFFFFE082)),
        TransitionItem("旋转", Color(0xFF66BB6A), Color(0xFF81C784)),
        TransitionItem("模糊", Color(0xFF78909C), Color(0xFF90A4AE)),
        TransitionItem("闪白", Color(0xFFE0E0E0), Color(0xFFFFFFFF))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // 转场类型网格
        Text("转场类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        transitions.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { t ->
                    val sel = selected == t.name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(t.color1, t.color2)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { selected = t.name },
                            contentAlignment = Alignment.Center) {
                            Text(t.name, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                repeat(4 - row.size) {
                    Column { Spacer(modifier = Modifier.weight(1f).fillMaxWidth().height(40.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 时长滑块 (0.1s - 2.0s)
        Text("时长", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("转场时长", 100, duration, 2000) { duration = it }
        Text("${String.format("%.1f", duration / 1000f)}s", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("0.1s" to 100, "0.3s" to 300, "0.5s" to 500, "1.0s" to 1000, "2.0s" to 2000).forEach { (label, ms) ->
                OptionChip(label, duration == ms) { duration = ms }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 缓动曲线
        Text("缓动曲线", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("线性", "缓入", "缓出", "缓入缓出").forEach { e ->
                OptionChip(e, easing == e) { easing = e }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 预览切换
        ToggleRow("预览转场效果", previewOn) { previewOn = it }
        Spacer(modifier = Modifier.height(10.dp))

        // 预览区域
        if (previewOn) {
            Text("预览", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp))
                .background(Color.Black), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(0.5f).fillMaxHeight()
                        .background(Color(0xFF4A90D9).copy(alpha = 0.3f)))
                    Box(modifier = Modifier.weight(0.5f).fillMaxHeight()
                        .background(Color(0xFFE85050).copy(alpha = 0.3f)))
                }
                Box(modifier = Modifier.align(Alignment.Center).width(1.dp).height(48.dp)
                    .background(Color.White.copy(alpha = 0.3f)))
                Text(selected, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 应用按钮
        ApplyButton("应用转场") {
            if (vm.clips.size < 2) {
                vm.showToast("需要至少两个片段才能添加转场")
            } else {
                bridge.applyEffect("transition", mapOf(
                    "type" to selected,
                    "duration" to duration,
                    "easing" to easing
                ))
                bridge.applyTransition(vm, selected, duration.toLong(),
                    onComplete = { vm.showToast("转场已应用: $selected") },
                    onError = { vm.showToast("转场失败: $it") })
            }
            onClose()
        }
    }
}
