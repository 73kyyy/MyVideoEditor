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
fun StabilizerPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var smoothing by remember { mutableStateOf(10) }
    var method by remember { mutableStateOf("vidstab") }
    var cropMode by remember { mutableStateOf("保持边界") }
    var zoom by remember { mutableStateOf(0) }
    var maxShift by remember { mutableStateOf(0) }
    var accuracy by remember { mutableStateOf(15) }
    var shakiness by remember { mutableStateOf(5) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("视频稳定器", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("消除手持拍摄抖动，让画面更平滑稳定", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("检测算法", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("vidstab", "运动补偿", "陀螺仪").forEach { m ->
                OptionChip(m, method == m) { method = m }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("平滑度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("平滑窗口", 2, smoothing, 30) { smoothing = it }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("5" to 5, "10" to 10, "15" to 15, "20" to 20, "30" to 30).forEach { (label, v) ->
                OptionChip(label, smoothing == v) { smoothing = v }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("抖动检测", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("灵敏度", 1, shakiness, 10) { shakiness = it }
        CgSlider("精度", 1, accuracy, 30) { accuracy = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("裁切模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("保持边界", "缩放填充", "黑色填充").forEach { m ->
                OptionChip(m, cropMode == m) { cropMode = m }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        CgSlider("额外缩放", 0, zoom, 20) { zoom = it }
        CgSlider("最大平移", 0, maxShift, 50) { maxShift = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("预览信息", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .padding(12.dp)) {
            Column {
                InfoRow("算法", method)
                InfoRow("平滑窗口", "${smoothing}帧")
                InfoRow("抖动检测", "灵敏度$shakiness 精度$accuracy")
                InfoRow("裁切", cropMode)
                InfoRow("状态", if (isProcessing) "处理中..." else "就绪")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton(if (isProcessing) "处理中..." else "开始稳定") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            bridge.applyStabilize(vm, smoothing,
                onComplete = { isProcessing = false; vm.showToast("稳定完成") },
                onError = { isProcessing = false; vm.showToast("稳定失败: $it") })
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
