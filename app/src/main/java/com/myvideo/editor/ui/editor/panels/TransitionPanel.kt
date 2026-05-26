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
fun TransitionPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var selected by remember { mutableStateOf("淡入淡出") }
    var duration by remember { mutableStateOf(500) }
    var easing by remember { mutableStateOf("线性") }
    var autoApply by remember { mutableStateOf(false) }

    data class TransitionItem(val name: String, val category: String, val color1: Color, val color2: Color)

    val transitions = listOf(
        TransitionItem("淡入淡出", "溶解", Color(0xFF4A4A4A), Color(0xFF888888)),
        TransitionItem("交叉溶解", "溶解", Color(0xFF5C6BC0), Color(0xFF7986CB)),
        TransitionItem("闪白", "溶解", Color(0xFFE0E0E0), Color(0xFFFFFFFF)),
        TransitionItem("抖动", "溶解", Color(0xFFEF5350), Color(0xFFE57373)),
        TransitionItem("滑动左", "滑动", Color(0xFF26A69A), Color(0xFF4DB6AC)),
        TransitionItem("滑动右", "滑动", Color(0xFF26A69A), Color(0xFF80CBC4)),
        TransitionItem("滑动上", "滑动", Color(0xFF42A5F5), Color(0xFF64B5F6)),
        TransitionItem("滑动下", "滑动", Color(0xFF42A5F5), Color(0xFF90CAF9)),
        TransitionItem("擦除", "擦除", Color(0xFFAB47BC), Color(0xFFBA68C8)),
        TransitionItem("擦除左", "擦除", Color(0xFFAB47BC), Color(0xFFCE93D8)),
        TransitionItem("擦除右", "擦除", Color(0xFF7E57C2), Color(0xFF9575CD)),
        TransitionItem("缩放", "缩放", Color(0xFFFFCA28), Color(0xFFFFE082)),
        TransitionItem("缩放入", "缩放", Color(0xFFFFA726), Color(0xFFFFB74D)),
        TransitionItem("缩放出", "缩放", Color(0xFFFF7043), Color(0xFFFF8A65)),
        TransitionItem("旋转", "旋转", Color(0xFF66BB6A), Color(0xFF81C784)),
        TransitionItem("旋转缩放", "旋转", Color(0xFF4CAF50), Color(0xFFA5D6A7)),
        TransitionItem("百叶窗", "图案", Color(0xFF78909C), Color(0xFF90A4AE)),
        TransitionItem("棋盘格", "图案", Color(0xFF8D6E63), Color(0xFFA1887F)),
        TransitionItem("径向", "图案", Color(0xFFEC407A), Color(0xFFF48FB1)),
        TransitionItem("菱形", "图案", Color(0xFF29B6F6), Color(0xFF81D4FA)),
        TransitionItem("推挤", "推挤", Color(0xFF5C6BC0), Color(0xFF9FA8DA)),
        TransitionItem("覆盖", "推挤", Color(0xFF26A69A), Color(0xFF80CBC4))
    )

    var category by remember { mutableStateOf("全部") }
    val categories = listOf("全部", "溶解", "滑动", "擦除", "缩放", "旋转", "图案", "推挤")
    val filtered = if (category == "全部") transitions else transitions.filter { it.category == category }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("转场类型", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            categories.forEach { cat ->
                OptionChip(cat, category == cat) { category = cat }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        filtered.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { t ->
                    val sel = selected == t.name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(t.color1, t.color2)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { selected = t.name },
                            contentAlignment = Alignment.Center) {
                            Text(t.name, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                repeat(4 - row.size) {
                    Column { Spacer(modifier = Modifier.weight(1f).fillMaxWidth().height(48.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("时长", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("转场时长", 100, duration, 3000) { duration = it }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("300ms", "500ms", "800ms", "1000ms", "1500ms").forEach { preset ->
                val ms = preset.replace("ms", "").toIntOrNull() ?: 500
                OptionChip(preset, duration == ms) { duration = ms }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("缓动曲线", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("线性", "缓入", "缓出", "缓入缓出", "弹性", "弹跳").forEach { e ->
                OptionChip(e, easing == e) { easing = e }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("预览", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp))
            .background(Color.Black), contentAlignment = Alignment.Center) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.5f).fillMaxHeight().background(Color(0xFF4A90D9).copy(alpha = 0.3f)))
                Box(modifier = Modifier.weight(0.5f).fillMaxHeight().background(Color(0xFFE85050).copy(alpha = 0.3f)))
            }
            Box(modifier = Modifier.align(Alignment.Center).width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
            Text(selected, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        ApplyButton("应用转场") {
            if (vm.clips.size < 2) {
                vm.showToast("需要至少两个片段才能添加转场")
            } else {
                bridge.applyTransition(vm, selected, duration.toLong(),
                    onComplete = { vm.showToast("转场已应用: $selected ($duration ms)") },
                    onError = { vm.showToast("转场失败: $it") })
            }
            onClose()
        }
    }
}
