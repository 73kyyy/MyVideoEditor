package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CG {
    val Bg = Color(0xFF1E1E1E); val Surf = Color(0xFF282828)
    val Card = Color(0xFF2C2C2C); val CardH = Color(0xFF323232)
    val Line = Color(0xFF3A3A3A); val Line2 = Color(0xFF444444)
    val T1 = Color(0xFFCCCCCC); val T2 = Color(0xFF999999)
    val T3 = Color(0xFF666666); val T4 = Color(0xFF4A4A4A)
    val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val AccS = Color(0x1F4A90D9); val Gold = Color(0xFFE8A820)
    val Green = Color(0xFF7EC850); val Red = Color(0xFFE85050)
}

@Composable
fun CgSlider(label: String, min: Int, value: Int, max: Int, onValueChange: (Int) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 9.sp, color = CG.T3)
            Text("$value", fontSize = 9.sp, color = CG.T2)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = CG.Acc,
                activeTrackColor = CG.Acc,
                inactiveTrackColor = CG.Line
            ),
            modifier = Modifier.fillMaxWidth().height(20.dp)
        )
    }
}

@Composable
fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
        .background(if (selected) CG.AccS else CG.Card)
        .then(if (selected) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
        .clickable { onClick() }
        .padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium,
            color = if (selected) CG.AccL else CG.T2)
    }
}

@Composable
fun ApplyButton(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(CG.Acc)
        .clickable { onClick() },
        contentAlignment = Alignment.Center) {
        Text(text, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 9.sp, color = CG.T3, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(36.dp, 20.dp).clip(RoundedCornerShape(10.dp))
            .background(if (checked) CG.Acc else CG.Line)
            .clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(modifier = Modifier.padding(2.dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
        }
    }
}
