package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun WheelsTab(vm: EditorViewModel, bridge: EditorBridge) {
    val trackId = vm.selectedClipId ?: "default"
    var shadowOffset by remember { mutableStateOf(Offset.Zero) }
    var midOffset by remember { mutableStateOf(Offset.Zero) }
    var highOffset by remember { mutableStateOf(Offset.Zero) }
    var shadowLum by remember { mutableStateOf(0) }
    var midLum by remember { mutableStateOf(0) }
    var highLum by remember { mutableStateOf(0) }
    var masterSat by remember { mutableStateOf(0) }
    var temperature by remember { mutableStateOf(0) }
    var tint by remember { mutableStateOf(0) }

    fun pushAll() {
        bridge.setTrackProperty(trackId, "shadows_lift", shadowLum.toFloat())
        bridge.setTrackProperty(trackId, "midtones_gamma", midLum.toFloat())
        bridge.setTrackProperty(trackId, "highlights_gain", highLum.toFloat())
        bridge.setTrackProperty(trackId, "saturation", masterSat.toFloat())
        bridge.setTrackProperty(trackId, "temperature", temperature.toFloat())
        bridge.setTrackProperty(trackId, "tint", tint.toFloat())
        // Color wheel offsets encode hue/saturation direction
        bridge.setTrackProperty(trackId, "shadows_hue", shadowOffset.getDistance())
        bridge.setTrackProperty(trackId, "midtones_hue", midOffset.getDistance())
        bridge.setTrackProperty(trackId, "highlights_hue", highOffset.getDistance())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("色轮", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        // Three color wheels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ColorWheel("阴影 (Lift)", shadowOffset) { shadowOffset = it; pushAll() }
            ColorWheel("中间调 (Gamma)", midOffset) { midOffset = it; pushAll() }
            ColorWheel("高光 (Gain)", highOffset) { highOffset = it; pushAll() }
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Luminance sliders below each wheel
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                CgSlider("亮度", -100, shadowLum, 100) { shadowLum = it; pushAll() }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                CgSlider("亮度", -100, midLum, 100) { midLum = it; pushAll() }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                CgSlider("亮度", -100, highLum, 100) { highLum = it; pushAll() }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("白平衡", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("色温", -100, temperature, 100) { temperature = it; pushAll() }
        CgSlider("色调", -100, tint, 100) { tint = it; pushAll() }
        Spacer(modifier = Modifier.height(14.dp))

        Text("主控", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CgSlider("主饱和度", -100, masterSat, 100) { masterSat = it; pushAll() }
        Spacer(modifier = Modifier.height(10.dp))

        Text("重置", fontSize = 9.sp, color = CG.Acc, fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable {
                shadowOffset = Offset.Zero; midOffset = Offset.Zero; highOffset = Offset.Zero
                shadowLum = 0; midLum = 0; highLum = 0
                masterSat = 0; temperature = 0; tint = 0
                pushAll()
            })
    }
}

@Composable
private fun ColorWheel(label: String, offset: Offset, onMove: (Offset) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(90.dp).clip(CircleShape)
            .background(CG.Card).border(2.dp, CG.Line, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val newOffset = offset + Offset(dragAmount.x, dragAmount.y)
                    val radius = size.width / 2f - 12f
                    val dist = (newOffset - center).getDistance()
                    val clamped = if (dist > radius) center + (newOffset - center) * (radius / dist) else newOffset
                    onMove(clamped - center)
                }
            },
            contentAlignment = Alignment.Center) {
            // Radial color gradient wheel
            val wheelSize = 90.dp
            Canvas(modifier = Modifier.size(wheelSize)) {
                val r = size.minDimension / 2f
                for (angle in 0 until 360 step 2) {
                    val rad = Math.toRadians(angle.toDouble())
                    val x = center.x + r * kotlin.math.cos(rad).toFloat() * 0.85f
                    val y = center.y + r * kotlin.math.sin(rad).toFloat() * 0.85f
                    val hue = angle.toFloat()
                    drawCircle(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.8f, 0.9f))), 3f, Offset(x, y))
                }
            }
            // Drag indicator
            Box(modifier = Modifier.offset(offset.x.dp, offset.y.dp).size(12.dp)
                .clip(CircleShape).background(Color.White)
                .border(2.dp, Color.Black, CircleShape))
        }
        Text(label, fontSize = 7.sp, color = CG.T3, modifier = Modifier.padding(top = 4.dp))
    }
}
