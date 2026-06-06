package com.videoeditor.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videoeditor.data.model.ExportConfig
import com.videoeditor.data.model.ExportFormat
import com.videoeditor.data.model.VideoCodec

@Composable
fun ExportDialog(
    isExporting: Boolean,
    progress: Float,
    onDismiss: () -> Unit,
    onExport: (ExportConfig) -> Unit
) {
    var resolution by remember { mutableIntStateOf(0) } // 0=1080p, 1=720p, 2=480p
    var frameRate by remember { mutableIntStateOf(30) }
    var codec by remember { mutableIntStateOf(0) } // 0=H264, 1=H265
    var format by remember { mutableIntStateOf(0) } // 0=MP4, 1=MOV

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = { Text("导出视频") },
        text = {
            if (isExporting) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在导出... ${(progress * 100).toInt()}%")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Resolution
                    Text("分辨率", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1080P", "720P", "480P").forEachIndexed { index, label ->
                            FilterChip(
                                selected = resolution == index,
                                onClick = { resolution = index },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Frame rate
                    Text("帧率", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(24, 30, 60).forEach { fps ->
                            FilterChip(
                                selected = frameRate == fps,
                                onClick = { frameRate = fps },
                                label = { Text("${fps}fps") }
                            )
                        }
                    }

                    // Codec
                    Text("编码", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = codec == 0,
                            onClick = { codec = 0 },
                            label = { Text("H.264") }
                        )
                        FilterChip(
                            selected = codec == 1,
                            onClick = { codec = 1 },
                            label = { Text("H.265") }
                        )
                    }

                    // Format
                    Text("格式", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = format == 0,
                            onClick = { format = 0 },
                            label = { Text("MP4") }
                        )
                        FilterChip(
                            selected = format == 1,
                            onClick = { format = 1 },
                            label = { Text("MOV") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isExporting) {
                Button(
                    onClick = {
                        val (w, h) = when (resolution) {
                            0 -> 1080 to 1920
                            1 -> 720 to 1280
                            else -> 480 to 854
                        }
                        onExport(
                            ExportConfig(
                                width = w,
                                height = h,
                                frameRate = frameRate,
                                codec = if (codec == 0) VideoCodec.H264 else VideoCodec.H265,
                                format = if (format == 0) ExportFormat.MP4 else ExportFormat.MOV
                            )
                        )
                    }
                ) {
                    Text("开始导出")
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}
