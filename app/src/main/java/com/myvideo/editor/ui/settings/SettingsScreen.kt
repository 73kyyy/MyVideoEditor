package com.myvideo.editor.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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

private object SC {
    val Bg = Color(0xFF080808); val Surf = Color(0xFF111111); val Card = Color(0xFF161616)
    val CardAlt = Color(0xFF1E1E1E); val Acc = Color(0xFF4A90D9); val Acc2 = Color(0xFF6EC850)
    val Gold = Color(0xFFE8A820); val Green = Color(0xFF6EC850); val Red = Color(0xFFE84848)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4); val T3 = Color(0xFF6A6660)
    val T4 = Color(0xFF3A3A3A); val Line = Color(0xFF1A1A1A); val Line2 = Color(0xFF242424)
}

@Composable
fun SettingsScreen(
    onOpenExportSettings: () -> Unit = {},
    onOpenAiModelManager: () -> Unit = {},
    onOpenPerformanceMonitor: () -> Unit = {},
    onOpenMemberCenter: () -> Unit = {},
    onOpenTutorial: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var defaultResolution by remember { mutableStateOf("1080p") }
    var defaultFps by remember { mutableStateOf("30") }
    var autoSaveInterval by remember { mutableStateOf("30秒") }
    var language by remember { mutableStateOf("中文") }
    var cacheSize by remember { mutableStateOf("128 MB") }
    var modelSize by remember { mutableStateOf("2.4 GB") }
    var exportDir by remember { mutableStateOf("/内部存储/NexClip") }
    var hwAccel by remember { mutableStateOf(true) }
    var previewQuality by remember { mutableStateOf("中") }
    var bgRender by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(SC.Bg)) {
        // Top bar with back navigation
        Row(modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(SC.Card)
                .clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("‹", fontSize = 20.sp, color = SC.T2)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("设置", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SC.T1)
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            // ===== 通用 =====
            item { SectionHeader("通用") }
            item {
                SettingsCard {
                    SettingRow("默认分辨率", defaultResolution) { /* dialog */ }
                    SettingDivider()
                    SettingRow("默认帧率", defaultFps) { /* dialog */ }
                    SettingDivider()
                    SettingRow("自动保存间隔", autoSaveInterval) { /* dialog */ }
                    SettingDivider()
                    SettingRow("语言", language) { /* dialog */ }
                }
            }

            // ===== 存储 =====
            item { SectionHeader("存储") }
            item {
                SettingsCard {
                    Row(modifier = Modifier.fillMaxWidth().clickable { onClearCache() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("缓存大小", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1)
                            Text(cacheSize, fontSize = 9.sp, color = SC.T3, modifier = Modifier.padding(top = 2.dp))
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SC.Red.copy(0.12f))
                            .clickable { cacheSize = "0 MB"; onClearCache() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Text("清除缓存", fontSize = 10.sp, color = SC.Red, fontWeight = FontWeight.Medium)
                        }
                    }
                    SettingDivider()
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOpenAiModelManager() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("模型管理", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1)
                            Text("AI 模型总大小: $modelSize", fontSize = 9.sp, color = SC.T3, modifier = Modifier.padding(top = 2.dp))
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SC.Acc.copy(0.12f))
                            .clickable { onOpenAiModelManager() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Text("管理", fontSize = 10.sp, color = SC.Acc, fontWeight = FontWeight.Medium)
                        }
                    }
                    SettingDivider()
                    SettingRow("导出目录", exportDir) { /* picker */ }
                }
            }

            // ===== 性能 =====
            item { SectionHeader("性能") }
            item {
                SettingsCard {
                    ToggleRow("硬件加速", hwAccel) { hwAccel = it }
                    SettingDivider()
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("预览质量", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("低", "中", "高").forEach { q ->
                                val sel = previewQuality == q
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(if (sel) SC.Acc.copy(0.15f) else SC.CardAlt)
                                    .then(if (sel) Modifier.border(1.dp, SC.Acc, RoundedCornerShape(6.dp)) else Modifier)
                                    .clickable { previewQuality = q }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                                    Text(q, fontSize = 10.sp, color = if (sel) SC.Acc else SC.T3, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                    SettingDivider()
                    ToggleRow("后台渲染", bgRender) { bgRender = it }
                }
            }

            // ===== 关于 =====
            item { SectionHeader("关于") }
            item {
                SettingsCard {
                    SettingRow("版本号", "v1.0.0", onClick = onOpenAbout)
                    SettingDivider()
                    SettingRow("检查更新", "", onClick = { /* check */ })
                    SettingDivider()
                    SettingRow("开源许可", "", onClick = onOpenLicenses)
                    SettingDivider()
                    SettingRow("隐私政策", "", onClick = onOpenPrivacy)
                    SettingDivider()
                    SettingRow("用户协议", "", onClick = onOpenTerms)
                }
            }

            item {
                Text("NexClip · AI 智能视频编辑器", fontSize = 9.sp, color = SC.T3,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 11.sp, color = SC.T3, letterSpacing = 1.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 6.dp, top = 16.dp, bottom = 6.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(SC.Card).padding(2.dp), content = content)
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }
        .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 10.sp, color = SC.T2, fontFamily = FontFamily.Monospace,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SC.CardAlt)
                    .padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text("›", fontSize = 16.sp, color = SC.T3)
    }
}

@Composable
private fun SettingDivider() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(0.5.dp).background(SC.Line))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(44.dp).height(24.dp).clip(RoundedCornerShape(12.dp))
            .background(if (checked) SC.Acc else SC.T4).clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(modifier = Modifier.padding(2.dp).size(20.dp).clip(RoundedCornerShape(10.dp)).background(Color.White))
        }
    }
}
