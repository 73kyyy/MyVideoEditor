package com.myvideo.editor.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.theme.AppColors
import com.myvideo.editor.theme.AppTypography

data class SettingItem(val icon: String, val title: String, val subtitle: String, val value: String = "", val onClick: () -> Unit = {})
data class SettingGroup(val title: String, val items: List<SettingItem>)

@Composable
fun SettingsScreen(
    onOpenExportSettings: () -> Unit = {},
    onOpenAiModelManager: () -> Unit = {},
    onOpenPerformanceMonitor: () -> Unit = {},
    onOpenMemberCenter: () -> Unit = {},
    onOpenTutorial: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    onClearCache: () -> Unit = {}
) {
    val groups = listOf(
        SettingGroup("工程设置", listOf(
            SettingItem("渲", "渲染引擎", "选择渲染后端", "MediaCodec", onOpenExportSettings),
            SettingItem("导", "导出设置", "分辨率/帧率/码率", "", onOpenExportSettings)
        )),
        SettingGroup("AI 模型", listOf(
            SettingItem("模", "模型管理", "查看和下载AI模型", "", onOpenAiModelManager),
            SettingItem("档", "设备性能档位", "根据设备自动优化", "T1 旗舰", onOpenPerformanceMonitor)
        )),
        SettingGroup("性能与缓存", listOf(
            SettingItem("缓", "缓存管理", "清理缓存释放空间", "0 MB", onClearCache),
            SettingItem("性", "性能中心", "内存/帧率/温度监控", "", onOpenPerformanceMonitor)
        )),
        SettingGroup("账号与会员", listOf(
            SettingItem("会", "会员中心", "查看会员权益", "免费版", onOpenMemberCenter)
        )),
        SettingGroup("关于", listOf(
            SettingItem("教", "使用教程", "新手引导和功能介绍", "", onOpenTutorial),
            SettingItem("证", "开源许可", "第三方库许可证", "", onOpenLicenses),
            SettingItem("版", "版本信息", "当前版本 1.0.0", "")
        ))
    )

    Column(modifier = Modifier.fillMaxSize().background(AppColors.BgPrimary)) {
        // 顶部
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("设置", style = AppTypography.HeadingLarge.copy(color = AppColors.TextPrimary))
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            groups.forEach { group ->
                item {
                    Text(group.title, fontSize = 10.sp, color = AppColors.TextDisabled,
                        letterSpacing = 0.5.sp, modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp))
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.BgCard)) {
                        group.items.forEach { item ->
                            SettingRow(item)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingRow(item: SettingItem) {
    Row(modifier = Modifier.fillMaxWidth().clickable { item.onClick() }.padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.BgCardAlt),
            contentAlignment = Alignment.Center) {
            Text(item.icon, fontSize = 14.sp, color = AppColors.TextSecondary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, color = AppColors.TextPrimary)
            Text(item.subtitle, fontSize = 10.sp, color = AppColors.TextTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        if (item.value.isNotEmpty()) {
            Text(item.value, fontSize = 10.sp, color = AppColors.TextDisabled, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
        Text(">", fontSize = 12.sp, color = AppColors.TextDisabled, modifier = Modifier.padding(start = 6.dp))
    }
}
