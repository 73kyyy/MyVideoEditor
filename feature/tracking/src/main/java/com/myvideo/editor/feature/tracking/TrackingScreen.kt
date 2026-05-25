package com.myvideo.editor.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrackingScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(16.dp)) {
        Text("运动追踪", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("点击画面选择追踪点", color = Color(0xFF666666), fontSize = 14.sp)
    }
}
