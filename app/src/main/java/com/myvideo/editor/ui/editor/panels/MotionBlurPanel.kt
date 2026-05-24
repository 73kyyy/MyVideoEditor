package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MotionBlurPanel(vm: com.myvideo.editor.ui.editor.EditorViewModel = com.myvideo.editor.ui.editor.EditorViewModel(), onClose: () -> Unit = {}) {
    var type by remember { mutableStateOf("方向模糊") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("模糊类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("方向模糊", "径向模糊", "旋转模糊", "自动运动模糊").forEach { t ->
                OptionChip(t, type == t) { type = t }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("模糊量", 0, 10, 50)
        CgSlider("方向", -180, 0, 180)
        CgSlider("采样数", 2, 8, 32)
        Spacer(modifier = Modifier.height(14.dp))

        Text("快门", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("快门角度", 0, 180, 360)
        CgSlider("偏移", -180, 0, 180)
        Spacer(modifier = Modifier.height(14.dp))

        Text("高级", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("衰减", 0, 50, 100)
        CgSlider("品质", 1, 5, 10)
        Spacer(modifier = Modifier.height(16.dp))
        ApplyButton("应用动态模糊") { onClose() }
    }
}
