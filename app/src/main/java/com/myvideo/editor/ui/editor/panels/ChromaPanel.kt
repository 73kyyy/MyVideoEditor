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
fun ChromaPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var keyColor by remember { mutableStateOf("0x00FF00") }
    var similarity by remember { mutableStateOf(15) }
    var blend by remember { mutableStateOf(10) }
    var spillReduction by remember { mutableStateOf(0) }
    var edgeBlur by remember { mutableStateOf(0) }
    var foregroundGain by remember { mutableStateOf(100) }
    var backgroundBlur by remember { mutableStateOf(0) }
    var shadowRecovery by remember { mutableStateOf(0) }
    var highlightRecovery by remember { mutableStateOf(0) }

    val colorOptions = listOf(
        "绿幕" to "0x00FF00", "蓝幕" to "0x0000FF", "红幕" to "0xFF0000",
        "白幕" to "0xFFFFFF", "黑幕" to "0x000000", "自定义" to ""
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("绿幕抠像", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("移除背景颜色，替换为自定义背景", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("背景颜色", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            colorOptions.forEach { (name, color) ->
                OptionChip(name, keyColor == color) { keyColor = color }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("抠像参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("相似度", 1, similarity, 100) { similarity = it }
        CgSlider("混合", 0, blend, 100) { blend = it }
        CgSlider("溢色抑制", 0, spillReduction, 100) { spillReduction = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("边缘处理", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("边缘模糊", 0, edgeBlur, 20) { edgeBlur = it }
        CgSlider("前景增益", 50, foregroundGain, 200) { foregroundGain = it }
        CgSlider("背景模糊", 0, backgroundBlur, 20) { backgroundBlur = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("高级恢复", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("阴影恢复", 0, shadowRecovery, 100) { shadowRecovery = it }
        CgSlider("高光恢复", 0, highlightRecovery, 100) { highlightRecovery = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("预览", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(android.graphics.Color.parseColor(
                when (keyColor) {
                    "0x00FF00" -> "#00FF00"; "0x0000FF" -> "#0000FF"; "0xFF0000" -> "#FF0000"
                    "0xFFFFFF" -> "#FFFFFF"; "0x000000" -> "#000000"; else -> "#333333"
                }
            ))).border(1.dp, CG.Line, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center) {
            Text("背景将被移除", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用抠像") {
            bridge.applyChromaKey(vm, keyColor, similarity / 100f,
                onComplete = { vm.showToast("抠像完成") },
                onError = { vm.showToast("抠像失败: $it") })
            onClose()
        }
    }
}
