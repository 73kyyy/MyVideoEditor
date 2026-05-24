package com.myvideo.editor.ui.subtitle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubtitleEditorScreen(onBack: () -> Unit = {}) {
    var subtitles by remember { mutableStateOf(listOf(
        Triple("00:00:01", "00:00:05", "字幕内容1"),
        Triple("00:00:06", "00:00:10", "字幕内容2")
    )) }
    var selectedIdx by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A1A1A)).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("←", fontSize = 16.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("字幕编辑", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        subtitles.forEachIndexed { idx, (start, end, text) ->
            val sel = idx == selectedIdx
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(if (sel) Color(0xFF1A2A3A) else Color(0xFF1A1A1A))
                .clickable { selectedIdx = idx }.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(start, fontSize = 10.sp, color = Color(0xFF4A90D9), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("→", fontSize = 10.sp, color = Color(0xFF666666))
                    Text(end, fontSize = 10.sp, color = Color(0xFF4A90D9), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text, fontSize = 13.sp, color = Color(0xFFCCCCCC))
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
