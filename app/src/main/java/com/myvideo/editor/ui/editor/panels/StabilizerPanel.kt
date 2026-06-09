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

    var strength by remember { mutableStateOf(50) }
    var cropMode by remember { mutableStateOf("自动") }
    var borderMode by remember { mutableStateOf("镜像") }
    var smoothingRadius by remember { mutableStateOf(10) }
    var rollingShutter by remember { mutableStateOf(false) }
    var previewBeforeAfter by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 标题
        Text("视频稳定器", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("消除手持拍摄抖动，让画面更平滑稳定", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // 稳定强度
        Text("稳定强度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("强度", 0, strength, 100) { strength = it }
        Text("${strength}%", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("轻微" to 25, "中等" to 50, "强力" to 75, "最大" to 100).forEach { (label, v) ->
                OptionChip(label, strength == v) { strength = v }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 裁切模式
        Text("裁切模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("自动", "手动").forEach { mode ->
                OptionChip(mode, cropMode == mode) { cropMode = mode }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 边界模式
        Text("边界模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("镜像", "黑色", "颜色").forEach { mode ->
                OptionChip(mode, borderMode == mode) { borderMode = mode }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 平滑半径
        Text("平滑半径", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("半径", 2, smoothingRadius, 30) { smoothingRadius = it }
        Text("${smoothingRadius}帧", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // 果冻效应校正
        ToggleRow("果冻效应校正", rollingShutter) { rollingShutter = it }
        Spacer(modifier = Modifier.height(10.dp))

        // 前后对比预览
        ToggleRow("前后对比预览", previewBeforeAfter) { previewBeforeAfter = it }
        Spacer(modifier = Modifier.height(14.dp))

        // 预览信息
        Text("参数信息", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .padding(12.dp)) {
            Column {
                StabInfoRow("稳定强度", "$strength%")
                StabInfoRow("裁切模式", cropMode)
                StabInfoRow("边界模式", borderMode)
                StabInfoRow("平滑半径", "${smoothingRadius}帧")
                StabInfoRow("果冻校正", if (rollingShutter) "开启" else "关闭")
                StabInfoRow("状态", if (isProcessing) "处理中..." else "就绪")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 应用按钮
        ApplyButton(if (isProcessing) "处理中..." else "应用稳定") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            bridge.applyEffect("stabilize", mapOf(
                "strength" to (strength / 100f),
                "cropMode" to cropMode,
                "borderMode" to borderMode,
                "smoothingRadius" to smoothingRadius,
                "rollingShutter" to rollingShutter
            ))
            bridge.applyStabilize(vm, smoothingRadius,
                onComplete = { isProcessing = false; vm.showToast("稳定完成") },
                onError = { isProcessing = false; vm.showToast("稳定失败: $it") })
            onClose()
        }
    }
}

@Composable
private fun StabInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 9.sp, color = CG.T3)
        Text(value, fontSize = 9.sp, color = CG.T1)
    }
}
