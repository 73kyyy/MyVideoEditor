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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object CG {
    val Card = Color(0xFF2C2C2C); val Line = Color(0xFF3A3A3A); val Line2 = Color(0xFF444444)
    val T1 = Color(0xFFCCCCCC); val T2 = Color(0xFF999999); val T3 = Color(0xFF666666)
    val T4 = Color(0xFF4A4A4A); val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val AccS = Color(0x1F4A90D9); val Red = Color(0xFFE85050); val Green = Color(0xFF7EC850)
}

@Composable
internal fun CgSlider(label: String, min: Int, value: Int, max: Int) {
    var v by remember { mutableStateOf(value) }
    val pct = ((v - min).toFloat() / (max - min) * 100).coerceIn(0f, 100f)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 9.sp, color = CG.T3, modifier = Modifier.width(40.dp))
        Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(CG.Card)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(pct / 100f).clip(RoundedCornerShape(3.dp)).background(CG.Acc))
        }
        Text("$v", fontSize = 8.sp, color = CG.T2, fontFamily = FontFamily.Monospace, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
    }
}

@Composable
internal fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
        .background(if (selected) CG.AccS else CG.Card)
        .then(if (selected) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp)) else Modifier)
        .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 11.sp, color = if (selected) CG.AccL else CG.T2)
    }
}

@Composable
internal fun ApplyButton(label: String, onClick: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp))
        .background(Brush.linearGradient(listOf(CG.Acc, CG.AccL))).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
