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
fun ReversePanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    var reverseEntireClip by remember { mutableStateOf(true) }
    var segmentStart by remember { mutableStateOf(0) }   // percentage 0-100
    var segmentEnd by remember { mutableStateOf(100) }    // percentage 0-100
    var reverseSpeed by remember { mutableStateOf(100) }  // 100 = 1.0x
    var previewOn by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("倒放设置", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("将视频片段反向播放", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // Reverse entire clip toggle
        ToggleRow("整段倒放", reverseEntireClip) { reverseEntireClip = it }
        Spacer(modifier = Modifier.height(14.dp))

        // Segment range (when not entire clip)
        if (!reverseEntireClip) {
            Text("倒放区间", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            CgSlider("起始位置", 0, segmentStart, 95) { segmentStart = it.coerceAtMost(segmentEnd - 5) }
            CgSlider("结束位置", 5, segmentEnd, 100) { segmentEnd = it.coerceAtLeast(segmentStart + 5) }
            Spacer(modifier = Modifier.height(8.dp))

            // Visual segment indicator
            Box(modifier = Modifier.fillMaxWidth().height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CG.Card)
                .border(1.dp, CG.Line, RoundedCornerShape(4.dp))
            ) {
                Box(modifier = Modifier.fillMaxHeight()
                    .padding(start = (segmentStart / 100f * 280f).dp, end = ((100 - segmentEnd) / 100f * 280f).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CG.Acc.copy(alpha = 0.3f))
                    .border(1.dp, CG.Acc, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center) {
                    Text("倒放区间", fontSize = 8.sp, color = CG.AccL)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Speed control for reversed section
        Text("倒放速度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("速度", 25, reverseSpeed, 400) { reverseSpeed = it }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("0.25x", "0.5x", "1.0x", "1.5x", "2.0x", "4.0x").forEach { s ->
                val speedVal = (s.replace("x", "").toFloat() * 100).toInt()
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (reverseSpeed == speedVal) CG.AccS else CG.Card)
                    .then(if (reverseSpeed == speedVal) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                    .clickable { reverseSpeed = speedVal }
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center) {
                    Text(s, fontSize = 9.sp, fontWeight = FontWeight.Medium,
                        color = if (reverseSpeed == speedVal) CG.AccL else CG.T2)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Preview toggle
        ToggleRow("预览倒放效果", previewOn) { previewOn = it }
        Spacer(modifier = Modifier.height(16.dp))

        // Apply button
        ApplyButton("应用倒放") {
            val clip = vm.selectedClip()
            if (clip != null) {
                if (reverseEntireClip) {
                    bridge.applyReverse(vm,
                        onComplete = { vm.showToast("倒放完成") },
                        onError = { vm.showToast("倒放失败: $it") }
                    )
                } else {
                    val params = mapOf<String, Any>(
                        "reverseEntire" to false,
                        "segmentStart" to segmentStart / 100f,
                        "segmentEnd" to segmentEnd / 100f,
                        "speed" to reverseSpeed / 100f
                    )
                    val result = bridge.applyEffect("reverse_segment", params)
                    if (result) {
                        vm.showToast("区间倒放已应用: ${segmentStart}%~${segmentEnd}% 速度${reverseSpeed / 100f}x")
                    } else {
                        vm.showToast("区间倒放应用失败")
                    }
                }
            } else {
                vm.showToast("请先选择片段")
            }
            onClose()
        }
    }
}
