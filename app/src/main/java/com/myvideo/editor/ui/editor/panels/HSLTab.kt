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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HSLTab() {
    var selectedColor by remember { mutableStateOf("红") }
    var mode by remember { mutableStateOf("色相") }
    val colorEntries = listOf(
        "红" to Color(0xFFEF5350), "橙" to Color(0xFFFF7043), "黄" to Color(0xFFFFCA28),
        "绿" to Color(0xFF66BB6A), "青" to Color(0xFF26C6DA), "蓝" to Color(0xFF42A5F5),
        "紫" to Color(0xFFAB47BC), "洋红" to Color(0xFFEC407A)
    )
    val colorParams = remember { mutableStateMapOf<String, Triple<Int, Int, Int>>() }
    val current = colorParams[selectedColor] ?: Triple(0, 0, 0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("HSL 调整", fontSize = 9.sp, color = CG.T4, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            colorEntries.forEach { (name, color) ->
                val sel = selectedColor == name
                Box(modifier = Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = if (sel) 0.8f else 0.2f))
                    .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(6.dp)) else Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(6.dp)))
                    .clickable { selectedColor = name },
                    contentAlignment = Alignment.Center) {
                    Text(name, fontSize = 8.sp, color = if (sel) Color.White else color)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("色相", "饱和", "明度").forEach { m ->
                OptionChip(m, mode == m) { mode = m }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("$selectedColor - $mode", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        when (mode) {
            "色相" -> CgSlider("色相偏移", -180, current.first, 180)
            "饱和" -> CgSlider("饱和度", -100, current.second, 100)
            "明度" -> CgSlider("明度", -100, current.third, 100)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("色相范围", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("中心", 0, 0, 360)
        CgSlider("宽度", 10, 30, 180)
        CgSlider("羽化", 0, 15, 100)
        Spacer(modifier = Modifier.height(14.dp))
        Text("全局调整", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("自然饱和", -100, 0, 100)
        CgSlider("全局饱和", -100, 0, 100)
        CgSlider("鲜艳度", 0, 0, 100)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("重置当前", fontSize = 9.sp, color = CG.Acc, modifier = Modifier.clickable {
                colorParams[selectedColor] = Triple(0, 0, 0)
            })
            Text("重置全部", fontSize = 9.sp, color = CG.T3, modifier = Modifier.clickable {
                colorEntries.forEach { (name, _) -> colorParams[name] = Triple(0, 0, 0) }
            })
        }
    }
}
