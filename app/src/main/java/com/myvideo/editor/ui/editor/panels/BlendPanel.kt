package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlendPanel(onClose: () -> Unit = {}) {
    var mode by remember { mutableStateOf("正常") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("混合模式", fontSize = 9.sp, color = CG.T4, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val modes = listOf("正常", "正片叠底", "滤色", "叠加", "柔光", "强光", "差值", "排除", "色相", "饱和度", "颜色", "亮度")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            modes.take(4).forEach { m -> OptionChip(m, mode == m) { mode = m } }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            modes.drop(4).take(4).forEach { m -> OptionChip(m, mode == m) { mode = m } }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            modes.drop(8).forEach { m -> OptionChip(m, mode == m) { mode = m } }
        }
        Spacer(modifier = Modifier.height(14.dp))
        CgSlider("不透明度", 0, 100, 100)
        Spacer(modifier = Modifier.height(16.dp))
        ApplyButton("应用") { onClose() }
    }
}
