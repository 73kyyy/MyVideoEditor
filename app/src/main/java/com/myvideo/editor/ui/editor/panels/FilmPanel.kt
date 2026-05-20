package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilmPanel(onClose: () -> Unit = {}) {
    var filmType by remember { mutableStateOf("35mm") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("颗粒", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("强度", 0, 30, 100)
        CgSlider("大小", 1, 3, 10)
        Spacer(modifier = Modifier.height(14.dp))

        Text("胶片类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("35mm", "16mm", "8mm", "Super 8").forEach { t ->
                OptionChip(t, filmType == t) { filmType = t }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("划痕", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("数量", 0, 0, 20)
        CgSlider("长度", 0, 30, 100)
        Spacer(modifier = Modifier.height(14.dp))

        Text("闪烁", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        CgSlider("亮度闪烁", 0, 0, 50)
        CgSlider("频率", 0, 30, 100)
        Spacer(modifier = Modifier.height(14.dp))

        CgSlider("暗角", 0, 20, 100)
        Spacer(modifier = Modifier.height(16.dp))
        ApplyButton("应用胶片颗粒") { onClose() }
    }
}
