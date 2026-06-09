package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun AISegmentPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var segMode by remember { mutableStateOf("点选") }
    var trackObject by remember { mutableStateOf(false) }
    var feather by remember { mutableStateOf(5) }
    var expand by remember { mutableStateOf(0) }
    var contract by remember { mutableStateOf(0) }
    var bgReplacement by remember { mutableStateOf("透明") }
    var selectedPoint by remember { mutableStateOf<Offset?>(null) }
    var boxStart by remember { mutableStateOf<Offset?>(null) }
    var boxEnd by remember { mutableStateOf<Offset?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI智能抠图", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("MobileSAM一键抠图，支持镜头追踪", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("选择模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("点选", "框选", "自动检测").forEach { m ->
                OptionChip(
                    when (m) { "点选" -> "点选目标"; "框选" -> "框选区域"; else -> "自动检测" },
                    segMode == m
                ) { segMode = m }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("选取画面", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .then(
                if (segMode == "点选") {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset -> selectedPoint = offset }
                    }
                } else if (segMode == "框选") {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> boxStart = offset; boxEnd = offset },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                boxEnd = boxEnd?.plus(dragAmount)
                            }
                        )
                    }
                } else Modifier
            )
            .drawBehind {
                selectedPoint?.let { pt ->
                    drawCircle(Color(0xFF4A90D9), radius = 12f, center = pt, style = Stroke(2f))
                    drawCircle(Color(0xFF4A90D9), radius = 3f, center = pt)
                }
                if (boxStart != null && boxEnd != null) {
                    val rect = Rect(boxStart!!, boxEnd!!)
                    drawRect(Color(0xFF4A90D9), topLeft = rect.topLeft,
                        size = rect.size, style = Stroke(2f))
                }
            },
            contentAlignment = Alignment.Center
        ) {
            when (segMode) {
                "点选" -> {
                    if (selectedPoint == null) {
                        Text("点击画面选择目标物体", fontSize = 9.sp, color = CG.T3)
                    } else {
                        Text("已选择 (${selectedPoint!!.x.toInt()}, ${selectedPoint!!.y.toInt()})",
                            fontSize = 8.sp, color = CG.AccL,
                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                    }
                }
                "框选" -> {
                    if (boxStart == null) {
                        Text("拖动画面框选目标区域", fontSize = 9.sp, color = CG.T3)
                    } else {
                        Text("已框选区域",
                            fontSize = 8.sp, color = CG.AccL,
                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                    }
                }
                "自动检测" -> {
                    Text("AI将自动检测画面主体", fontSize = 9.sp, color = CG.T3)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("镜头追踪（跨帧跟踪）", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (trackObject) CG.Acc else CG.Line)
                .clickable { trackObject = !trackObject },
                contentAlignment = if (trackObject) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("蒙版精修", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("羽化", 0, feather, 30) { feather = it }
        CgSlider("扩展", 0, expand, 50) { expand = it }
        CgSlider("收缩", 0, contract, 50) { contract = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("背景替换", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("透明", "纯色", "模糊", "图片").forEach { bg ->
                OptionChip(bg, bgReplacement == bg) { bgReplacement = bg }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(6.dp))
            .background(
                when (bgReplacement) {
                    "透明" -> Color(0xFF1A1A2E)
                    "纯色" -> Color(0xFF00FF00)
                    "模糊" -> Color(0xFF2A2A3E)
                    "图片" -> Color(0xFF3A2A2A)
                    else -> Color(0xFF1A1A2E)
                }
            ).border(1.dp, CG.Line, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (bgReplacement) {
                    "透明" -> "透明背景（棋盘格）"
                    "纯色" -> "绿色背景"
                    "模糊" -> "高斯模糊背景"
                    "图片" -> "自定义图片背景"
                    else -> ""
                },
                fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton(if (isProcessing) "处理中..." else "开始抠图") {
            if (isProcessing) return@ApplyButton
            isProcessing = true
            bridge.aiSegment(vm, true,
                onComplete = {
                    isProcessing = false
                    vm.showToast("抠图完成")
                },
                onError = {
                    isProcessing = false
                    vm.showToast("抠图失败: $it")
                })
            onClose()
        }
    }
}
