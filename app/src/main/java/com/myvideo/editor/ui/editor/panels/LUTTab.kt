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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LUTTab() {
    var selectedLut by remember { mutableStateOf("无") }
    var strength by remember { mutableStateOf(100) }
    var sharpenAmount by remember { mutableStateOf(0) }
    var sharpenRadius by remember { mutableStateOf(1) }
    var dehaloAmount by remember { mutableStateOf(0) }
    var dehaloRadius by remember { mutableStateOf(2) }
    var denoiseAmount by remember { mutableStateOf(0) }

    val luts = listOf(
        Triple("无", Color(0xFF2C2C2C), Color(0xFF2C2C2C)),
        Triple("电影", Color(0xFF2A1A0A), Color(0xFF4A3020)),
        Triple("冷色", Color(0xFF0A1A2E), Color(0xFF1A3A5E)),
        Triple("暖色", Color(0xFF2E1A0A), Color(0xFF5E3A1A)),
        Triple("胶片", Color(0xFF1A1A1A), Color(0xFF3A3020)),
        Triple("复古", Color(0xFF2A2015), Color(0xFF5A4A35)),
        Triple("清新", Color(0xFF1A3A1A), Color(0xFFD0E8D0)),
        Triple("赛博", Color(0xFF0A0A2E), Color(0xFF2A0A4E)),
        Triple("日系", Color(0xFFE8E0D0), Color(0xFFF0E8E0)),
        Triple("黑金", Color(0xFF1A1A0A), Color(0xFFE8A820)),
        Triple("青橙", Color(0xFF0A2A2A), Color(0xFFE86A20)),
        Triple("粉紫", Color(0xFF2A0A2A), Color(0xFFE870A0)),
        Triple("森林", Color(0xFF0A1A0A), Color(0xFF2A4A1A)),
        Triple("海洋", Color(0xFF0A1A2E), Color(0xFF1A5A7E)),
        Triple("黄昏", Color(0xFF2E1A0A), Color(0xFFE85030)),
        Triple("雪景", Color(0xFFD0D8E0), Color(0xFFF0F4F8)),
        Triple("电影蓝", Color(0xFF0A1020), Color(0xFF2A3A5E)),
        Triple("暗调", Color(0xFF0A0A0A), Color(0xFF1A1A1A)),
        Triple("高光暖", Color(0xFF2E1A0A), Color(0xFFE8C080)),
        Triple("街头", Color(0xFF1A1A1A), Color(0xFF4A4A3A)),
        Triple("LOMO", Color(0xFF1A0A0A), Color(0xFFE86020))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("LUT 预设", fontSize = 9.sp, color = CG.T4, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        luts.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (name, c1, c2) ->
                    val sel = selectedLut == name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(c1, c2)))
                            .then(if (sel) Modifier.border(2.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { selectedLut = name },
                            contentAlignment = Alignment.Center) {
                            if (name == "无") Text("无", fontSize = 8.sp, color = CG.T2)
                        }
                        Text(name, fontSize = 6.sp, color = if (sel) CG.AccL else CG.T3,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                }
                repeat(4 - row.size) {
                    Column { Spacer(modifier = Modifier.weight(1f).fillMaxWidth()) }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("LUT 强度", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("强度", 0, strength, 100) { strength = it }
        Spacer(modifier = Modifier.height(14.dp))
        Text("锐化", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("锐化量", 0, sharpenAmount, 100) { sharpenAmount = it }
        CgSlider("半径", 1, sharpenRadius, 5) { sharpenRadius = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("去光晕", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("强度", 0, dehaloAmount, 100) { dehaloAmount = it }
        CgSlider("半径", 1, dehaloRadius, 10) { dehaloRadius = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("降噪", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("降噪量", 0, denoiseAmount, 100) { denoiseAmount = it }
        Spacer(modifier = Modifier.height(8.dp))
        Text("重置", fontSize = 9.sp, color = CG.Acc, modifier = Modifier.clickable {
            selectedLut = "无"; strength = 100; sharpenAmount = 0; sharpenRadius = 1
            dehaloAmount = 0; dehaloRadius = 2; denoiseAmount = 0
        })
    }
}
