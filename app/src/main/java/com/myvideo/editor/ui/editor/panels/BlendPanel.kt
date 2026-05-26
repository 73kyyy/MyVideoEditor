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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun BlendPanel(vm: EditorViewModel, onClose: () -> Unit) {
    var selectedMode by remember { mutableStateOf("正常") }
    var opacity by remember { mutableStateOf(100) }
    var fillOpacity by remember { mutableStateOf(100) }
    var knockout by remember { mutableStateOf(false) }
    var blendColor by remember { mutableStateOf("#FFFFFF") }

    val blendModes = listOf(
        "正常" to Color(0xFF666666), "变暗" to Color(0xFF3A3A3A), "正片叠底" to Color(0xFF2A2A2A),
        "颜色加深" to Color(0xFF1A1A1A), "线性加深" to Color(0xFF0A0A0A),
        "变亮" to Color(0xFFAAAAAA), "滤色" to Color(0xFFCCCCCC), "颜色减淡" to Color(0xFFE0E0E0),
        "线性减淡" to Color(0xFFF0F0F0), "叠加" to Color(0xFF886644), "柔光" to Color(0xFF997755),
        "强光" to Color(0xFFAA6633), "亮光" to Color(0xFFBB5522), "线性光" to Color(0xFFCC4411),
        "点光" to Color(0xFFDD3300), "实色混合" to Color(0xFFEE2200),
        "差值" to Color(0xFF5555AA), "排除" to Color(0xFF777788), "减去" to Color(0xFF444466),
        "划分" to Color(0xFF888899), "色相" to Color(0xFF44AA66), "饱和度" to Color(0xFFAA6644),
        "颜色" to Color(0xFF6644AA), "明度" to Color(0xFFAA8866)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("混合模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("控制片段与下方图层的混合方式", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(10.dp))

        val categories = listOf(
            "常用" to listOf("正常", "叠加", "柔光", "强光", "滤色", "正片叠底"),
            "变暗" to listOf("变暗", "正片叠底", "颜色加深", "线性加深"),
            "变亮" to listOf("变亮", "滤色", "颜色减淡", "线性减淡"),
            "对比" to listOf("叠加", "柔光", "强光", "亮光", "线性光", "点光", "实色混合"),
            "差值" to listOf("差值", "排除", "减去", "划分"),
            "HSL" to listOf("色相", "饱和度", "颜色", "明度")
        )

        categories.forEach { (catName, modes) ->
            Text(catName, fontSize = 8.sp, color = CG.T3, modifier = Modifier.padding(top = 6.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                modes.forEach { mode ->
                    val sel = selectedMode == mode
                    val bgColor = blendModes.find { it.first == mode }?.second ?: CG.Card
                    Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(6.dp))
                        .background(bgColor.copy(alpha = if (sel) 0.8f else 0.3f))
                        .then(if (sel) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(6.dp)) else Modifier)
                        .clickable { selectedMode = mode },
                        contentAlignment = Alignment.Center) {
                        Text(mode, fontSize = 7.sp, color = if (sel) Color.White else CG.T2, maxLines = 1)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        CgSlider("不透明度", 0, opacity, 100) { opacity = it }
        CgSlider("填充不透明度", 0, fillOpacity, 100) { fillOpacity = it }
        Spacer(modifier = Modifier.height(10.dp))

        Text("高级选项", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("挖空", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (knockout) CG.Acc else CG.Line)
                .clickable { knockout = !knockout },
                contentAlignment = if (knockout) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用混合模式") {
            vm.showToast("混合模式: $selectedMode 不透明度: $opacity%")
            onClose()
        }
    }
}
