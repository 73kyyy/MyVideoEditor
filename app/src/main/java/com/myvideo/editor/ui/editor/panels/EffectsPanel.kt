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
fun EffectsPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    var basicSel by remember { mutableStateOf(setOf<String>()) }
    var styleSel by remember { mutableStateOf(setOf<String>()) }
    var deformSel by remember { mutableStateOf(setOf<String>()) }
    var blurRadius by remember { mutableStateOf(0) }
    var sharpenStrength by remember { mutableStateOf(0) }
    var glowStrength by remember { mutableStateOf(0) }
    var mosaicSize by remember { mutableStateOf(10) }
    var chromaShift by remember { mutableStateOf(0) }
    var noiseAmount by remember { mutableStateOf(0) }
    var glitchIntensity by remember { mutableStateOf(50) }
    var vignetteStrength by remember { mutableStateOf(0) }

    val presets = listOf(
        "电影感" to listOf("柔光", "电影色", "暗角"),
        "复古胶片" to listOf("复古", "胶片", "老电影"),
        "赛博朋克" to listOf("霓虹", "故障", "色差"),
        "日系清新" to listOf("柔光", "清新"),
        "黑白经典" to listOf("黑白", "高对比"),
        "抖音风格" to listOf("鲜艳", "锐化"),
        "梦幻" to listOf("模糊", "发光", "柔光"),
        "恐怖" to listOf("暗调", "色差", "老电影")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("预设滤镜组", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        presets.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (name, _) ->
                    val sel = selectedPreset == name
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (sel) CG.AccS else CG.Card)
                        .then(if (sel) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
                        .clickable { selectedPreset = if (sel) null else name }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center) {
                        Text(name, fontSize = 10.sp, color = if (sel) CG.AccL else CG.T2, fontWeight = FontWeight.Medium)
                    }
                }
                repeat(2 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("基础", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val basics = listOf("模糊", "锐化", "发光", "阴影", "浮雕", "马赛克", "像素化", "色差", "暗角", "噪声", "高对比", "柔光")
        basics.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { f ->
                    EffectChip(f, basicSel.contains(f)) {
                        basicSel = if (basicSel.contains(f)) basicSel - f else basicSel + f
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (basicSel.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            basicSel.forEach { effect ->
                when (effect) {
                    "模糊" -> CgSlider("模糊半径", 1, blurRadius, 20) { blurRadius = it }
                    "锐化" -> CgSlider("锐化强度", 0, sharpenStrength, 100) { sharpenStrength = it }
                    "发光" -> CgSlider("发光强度", 0, glowStrength, 100) { glowStrength = it }
                    "马赛克" -> CgSlider("马赛克大小", 2, mosaicSize, 50) { mosaicSize = it }
                    "色差" -> CgSlider("色差偏移", 0, chromaShift, 20) { chromaShift = it }
                    "噪声" -> CgSlider("噪声量", 0, noiseAmount, 100) { noiseAmount = it }
                    "暗角" -> CgSlider("暗角强度", 0, vignetteStrength, 100) { vignetteStrength = it }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("风格化", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val styles = listOf("老电影", "故障", "复古", "霓虹", "油画", "素描", "卡通", "水墨", "清新", "胶片", "赛博", "黑白", "高对比", "暗调", "明亮", "怀旧")
        styles.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { f ->
                    EffectChip(f, styleSel.contains(f)) {
                        styleSel = if (styleSel.contains(f)) styleSel - f else styleSel + f
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (styleSel.contains("故障")) {
            CgSlider("故障强度", 1, glitchIntensity, 100) { glitchIntensity = it }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("变形", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val deforms = listOf("扭曲", "波纹", "球面化", "湍流置换", "鱼眼", "旋转模糊", "径向模糊", "万花筒")
        deforms.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { f ->
                    EffectChip(f, deformSel.contains(f)) {
                        deformSel = if (deformSel.contains(f)) deformSel - f else deformSel + f
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用效果") {
            val allEffects = basicSel + styleSel + deformSel
            if (allEffects.isEmpty() && selectedPreset == null) {
                vm.showToast("请选择效果")
                return@ApplyButton
            }
            val filterNames = allEffects.map { name ->
                when (name) {
                    "模糊" -> "blur=$blurRadius"
                    "锐化" -> "unsharp=$sharpenStrength"
                    "发光" -> "glow=$glowStrength"
                    "马赛克" -> "pixelize=$mosaicSize"
                    "色差" -> "chromatic=$chromaShift"
                    "噪声" -> "noise=$noiseAmount"
                    "暗角" -> "vignette=$vignetteStrength"
                    "黑白" -> "grayscale"
                    "复古" -> "sepia"
                    "老电影" -> "oldfilm"
                    "故障" -> "glitch=$glitchIntensity"
                    "霓虹" -> "neon"
                    "油画" -> "oil"
                    "素描" -> "sketch"
                    "卡通" -> "cartoon"
                    "水墨" -> "ink"
                    "清新" -> "fresh"
                    "胶片" -> "film"
                    "赛博" -> "cyber"
                    "高对比" -> "highcontrast"
                    "暗调" -> "darken"
                    "明亮" -> "brighten"
                    "怀旧" -> "vintage"
                    "浮雕" -> "emboss"
                    "像素化" -> "pixelate"
                    "阴影" -> "shadow"
                    "扭曲" -> "distort"
                    "波纹" -> "ripple"
                    "球面化" -> "spherical"
                    "鱼眼" -> "fisheye"
                    else -> name.lowercase()
                }
            }
            if (filterNames.size == 1) {
                bridge.applyFilter(vm, filterNames[0],
                    onComplete = { vm.showToast("效果已应用") },
                    onError = { vm.showToast("应用失败: $it") })
            } else {
                bridge.applyMultipleFilters(vm, filterNames,
                    onComplete = { vm.showToast("${filterNames.size}个效果已应用") },
                    onError = { vm.showToast("应用失败: $it") })
            }
            onClose()
        }
    }
}

@Composable
private fun EffectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
        .background(if (selected) CG.AccS else CG.Card)
        .then(if (selected) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
        .clickable { onClick() }
        .padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium,
            color = if (selected) CG.AccL else CG.T2)
    }
}
