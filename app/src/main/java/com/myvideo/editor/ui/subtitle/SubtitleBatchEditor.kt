package com.myvideo.editor.ui.subtitle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubtitleBatchEditor(subtitles: List<String> = emptyList()) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("批量编辑", fontSize = 11.sp, color = Color(0xFF999999), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("全选", "删除选中", "统一字体", "统一颜色", "导出SRT").forEach { action ->
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1A1A1A)).clickable {}
                    .padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(action, fontSize = 10.sp, color = Color(0xFFCCCCCC))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("共 ${subtitles.size} 条字幕", fontSize = 10.sp, color = Color(0xFF666666))
    }
}
