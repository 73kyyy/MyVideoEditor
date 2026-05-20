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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExportPanel(onClose: () -> Unit = {}) {
    var preset by remember { mutableStateOf("自定义") }
    var resolution by remember { mutableStateOf("1080P") }
    var format by remember { mutableStateOf("MP4") }
    var customRatio by remember { mutableStateOf(false) }
    var customW by remember { mutableStateOf("1920") }
    var customH by remember { mutableStateOf("1080") }
    var progress by remember { mutableStateOf(0f) }
    var exporting by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 预设
        Text("预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("自定义", "抖音", "朋友圈", "YouTube", "Instagram").forEach { p ->
                OptionChip(p, preset == p) { preset = p }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 分辨率
        Text("分辨率", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("720P", "1080P", "2K", "4K").forEach { r ->
                OptionChip(r, resolution == r && !customRatio) {
                    resolution = r; customRatio = false
                }
            }
            OptionChip("自定义", customRatio) { customRatio = true }
        }
        // 自定义比例输入框
        if (customRatio) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(CG.Card).border(1.dp, CG.Line2, RoundedCornerShape(8.dp)), contentAlignment = Alignment.CenterStart) {
                    Text("  W: $customW", fontSize = 11.sp, color = CG.T1, fontFamily = FontFamily.Monospace)
                }
                Text("×", fontSize = 12.sp, color = CG.T3)
                Box(modifier = Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(CG.Card).border(1.dp, CG.Line2, RoundedCornerShape(8.dp)), contentAlignment = Alignment.CenterStart) {
                    Text("  H: $customH", fontSize = 11.sp, color = CG.T1, fontFamily = FontFamily.Monospace)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 码率 + 帧率
        CgSlider("码率", 1, 20, 100)
        CgSlider("帧率", 15, 30, 60)
        Spacer(modifier = Modifier.height(14.dp))

        // 格式
        Text("格式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("MP4", "MOV", "GIF").forEach { f -> OptionChip(f, format == f) { format = f } }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // 进度条
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(CG.Card)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress / 100f).clip(RoundedCornerShape(3.dp))
                .background(Brush.linearGradient(listOf(CG.Acc, CG.AccL))))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            when {
                done -> "导出完成！"
                exporting -> "导出中 ${progress.toInt()}%"
                else -> "准备导出"
            },
            fontSize = 10.sp, color = CG.T3, modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 导出按钮
        ApplyButton(if (done) "完成" else if (exporting) "导出中..." else "开始导出") {
            if (!exporting && !done) {
                exporting = true
                // 模拟导出进度
                Thread {
                    var p = 0f
                    while (p < 100) {
                        p += (5..15).random()
                        if (p > 100) p = 100f
                        progress = p
                        Thread.sleep(300)
                    }
                    done = true
                    exporting = false
                }.start()
            }
        }
    }
}
