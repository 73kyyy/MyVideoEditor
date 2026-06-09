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
fun MotionBlurPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var blurType by remember { mutableStateOf("方向模糊") }
    var strength by remember { mutableStateOf(50) }
    var direction by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(0) }
    var centerX by remember { mutableStateOf(0) }
    var centerY by remember { mutableStateOf(0) }
    var quality by remember { mutableStateOf("中") }
    var previewOn by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("动态模糊", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("模拟真实相机快门运动模糊效果", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // Blur type selection
        Text("模糊类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("方向模糊", "径向模糊", "缩放模糊", "旋转模糊").forEach { t ->
                OptionChip(t, blurType == t) { blurType = t }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Common parameter: strength
        Text("参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("强度", 0, strength, 100) { strength = it }

        // Type-specific parameters
        when (blurType) {
            "方向模糊" -> {
                CgSlider("方向", 0, direction, 360) { direction = it }
            }
            "径向模糊" -> {
                CgSlider("角度", 0, angle, 360) { angle = it }
                CgSlider("中心X", -100, centerX, 100) { centerX = it }
                CgSlider("中心Y", -100, centerY, 100) { centerY = it }
            }
            "缩放模糊" -> {
                CgSlider("角度", 0, angle, 360) { angle = it }
                CgSlider("中心X", -100, centerX, 100) { centerX = it }
                CgSlider("中心Y", -100, centerY, 100) { centerY = it }
            }
            "旋转模糊" -> {
                CgSlider("角度", 0, angle, 360) { angle = it }
                CgSlider("中心X", -100, centerX, 100) { centerX = it }
                CgSlider("中心Y", -100, centerY, 100) { centerY = it }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Quality selection
        Text("质量", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("低", "中", "高").forEach { q ->
                OptionChip(q, quality == q) { quality = q }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Preview toggle
        ToggleRow("实时预览", previewOn) { previewOn = it }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用动态模糊") {
            val qualitySamples = when (quality) {
                "低" -> 3
                "中" -> 5
                else -> 8
            }
            val params = mapOf<String, Any>(
                "type" to blurType,
                "strength" to strength / 100f,
                "quality" to quality,
                "samples" to qualitySamples,
                "direction" to direction,
                "angle" to angle,
                "centerX" to centerX / 100f,
                "centerY" to centerY / 100f,
                "preview" to previewOn
            )
            val result = bridge.applyEffect("motion_blur", params)
            if (result) {
                vm.showToast("动态模糊已应用: $blurType 强度${strength}%")
            } else {
                vm.showToast("应用动态模糊失败")
            }
            onClose()
        }
    }
}
