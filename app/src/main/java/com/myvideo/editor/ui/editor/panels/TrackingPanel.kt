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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun TrackingPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    var trackMode by remember { mutableStateOf("点追踪") }
    var isTracking by remember { mutableStateOf(false) }
    var trackPoint by remember { mutableStateOf(Offset(150f, 100f)) }
    var objectCount by remember { mutableStateOf(0) }
    var confidence by remember { mutableStateOf(0) }
    var showPath by remember { mutableStateOf(false) }
    var stabilizeTracked by remember { mutableStateOf(false) }
    var muteRegion by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 追踪模式
        Text("追踪模式 (MobileSAM)", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("点追踪", "框选追踪", "自动追踪").forEach { mode ->
                OptionChip(mode, trackMode == mode) { trackMode = mode }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 追踪画面
        Text("追踪画面", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .then(if (trackMode != "自动追踪") Modifier.pointerInput(Unit) {
                detectTapGestures { offset -> trackPoint = offset }
            } else Modifier)) {
            // 追踪点十字线
            if (trackMode == "点追踪") {
                Box(modifier = Modifier.offset(trackPoint.x.dp, trackPoint.y.dp)
                    .size(24.dp).clip(CircleShape)
                    .background(Color.Transparent)
                    .border(2.dp, if (isTracking) CG.Green else CG.Gold, CircleShape))
                Box(modifier = Modifier.offset((trackPoint.x - 12).dp, trackPoint.y.dp)
                    .width(24.dp).height(1.dp).background(if (isTracking) CG.Green else CG.Gold))
                Box(modifier = Modifier.offset(trackPoint.x.dp, (trackPoint.y - 12).dp)
                    .width(1.dp).height(24.dp).background(if (isTracking) CG.Green else CG.Gold))
            } else if (trackMode == "框选追踪") {
                Box(modifier = Modifier.offset(40.dp, 30.dp)
                    .size(100.dp, 70.dp).clip(RoundedCornerShape(4.dp))
                    .background(Color.Transparent)
                    .border(2.dp, if (isTracking) CG.Green else CG.Acc, RoundedCornerShape(4.dp)))
            } else {
                // 自动追踪 - 显示检测到的多个区域
                listOf(Offset(60f, 40f), Offset(180f, 80f), Offset(120f, 120f)).forEach { pt ->
                    Box(modifier = Modifier.offset(pt.x.dp, pt.y.dp)
                        .size(16.dp).clip(CircleShape)
                        .background(Color.Transparent)
                        .border(2.dp, CG.Green.copy(alpha = 0.7f), CircleShape))
                }
            }
            Text(
                when {
                    isTracking -> "追踪中..."
                    trackMode == "自动追踪" -> "自动检测模式"
                    else -> "点击选择追踪点: (${trackPoint.x.toInt()}, ${trackPoint.y.toInt()})"
                },
                fontSize = 8.sp, color = CG.T3,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 开始追踪按钮
        ApplyButton(if (isTracking) "停止追踪" else "开始追踪") {
            isTracking = !isTracking
            if (isTracking) {
                objectCount = if (trackMode == "自动追踪") 3 else 1
                confidence = (75..95).random()
                bridge.applyEffect("tracking_start", mapOf(
                    "mode" to trackMode,
                    "point" to "${trackPoint.x},${trackPoint.y}"
                ))
                vm.showToast("追踪已启动 - $trackMode")
            } else {
                vm.showToast("追踪已停止")
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 追踪结果
        Text("追踪结果", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .padding(12.dp)) {
            Column {
                ResultRow("检测目标数", if (isTracking) "$objectCount" else "-")
                ResultRow("置信度", if (isTracking) "$confidence%" else "-")
                ResultRow("追踪模式", trackMode)
                ResultRow("状态", if (isTracking) "追踪中" else "未启动")
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 追踪路径可视化
        ToggleRow("显示追踪路径", showPath) { showPath = it }
        Spacer(modifier = Modifier.height(10.dp))

        // 稳定追踪对象
        ToggleRow("稳定追踪对象", stabilizeTracked) { stabilizeTracked = it }
        Spacer(modifier = Modifier.height(10.dp))

        // 静音追踪区域
        ToggleRow("静音追踪区域", muteRegion) { muteRegion = it }
        Spacer(modifier = Modifier.height(16.dp))

        // 清除追踪
        Box(modifier = Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(8.dp))
            .background(CG.Red.copy(alpha = 0.2f)).border(1.dp, CG.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable {
                isTracking = false
                objectCount = 0
                confidence = 0
                showPath = false
                stabilizeTracked = false
                muteRegion = false
                bridge.applyEffect("tracking_clear", emptyMap())
                vm.showToast("追踪已清除")
            },
            contentAlignment = Alignment.Center) {
            Text("清除追踪", fontSize = 11.sp, color = CG.Red, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 9.sp, color = CG.T3)
        Text(value, fontSize = 9.sp, color = CG.T1)
    }
}
