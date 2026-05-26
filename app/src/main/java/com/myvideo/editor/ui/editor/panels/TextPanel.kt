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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun TextPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var text by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(48) }
    var fontColor by remember { mutableStateOf("#FFFFFF") }
    var selectedFont by remember { mutableStateOf("默认") }
    var selectedStyle by remember { mutableStateOf("无") }
    var selectedAnim by remember { mutableStateOf("无") }
    var bgColor by remember { mutableStateOf("#00000000") }
    var bgOpacity by remember { mutableStateOf(0) }
    var textShadow by remember { mutableStateOf(false) }
    var textStroke by remember { mutableStateOf(false) }
    var strokeColor by remember { mutableStateOf("#000000") }
    var strokeWidth by remember { mutableStateOf(2) }

    val fonts = listOf("默认", "粗体", "细体", "手写", "等宽", "衬线", "无衬线", "圆体")
    val styles = listOf("无", "阴影", "描边", "发光", "浮雕", "霓虹", "3D")
    val animations = listOf("无", "淡入", "打字机", "弹跳", "滑入", "逐字", "缩放", "旋转", "波浪", "闪烁")
    val colorPresets = listOf(
        "#FFFFFF", "#000000", "#FF0000", "#00FF00", "#0000FF", "#FFFF00",
        "#FF00FF", "#00FFFF", "#FFA500", "#800080", "#FFC0CB", "#A52A2A",
        "#E8A820", "#4A90D9", "#7EC850", "#E85050"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("文字内容", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Card).border(1.dp, CG.Line2, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.CenterStart) {
            Text(if (text.isEmpty()) "  输入文字..." else "  $text",
                fontSize = 12.sp, color = if (text.isEmpty()) CG.T3 else CG.T1)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("标题", "字幕", "片头", "片尾", "歌词", "水印").forEach { preset ->
                OptionChip(preset, false) {
                    text = when (preset) {
                        "标题" -> "标题文字"; "字幕" -> "这里是字幕"; "片头" -> "NexClip"
                        "片尾" -> "THE END"; "歌词" -> "♪ 歌词 ♪"; "水印" -> "NexClip"
                        else -> preset
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("字体", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            fonts.forEach { f -> OptionChip(f, selectedFont == f) { selectedFont = f } }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("字号", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("字号", 8, fontSize, 200) { fontSize = it }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("24", "36", "48", "72", "96", "120").forEach { s ->
                val v = s.toIntOrNull() ?: 48
                OptionChip(s, fontSize == v) { fontSize = v }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("颜色", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            colorPresets.forEach { c ->
                val sel = fontColor == c
                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                    .background(Color(android.graphics.Color.parseColor(c)))
                    .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp)) else Modifier)
                    .clickable { fontColor = c })
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("样式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            styles.forEach { s -> OptionChip(s, selectedStyle == s) { selectedStyle = s } }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("动画", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            animations.take(5).forEach { a -> OptionChip(a, selectedAnim == a) { selectedAnim = a } }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            animations.drop(5).forEach { a -> OptionChip(a, selectedAnim == a) { selectedAnim = a } }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("背景", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("背景透明度", 0, bgOpacity, 100) { bgOpacity = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("描边", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("描边宽度", 0, strokeWidth, 10) { strokeWidth = it }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("添加文字") {
            if (text.isEmpty()) { vm.showToast("请输入文字"); return@ApplyButton }
            bridge.addTextOverlay(vm, text, fontSize, fontColor,
                onComplete = { vm.showToast("文字已添加: $text") },
                onError = { vm.showToast("添加失败: $it") })
            onClose()
        }
    }
}
