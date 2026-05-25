package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WheelsTab() {
    var shadowHue by remember { mutableStateOf(Offset.Zero) }
    var midHue by remember { mutableStateOf(Offset.Zero) }
    var highHue by remember { mutableStateOf(Offset.Zero) }
    var brightness by remember { mutableStateOf(50) }
    var contrast by remember { mutableStateOf(50) }
    var saturation by remember { mutableStateOf(50) }
    var exposure by remember { mutableStateOf(0) }
    var highlights by remember { mutableStateOf(0) }
    var shadows by remember { mutableStateOf(0) }
    var whites by remember { mutableStateOf(0) }
    var blacks by remember { mutableStateOf(0) }
    var temperature by remember { mutableStateOf(0) }
    var tint by remember { mutableStateOf(0) }
    var vignette by remember { mutableStateOf(0) }
    var vignetteFeather by remember { mutableStateOf(50) }
    var vignetteRoundness by remember { mutableStateOf(50) }
    var clarity by remember { mutableStateOf(0) }
    var dehaze by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("色轮", fontSize = 9.sp, color = CG.T4, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ColorWheel("阴影", shadowHue) { shadowHue = it }
            ColorWheel("中间调", midHue) { midHue = it }
            ColorWheel("高光", highHue) { highHue = it }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("基础调整", fontSize = 9.sp, color = CG.T4, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        ParamSlider("亮度", brightness, 0, 100) { brightness = it }
        ParamSlider("对比度", contrast, 0, 100) { contrast = it }
        ParamSlider("饱和度", saturation, 0, 100) { saturation = it }
        ParamSlider("曝光", exposure, -100, 100) { exposure = it }
        ParamSlider("高光", highlights, -100, 100) { highlights = it }
        ParamSlider("阴影", shadows, -100, 100) { shadows = it }
        ParamSlider("白色", whites, -100, 100) { whites = it }
        ParamSlider("黑色", blacks, -100, 100) { blacks = it }
        Spacer(modifier = Modifier.height(14.dp))
        Text("白平衡", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        ParamSlider("色温", temperature, -100, 100) { temperature = it }
        ParamSlider("色调", tint, -100, 100) { tint = it }
        Spacer(modifier = Modifier.height(14.dp))
        Text("效果", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        ParamSlider("清晰度", clarity, -100, 100) { clarity = it }
        ParamSlider("去雾", dehaze, -100, 100) { dehaze = it }
        Spacer(modifier = Modifier.height(14.dp))
        Text("暗角", fontSize = 9.sp, color = CG.T4)
        Spacer(modifier = Modifier.height(6.dp))
        ParamSlider("强度", vignette, -100, 100) { vignette = it }
        ParamSlider("羽化", vignetteFeather, 0, 100) { vignetteFeather = it }
        ParamSlider("圆度", vignetteRoundness, -100, 100) { vignetteRoundness = it }
    }
}

@Composable
private fun ColorWheel(label: String, offset: Offset, onMove: (Offset) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape)
            .background(CG.Card).border(2.dp, CG.Line, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val newOffset = offset + dragAmount
                    val radius = size.width / 2f - 10f
                    val dist = (newOffset - center).getDistance()
                    val clamped = if (dist > radius) center + (newOffset - center) * (radius / dist) else newOffset
                    onMove(clamped - center)
                }
            },
            contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.offset(offset.x.dp, offset.y.dp).size(10.dp)
                .clip(CircleShape).background(Color.White))
        }
        Text(label, fontSize = 8.sp, color = CG.T3, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ParamSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    CgSlider(label, min, value, max)
}
