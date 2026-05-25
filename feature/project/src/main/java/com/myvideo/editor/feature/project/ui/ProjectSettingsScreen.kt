package com.myvideo.editor.feature.project.ui

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
fun ProjectSettingsScreen(projectName: String = "") {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(16.dp)) {
        Text("项目设置", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("名称: $projectName", color = Color.White, fontSize = 14.sp)
    }
}
