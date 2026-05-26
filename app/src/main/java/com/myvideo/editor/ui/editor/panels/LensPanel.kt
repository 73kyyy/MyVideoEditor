package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun LensPanel(vm: EditorViewModel, onClose: () -> Unit) {
    var selectedEffect by remember { mutableStateOf("无") }
    var flareX by remember { mutableStateOf(50) }
    var flareY by remember { mutableStateOf(30) }
    var flareIntensity by remember { mutableStateOf(50) }
    var flareSize by remember { mutableStateOf(30) }
    var flareColor by remember { mutableStateOf("#FFD700") }
    var anamorphicRatio by remember { mutableStateOf(2.4f) }
    var prismRotation by remember { mutableStateOf(0) }
    var prismIntensity by remember { mutableStateOf(50) }
    var haloSize by remember { mutableStateOf(40) }
    var haloIntensity by remember { mutableStateOf(30) }
    var dirtIntensity by remember { mutableStateOf(20) }
    var aberration by remember { mutableStateOf(0) }

    val effects = listOf(
        "无" to Color(0xFF2C2C2C), "光晕" to Color(0xFFFFD700), "镜头光斑" to Color(0xFFFFA500),
        "棱镜" to Color(0xFF7B68EE), "色差" to Color(0xFFFF4500), "光圈形变" to Color(0xFF4169E1),
        "变形宽银幕" to Color(0xFF2E8B57), "星芒" to Color(0xFFFFD700), "彩虹光斑" to Color(0xFFFF69B4),
        "柔焦" to Color(0xFFFFB6C1), "镜头灰尘" to Color(0xFF696969), "光管" to Color(0xFF00CED1)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("镜头效果", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("模拟真实镜头的光学效果", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(10.dp))

        effects.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (name, c) ->
                    val sel = selectedEffect == name
                    Box(modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                        .background(c.copy(alpha = if (sel) 0.5f else 0.15f))
                        .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                        .clickable { selectedEffect = name },
                        contentAlignment = Alignment.Center) {
                        Text(name, fontSize = 8.sp, color = Color.White)
                    }
                }
                repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))

        when (selectedEffect) {
            "光晕", "镜头光斑" -> {
                Text("光斑参数", fontSize = 9.sp, color = CG.T4)
                CgSlider("X位置", 0, flareX, 100) { flareX = it }
                CgSlider("Y位置", 0, flareY, 100) { flareY = it }
                CgSlider("强度", 0, flareIntensity, 100) { flareIntensity = it }
                CgSlider("大小", 5, flareSize, 100) { flareSize = it }
                Text("光斑颜色", fontSize = 9.sp, color = CG.T4)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("#FFD700", "#FF6347", "#87CEEB", "#FF69B4", "#FFFFFF", "#00FFFF").forEach { c ->
                        val sel = flareColor == c
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(c)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                            .clickable { flareColor = c })
                    }
                }
            }
            "棱镜" -> {
                Text("棱镜参数", fontSize = 9.sp, color = CG.T4)
                CgSlider("旋转", 0, prismRotation, 360) { prismRotation = it }
                CgSlider("强度", 0, prismIntensity, 100) { prismIntensity = it }
            }
            "变形宽银幕" -> {
                Text("宽银幕参数", fontSize = 9.sp, color = CG.T4)
                CgSlider("比例", 10, (anamorphicRatio * 10).toInt(), 40) { anamorphicRatio = it / 10f }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("1.33x", "1.5x", "2.0x", "2.35x", "2.76x").forEach { r ->
                        OptionChip(r, false) { anamorphicRatio = r.replace("x", "").toFloatOrNull() ?: 2.4f }
                    }
                }
            }
            "柔焦" -> {
                Text("柔焦参数", fontSize = 9.sp, color = CG.T4)
                CgSlider("光晕大小", 0, haloSize, 100) { haloSize = it }
                CgSlider("光晕强度", 0, haloIntensity, 100) { haloIntensity = it }
            }
            "镜头灰尘" -> {
                Text("灰尘参数", fontSize = 9.sp, color = CG.T4)
                CgSlider("灰尘强度", 0, dirtIntensity, 100) { dirtIntensity = it }
            }
            "色差" -> {
                Text("色差参数", fontSize = 9.sp, color = CG.T4)
                CgSlider("偏移量", 0, aberration, 20) { aberration = it }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用镜头效果") {
            vm.showToast("镜头效果已应用: $selectedEffect")
            onClose()
        }
    }
}
