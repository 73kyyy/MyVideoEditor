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
    var selectedFilter by remember { mutableStateOf("自然") }
    var brightness by remember { mutableStateOf(50) }
    var contrast by remember { mutableStateOf(50) }
    var saturation by remember { mutableStateOf(50) }
    var temperature by remember { mutableStateOf(0) }
    var vignetteStrength by remember { mutableStateOf(0) }
    var grain by remember { mutableStateOf(0) }
    var sharpen by remember { mutableStateOf(0) }
    var lutPreset by remember { mutableStateOf("无") }
    var lutStrength by remember { mutableStateOf(100) }
    val tabs = listOf("滤镜", "色轮", "曲线", "HSL", "色阶", "LUT")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(30.dp).border(1.dp, CG.Line)) {
            tabs.forEachIndexed { i, t ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { tab = i },
                    contentAlignment = Alignment.Center) {
                    Text(t, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        color = if (tab == i) CG.Acc else CG.T3)
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(12.dp, 16.dp)
            .heightIn(max = 400.dp)) {
            when (tab) {
                0 -> FilterTab(selectedFilter) { selectedFilter = it }
                1 -> WheelsTab()
                2 -> CurvesTab()
                3 -> HSLTab()
                4 -> LevelsTab()
                5 -> LUTTab()
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ApplyButton("应用调色") {
            val filters = mutableListOf<String>()
            if (selectedFilter != "自然") filters.add(selectedFilter)
            if (brightness != 50) filters.add("brightness=${(brightness - 50) * 2}")
            if (contrast != 50) filters.add("contrast=${(contrast - 50) * 2}")
            if (saturation != 50) filters.add("saturation=${saturation * 2}")
            if (temperature != 0) filters.add("colortemperature=$temperature")
            if (sharpen > 0) filters.add("unsharp=$sharpen")
            if (vignetteStrength > 0) filters.add("vignette=$vignetteStrength")
            if (grain > 0) filters.add("noise=$grain")

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
        }
    }
}
