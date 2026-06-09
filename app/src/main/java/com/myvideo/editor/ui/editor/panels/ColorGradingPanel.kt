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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun ColorGradingPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    var tab by remember { mutableStateOf(0) }
    var intensity by remember { mutableStateOf(100) }
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("色轮", "曲线", "HSL", "色阶", "LUT")

    Column(modifier = Modifier.fillMaxWidth()) {
        // 预设芯片
        Text("预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("电影感", "复古", "暖色", "冷色", "青橙", "黑白").forEach { preset ->
                val sel = selectedPreset == preset
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (sel) CG.AccS else CG.Card)
                    .then(if (sel) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                    .clickable {
                        selectedPreset = if (sel) null else preset
                        if (!sel) {
                            bridge.applyEffect("color_preset", mapOf("preset" to preset, "intensity" to (intensity / 100f)))
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(preset, fontSize = 10.sp, color = if (sel) CG.AccL else CG.T2, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 标签页
        Row(modifier = Modifier.fillMaxWidth().height(30.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))) {
            tabs.forEachIndexed { i, t ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (tab == i) CG.AccS else Color.Transparent)
                    .clickable { tab = i },
                    contentAlignment = Alignment.Center) {
                    Text(t, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        color = if (tab == i) CG.Acc else CG.T3)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 标签页内容
        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
            when (tab) {
                0 -> WheelsTab(vm, bridge)
                1 -> CurvesTab(vm, bridge)
                2 -> HSLTab(vm, bridge)
                3 -> LevelsTab(vm, bridge)
                4 -> LUTTab()
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 全局强度
        Text("全局强度", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("强度", 0, intensity, 100) { intensity = it }
        Text("${intensity}%", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        // 重置按钮
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
                .clickable {
                    intensity = 100
                    selectedPreset = null
                    bridge.applyEffect("color_reset", emptyMap())
                    vm.showToast("调色已重置")
                },
                contentAlignment = Alignment.Center) {
                Text("重置", fontSize = 11.sp, color = CG.T2, fontWeight = FontWeight.Medium)
            }

            // 应用按钮
            Box(modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CG.Acc).clickable {
                    val filters = mutableListOf<String>()
                    selectedPreset?.let { filters.add("preset=$it") }
                    if (intensity != 100) filters.add("intensity=${intensity / 100f}")
                    if (filters.isEmpty()) {
                        vm.showToast("请调整参数")
                    } else if (filters.size == 1) {
                        bridge.applyFilter(vm, filters[0],
                            onComplete = { vm.showToast("调色完成") },
                            onError = { vm.showToast("调色失败: $it") })
                    } else {
                        bridge.applyMultipleFilters(vm, filters,
                            onComplete = { vm.showToast("调色完成") },
                            onError = { vm.showToast("调色失败: $it") })
                    }
                    onClose()
                },
                contentAlignment = Alignment.Center) {
                Text("应用调色", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
