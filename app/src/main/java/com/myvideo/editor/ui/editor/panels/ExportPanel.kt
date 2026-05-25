package com.myvideo.editor.ui.editor.panels

import android.os.Environment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel
import java.io.File

@Composable
fun ExportPanel(vm: EditorViewModel = EditorViewModel(), onClose: () -> Unit = {}) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var resolution by remember { mutableStateOf("1080P") }
    var format by remember { mutableStateOf("MP4") }

    val (exportW, exportH) = when (resolution) {
        "720P" -> 1280 to 720
        "1080P" -> 1920 to 1080
        "2K" -> 2560 to 1440
        "4K" -> 3840 to 2160
        else -> 1920 to 1080
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("分辨率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("720P", "1080P", "2K", "4K").forEach { r ->
                OptionChip(r, resolution == r) { resolution = r }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text("格式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("MP4", "MOV", "GIF").forEach { f ->
                OptionChip(f, format == f) { format = f }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 进度条
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(CG.Card)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(vm.exportProgress / 100f)
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.linearGradient(listOf(CG.Acc, CG.AccL))))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            when {
                vm.exportDone -> "导出完成！"
                vm.exportError != null -> "错误: ${vm.exportError}"
                vm.isExporting -> "导出中 ${vm.exportProgress.toInt()}%"
                else -> "准备导出"
            },
            fontSize = 10.sp, color = CG.T3, modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 导出按钮（连接EditorBridge）
        ApplyButton(
            if (vm.exportDone) "完成"
            else if (vm.isExporting) "导出中..."
            else "开始导出"
        ) {
            if (!vm.isExporting && !vm.exportDone) {
                bridge.export(vm, exportW, exportH, 30, "16M",
                    onComplete = { path -> vm.showToast("导出完成: $path") },
                    onError = { err -> vm.showToast("导出失败: $err") }
                )
            }
        }
    }
}
