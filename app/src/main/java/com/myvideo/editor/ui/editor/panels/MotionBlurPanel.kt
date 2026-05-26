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
    var strength by remember { mutableStateOf(50) }
    var direction by remember { mutableStateOf("水平") }
    var angle by remember { mutableStateOf(0) }
    var shutterAngle by remember { mutableStateOf(180) }
    var samples by remember { mutableStateOf(5) }
    var centerBias by remember { mutableStateOf(0) }
    var zoomBlur by remember { mutableStateOf(0) }
    var radialAngle by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("动态模糊", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("模拟真实相机快门运动模糊效果", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("模糊类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("方向模糊", "径向模糊", "变焦模糊", "旋转模糊").forEach { t ->
                OptionChip(t, direction == t) { direction = t }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("强度", 0, strength, 100) { strength = it }
        CgSlider("采样数", 2, samples, 16) { samples = it }
        CgSlider("快门角度", 0, shutterAngle, 360) { shutterAngle = it }

        when (direction) {
            "方向模糊" -> {
                CgSlider("方向角度", 0, angle, 360) { angle = it }
                CgSlider("中心偏移", -100, centerBias, 100) { centerBias = it }
            }
            "径向模糊" -> {
                CgSlider("中心X", -100, 0, 100)
                CgSlider("中心Y", -100, 0, 100)
                CgSlider("径向角度", 0, radialAngle, 360) { radialAngle = it }
            }
            "变焦模糊" -> {
                CgSlider("变焦量", 0, zoomBlur, 100) { zoomBlur = it }
                CgSlider("中心X", -100, 0, 100)
                CgSlider("中心Y", -100, 0, 100)
            }
            "旋转模糊" -> {
                CgSlider("旋转角度", 0, radialAngle, 360) { radialAngle = it }
                CgSlider("中心X", -100, 0, 100)
                CgSlider("中心Y", -100, 0, 100)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("轻微", "中等", "强烈", "极速", "电影24帧").forEach { preset ->
                OptionChip(preset, false) {
                    when (preset) {
                        "轻微" -> { strength = 25; samples = 3 }
                        "中等" -> { strength = 50; samples = 5 }
                        "强烈" -> { strength = 75; samples = 8 }
                        "极速" -> { strength = 100; samples = 12 }
                        "电影24帧" -> { strength = 40; shutterAngle = 180; samples = 5 }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用动态模糊") {
            bridge.applyMotionBlur(vm, strength.toFloat(),
                onComplete = { vm.showToast("动态模糊已应用") },
                onError = { vm.showToast("应用失败: $it") })
            onClose()
        }
    }
}
