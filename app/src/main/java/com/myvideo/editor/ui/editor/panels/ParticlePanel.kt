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
fun ParticlePanel(vm: EditorViewModel, onClose: () -> Unit) {
    var selectedEffect by remember { mutableStateOf("雪花") }
    var count by remember { mutableStateOf(50) }
    var speed by remember { mutableStateOf(50) }
    var size by remember { mutableStateOf(10) }
    var opacity by remember { mutableStateOf(80) }
    var gravity by remember { mutableStateOf(30) }
    var spread by remember { mutableStateOf(50) }
    var rotation by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf("#FFFFFF") }
    var lifetime by remember { mutableStateOf(100) }
    var fadeOut by remember { mutableStateOf(true) }

    val effects = listOf(
        "雪花" to Color(0xFFE0E8FF), "雨滴" to Color(0xFF80B0D0), "火焰" to Color(0xFFFF6633),
        "星星" to Color(0xFFFFDD44), "光斑" to Color(0xFFFFAA88), "花瓣" to Color(0xFFFFB0C0),
        "烟雾" to Color(0xFF888888), "火花" to Color(0xFFFFAA00), "气泡" to Color(0xFF80DDFF),
        "落叶" to Color(0xFFBB8833), "尘埃" to Color(0xFFAA9977), "光粒子" to Color(0xFFFFEECC),
        "萤火虫" to Color(0xFFAAFF66), "心形" to Color(0xFFFF4488), "音符" to Color(0xFF8866FF),
        "蝴蝶" to Color(0xFF66AADD)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("粒子效果", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("为画面添加动态粒子特效", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(10.dp))

        effects.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (name, color2) ->
                    val sel = selectedEffect == name
                    Box(modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                        .background(color2.copy(alpha = if (sel) 0.4f else 0.15f))
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

        Text("粒子参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("数量", 5, count, 200) { count = it }
        CgSlider("速度", 1, speed, 100) { speed = it }
        CgSlider("大小", 1, size, 50) { size = it }
        CgSlider("透明度", 10, opacity, 100) { opacity = it }
        CgSlider("重力", -100, gravity, 100) { gravity = it }
        CgSlider("扩散", 0, spread, 100) { spread = it }
        CgSlider("旋转", -180, rotation, 180) { rotation = it }
        CgSlider("存活时间", 20, lifetime, 300) { lifetime = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("预设", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("暴风雪" to Triple(200, 80, 80), "小雨" to Triple(80, 40, 30),
                "烟花" to Triple(100, 100, 5), "萤火虫" to Triple(30, 20, 50)).forEach { (name, params) ->
                OptionChip(name, false) { count = params.first; speed = params.second; gravity = params.third }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("添加粒子效果") {
            vm.showToast("粒子效果已添加: $selectedEffect x$count")
            onClose()
        }
    }
}
