package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
fun PiPPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var pipSize by remember { mutableStateOf(30) }
    var pipX by remember { mutableStateOf(0) }
    var pipY by remember { mutableStateOf(0) }
    var pipShape by remember { mutableStateOf("矩形") }
    var pipBorder by remember { mutableStateOf(0) }
    var pipBorderColor by remember { mutableStateOf("#FFFFFF") }
    var pipShadow by remember { mutableStateOf(false) }
    var pipCornerRadius by remember { mutableStateOf(8) }
    var pipOpacity by remember { mutableStateOf(100) }
    var pipRotation by remember { mutableStateOf(0) }
    var entranceAnim by remember { mutableStateOf("无") }
    var exitAnim by remember { mutableStateOf("无") }
    var position by remember { mutableStateOf(Offset(200f, 150f)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("画中画", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("在主画面上叠加第二个视频画面", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(14.dp))

        Text("位置预览", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E)).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    position = Offset(
                        (position.x + dragAmount.x).coerceIn(0f, size.width.toFloat() - 60f),
                        (position.y + dragAmount.y).coerceIn(0f, size.height.toFloat() - 45f)
                    )
                    pipX = ((position.x / size.width) * 100).toInt()
                    pipY = ((position.y / size.height) * 100).toInt()
                }
            }) {
            Box(modifier = Modifier.offset(position.x.dp, position.y.dp)
                .size((pipSize * 0.6f).dp, (pipSize * 0.45f).dp)
                .clip(RoundedCornerShape(pipCornerRadius.dp))
                .background(Color(0xFF4A90D9).copy(alpha = pipOpacity / 100f))
                .then(if (pipBorder > 0) Modifier.border(pipBorder.dp,
                    Color(android.graphics.Color.parseColor(pipBorderColor)),
                    RoundedCornerShape(pipCornerRadius.dp)) else Modifier),
                contentAlignment = Alignment.Center) {
                Text("PiP", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("尺寸与位置", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        CgSlider("尺寸", 10, pipSize, 60) { pipSize = it }
        CgSlider("X位置", -100, pipX, 100) { pipX = it }
        CgSlider("Y位置", -100, pipY, 100) { pipY = it }
        Spacer(modifier = Modifier.height(4.dp))
        Text("快速定位", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("左上" to (-80 to 80), "右上" to (80 to 80), "左下" to (-80 to -80), "右下" to (80 to -80), "居中" to (0 to 0)).forEach { (name, pos) ->
                OptionChip(name, false) { pipX = pos.first; pipY = pos.second }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("形状", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("矩形", "圆角", "圆形", "椭圆", "菱形").forEach { s ->
                OptionChip(s, pipShape == s) { pipShape = s }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        CgSlider("圆角", 0, pipCornerRadius, 50) { pipCornerRadius = it }
        CgSlider("透明度", 10, pipOpacity, 100) { pipOpacity = it }
        CgSlider("旋转", -180, pipRotation, 180) { pipRotation = it }
        CgSlider("边框", 0, pipBorder, 10) { pipBorder = it }
        Spacer(modifier = Modifier.height(14.dp))

        Text("入场动画", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("无", "缩放入场", "滑入", "淡入", "弹跳").forEach { a ->
                OptionChip(a, entranceAnim == a) { entranceAnim = a }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("退场动画", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("无", "缩放退场", "滑出", "淡出", "弹出").forEach { a ->
                OptionChip(a, exitAnim == a) { exitAnim = a }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用画中画") {
            vm.showToast("画中画已设置: ${pipSize}% 位置($pipX,$pipY)")
            onClose()
        }
    }
}
