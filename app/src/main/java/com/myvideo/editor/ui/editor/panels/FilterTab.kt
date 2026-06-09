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
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class FilterPreset(
    val name: String,
    val color1: Color,
    val color2: Color,
    val filterCommand: String
)

@Composable
fun FilterTab(vm: EditorViewModel, bridge: EditorBridge) {
    val trackId = vm.selectedClipId ?: "default"
    var selected by remember { mutableStateOf("无") }
    var intensity by remember { mutableStateOf(100) }

    val filters = listOf(
        FilterPreset("无", Color(0xFF2C2C2C), Color(0xFF3C3C3C), "eq=brightness=0:contrast=1:saturation=1"),
        FilterPreset("鲜艳", Color(0xFFFF7043), Color(0xFFE64A19), "eq=saturation=1.8:contrast=1.1"),
        FilterPreset("暖色", Color(0xFFE8A820), Color(0xFFFF8F00), "colorbalance=rs=0.15:rm=0.08"),
        FilterPreset("冷色", Color(0xFF4A90D9), Color(0xFF1565C0), "colorbalance=bs=0.15:bm=0.08"),
        FilterPreset("复古", Color(0xFFD4A574), Color(0xFF8D6E63), "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131"),
        FilterPreset("胶片", Color(0xFF8D6E63), Color(0xFF5D4037), "eq=contrast=1.2:brightness=-0.05:saturation=0.9"),
        FilterPreset("黑白", Color(0xFF616161), Color(0xFF9E9E9E), "hue=s=0"),
        FilterPreset("棕褐", Color(0xFFA1887F), Color(0xFF6D4C41), "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131"),
        FilterPreset("褪色", Color(0xFFB0BEC5), Color(0xFFCFD8DC), "eq=contrast=0.8:saturation=0.6:brightness=0.05"),
        FilterPreset("铬色", Color(0xFF78909C), Color(0xFF37474F), "eq=contrast=1.3:saturation=0.7"),
        FilterPreset("冲印", Color(0xFF7E57C2), Color(0xFF4527A0), "eq=contrast=1.1:saturation=0.8:gamma=0.9"),
        FilterPreset("转印", Color(0xFFEF5350), Color(0xFFC62828), "curves=preset=cross_process"),
        FilterPreset("即时", Color(0xFFFFCA28), Color(0xFFF9A825), "eq=contrast=1.15:brightness=0.08:saturation=1.2"),
        FilterPreset("单色", Color(0xFF455A64), Color(0xFF263238), "hue=s=0,eq=contrast=1.2"),
        FilterPreset("黑色", Color(0xFF1A1A1A), Color(0xFF212121), "hue=s=0,eq=contrast=1.5:brightness=-0.1"),
        FilterPreset("色调", Color(0xFF26A69A), Color(0xFF00695C), "hue=s=0,eq=contrast=1.1:brightness=0.05")
    )

    fun applyFilter() {
        val filter = filters.find { it.name == selected } ?: return
        bridge.setTrackProperty(trackId, "filter_intensity", intensity.toFloat() / 100f)
        if (selected != "无") {
            bridge.applyFilter(vm, filter.filterCommand,
                onComplete = { vm.showToast("滤镜已应用: $selected") },
                onError = { vm.showToast("滤镜应用失败: $it") })
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("滤镜预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // 2-column grid of filter thumbnails
        filters.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { f ->
                    val sel = selected == f.name
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(f.color1, f.color2)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { selected = f.name },
                            contentAlignment = Alignment.Center) {
                            Text(f.name, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                // Fill empty cells
                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("强度", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("滤镜强度", 0, intensity, 100) { intensity = it }
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CG.Acc).clickable { applyFilter() },
                contentAlignment = Alignment.Center) {
                Text("应用", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
                .clickable {
                    selected = "无"; intensity = 100
                },
                contentAlignment = Alignment.Center) {
                Text("重置", fontSize = 11.sp, color = CG.T2, fontWeight = FontWeight.Medium)
            }
        }
    }
}
