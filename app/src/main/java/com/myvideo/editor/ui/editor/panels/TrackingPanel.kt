package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun TrackingPanel(vm: EditorViewModel, onClose: () -> Unit) {
    var trackPoint by remember { mutableStateOf(Offset(150f, 100f)) }
    var isTracking by remember { mutableStateOf(false) }
    var trackMethod by remember { mutableStateOf("点追踪") }
    var searchRange by remember { mutableStateOf(30) }
    var confidence by remember { mutableStateOf(0) }
    var smoothFactor by remember { mutableStateOf(50) }
    var trackTarget by remember { mutableStateOf("位置") }
    var stabilizeMode by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("运动追踪", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("追踪画面中的运动物体，绑定文字或贴纸", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("追踪画面", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset -> trackPoint = offset }
            }) {
            Box(modifier = Modifier.offset(trackPoint.x.dp, trackPoint.y.dp)
                .size(24.dp).clip(CircleShape)
                .background(Color.Transparent)
                .border(2.dp, if (isTracking) Color(0xFF7EC850) else Color(0xFFE8A820), CircleShape))
            Box(modifier = Modifier.offset((trackPoint.x - 12).dp, trackPoint.y.dp)
                .width(24.dp).height(1.dp).background(if (isTracking) Color(0xFF7EC850) else Color(0xFFE8A820)))
            Box(modifier = Modifier.offset(trackPoint.x.dp, (trackPoint.y - 12).dp)
                .width(1.dp).height(24.dp).background(if (isTracking) Color(0xFF7EC850) else Color(0xFFE8A820)))
            Text(
                if (isTracking) "追踪中: (${trackPoint.x.toInt()}, ${trackPoint.y.toInt()})"
                else "点击选择追踪点: (${trackPoint.x.toInt()}, ${trackPoint.y.toInt()})",
                fontSize = 8.sp, color = CG.T3, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("追踪方法", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("点追踪", "区域追踪", "平面追踪", "特征追踪").forEach { m ->
                OptionChip(m, trackMethod == m) { trackMethod = m }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("追踪目标", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("位置", "位置+缩放", "位置+旋转", "全部变换", "稳定").forEach { t ->
                OptionChip(t, trackTarget == t) { trackTarget = t }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("参数", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("搜索范围", 5, searchRange, 100) { searchRange = it }
        CgSlider("平滑度", 0, smoothFactor, 100) { smoothFactor = it }
        CgSlider("置信度阈值", 0, confidence, 100) { confidence = it }
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("追踪同时稳定画面", fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
                .background(if (stabilizeMode) CG.Acc else CG.Line)
                .clickable { stabilizeMode = !stabilizeMode },
                contentAlignment = if (stabilizeMode) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton(if (isTracking) "停止追踪" else "开始追踪") {
            isTracking = !isTracking
            vm.showToast(if (isTracking) "追踪已启动" else "追踪已停止")
        }
        Spacer(modifier = Modifier.height(8.dp))
        ApplyButton("绑定到选中文字") {
            vm.showToast("已绑定追踪到文字")
            onClose()
        }
    }
}
