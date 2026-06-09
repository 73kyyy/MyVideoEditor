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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun AutoSavePanel(vm: EditorViewModel) {
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var saveInterval by remember { mutableStateOf("1min") }
    var lastSavedTime by remember { mutableStateOf("刚刚") }
    var hasUnsavedChanges by remember { mutableStateOf(true) }
    var storageUsed by remember { mutableStateOf("12.4 MB") }

    val intervals = listOf("30s" to 30_000L, "1min" to 60_000L, "2min" to 120_000L, "5min" to 300_000L)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Auto-save toggle
        ToggleRow("自动保存", autoSaveEnabled) { autoSaveEnabled = it }
        Spacer(modifier = Modifier.height(12.dp))

        // Save interval
        Text("保存间隔", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            intervals.forEach { (label, _) ->
                OptionChip(label, saveInterval == label) {
                    saveInterval = label
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Last saved time
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("上次保存", fontSize = 9.sp, color = CG.T3)
            Text(lastSavedTime, fontSize = 9.sp, color = CG.T2)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Storage usage
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("存储占用", fontSize = 9.sp, color = CG.T3)
            Text(storageUsed, fontSize = 9.sp, color = CG.T2)
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Storage bar
        Box(modifier = Modifier.fillMaxWidth().height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(CG.Line)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.12f)
                .clip(RoundedCornerShape(2.dp))
                .background(CG.Acc))
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Unsaved changes warning
        if (hasUnsavedChanges) {
            Box(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CG.Gold.copy(alpha = 0.1f))
                .border(1.dp, CG.Gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("有未保存的更改", fontSize = 10.sp, color = CG.Gold, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Manual save button
        Box(modifier = Modifier.fillMaxWidth().height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CG.Acc)
            .clickable {
                hasUnsavedChanges = false
                lastSavedTime = "刚刚"
                vm.showToast("项目已保存")
            },
            contentAlignment = Alignment.Center) {
            Text("立即保存", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
