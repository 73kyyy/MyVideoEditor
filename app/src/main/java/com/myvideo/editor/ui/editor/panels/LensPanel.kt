package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LensPanel(vm: com.myvideo.editor.ui.editor.EditorViewModel = com.myvideo.editor.ui.editor.EditorViewModel(), onClose: () -> Unit = {}) {
    var lensType by remember { mutableStateOf("50mm") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("畸变", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("桶形", -100, 0, 100)
        CgSlider("枕形", -100, 0, 100)
        Spacer(modifier = Modifier.height(14.dp))

        Text("色差", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("强度", 0, 0, 100)
        CgSlider("偏移", 0, 0, 20)
        Spacer(modifier = Modifier.height(14.dp))

        Text("暗角", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("暗角", 0, 30, 100)
        CgSlider("中点", 0, 50, 100)
        CgSlider("羽化", 0, 50, 100)
        Spacer(modifier = Modifier.height(14.dp))

        Text("光晕", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("无", "50mm", "105mm", "300mm").forEach { t ->
                OptionChip(t, lensType == t) { lensType = t }
            }
        }
        CgSlider("亮度", 0, 50, 100)
        Spacer(modifier = Modifier.height(16.dp))
        ApplyButton("应用镜头效果") { onClose() }
    }
}
