package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class HistoryEntry(
    val id: Int,
    val icon: String,
    val description: String,
    val timestamp: String
)

@Composable
fun HistoryPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }

    // Demo history entries
    var historyEntries by remember {
        mutableStateOf(
            mutableListOf(
                HistoryEntry(0, "📄", "新建项目", "14:30:01"),
                HistoryEntry(1, "📹", "导入视频 Scene_01.mp4", "14:30:15"),
                HistoryEntry(2, "✂️", "分割片段", "14:31:02"),
                HistoryEntry(3, "🎨", "调整亮度 +15", "14:32:10"),
                HistoryEntry(4, "🎨", "调整对比度 +8", "14:32:18"),
                HistoryEntry(5, "🎞", "添加转场 淡入淡出", "14:33:05"),
                HistoryEntry(6, "✏️", "添加文字标题", "14:34:22"),
                HistoryEntry(7, "🔊", "调整音量 80%", "14:35:01"),
                HistoryEntry(8, "⏩", "变速 1.5x", "14:36:15"),
                HistoryEntry(9, "🎨", "应用滤镜 暖色调", "14:37:00"),
                HistoryEntry(10, "✂️", "删除片段", "14:37:45"),
                HistoryEntry(11, "🔄", "倒放片段", "14:38:20"),
                HistoryEntry(12, "📐", "裁剪画面 16:9", "14:39:10"),
                HistoryEntry(13, "🔑", "添加关键帧", "14:40:00"),
                HistoryEntry(14, "🎨", "调整饱和度 +12", "14:40:30")
            )
        )
    }
    var currentPosition by remember { mutableStateOf(historyEntries.lastIndex) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Undo/Redo buttons at top
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (currentPosition > 0) CG.Card else CG.Card.copy(alpha = 0.5f))
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable(enabled = currentPosition > 0) {
                    if (currentPosition > 0) {
                        currentPosition--
                        bridge.undo()
                        vm.showToast("已撤销: ${historyEntries[currentPosition + 1].description}")
                    }
                },
                contentAlignment = Alignment.Center) {
                Text("↩ 撤销", fontSize = 12.sp, color = if (currentPosition > 0) CG.T1 else CG.T4,
                    fontWeight = FontWeight.Medium)
            }
            Box(modifier = Modifier.weight(1f).height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (currentPosition < historyEntries.lastIndex) CG.Card else CG.Card.copy(alpha = 0.5f))
                .border(1.dp, CG.Line2, RoundedCornerShape(8.dp))
                .clickable(enabled = currentPosition < historyEntries.lastIndex) {
                    if (currentPosition < historyEntries.lastIndex) {
                        currentPosition++
                        bridge.redo()
                        vm.showToast("已重做: ${historyEntries[currentPosition].description}")
                    }
                },
                contentAlignment = Alignment.Center) {
                Text("↪ 重做", fontSize = 12.sp, color = if (currentPosition < historyEntries.lastIndex) CG.T1 else CG.T4,
                    fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Position indicator
        Text("当前位置: ${currentPosition + 1} / ${historyEntries.size}",
            fontSize = 9.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(8.dp))

        // History list
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(historyEntries) { index, entry ->
                val isCurrent = index == currentPosition
                val isFuture = index > currentPosition
                Box(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCurrent) CG.AccS else if (isFuture) CG.Card.copy(alpha = 0.5f) else CG.Card)
                    .then(if (isCurrent) Modifier.border(1.dp, CG.Acc, RoundedCornerShape(6.dp)) else Modifier)
                    .clickable {
                        currentPosition = index
                        vm.showToast("已跳转到: ${entry.description}")
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.icon, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.description,
                                fontSize = 11.sp,
                                color = if (isFuture) CG.T4 else if (isCurrent) CG.AccL else CG.T1,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(entry.timestamp, fontSize = 9.sp, color = CG.T4)
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("◀", fontSize = 10.sp, color = CG.AccL)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Clear history button
        Box(modifier = Modifier.fillMaxWidth().height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CG.Red.copy(alpha = 0.15f))
            .border(1.dp, CG.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable {
                historyEntries = mutableListOf(
                    HistoryEntry(0, "📄", "新建项目", "14:30:01")
                )
                currentPosition = 0
                vm.showToast("历史记录已清除")
            },
            contentAlignment = Alignment.Center) {
            Text("清除历史记录", fontSize = 12.sp, color = CG.Red, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
