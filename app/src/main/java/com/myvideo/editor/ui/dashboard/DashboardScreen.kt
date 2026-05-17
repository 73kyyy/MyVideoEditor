package com.myvideo.editor.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.theme.AppColors
import com.myvideo.editor.theme.AppTypography

data class ProjectItem(
    val id: String,
    val name: String,
    val duration: String,
    val resolution: String,
    val lastModified: String,
    val thumbnailColors: List<Color>,
    val isDraft: Boolean = false
)

@Composable
fun DashboardScreen(
    recentProjects: List<ProjectItem>,
    allProjects: List<ProjectItem>,
    onCreateProject: () -> Unit = {},
    onOpenProject: (String) -> Unit = {},
    onOpenDraftBox: () -> Unit = {},
    onOpenTemplateCenter: () -> Unit = {},
    onOpenTutorial: () -> Unit = {},
    onSearchProject: (String) -> Unit = {},
    onRenameProject: (String) -> Unit = {},
    onDeleteProject: (String) -> Unit = {},
    onDuplicateProject: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.BgPrimary)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("我的项目", style = AppTypography.HeadingLarge.copy(color = AppColors.TextPrimary))
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.BgCard)
                .clickable { onOpenTutorial() }, contentAlignment = Alignment.Center) {
                Text("?", fontSize = 14.sp, color = AppColors.TextTertiary, fontWeight = FontWeight.Bold)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(38.dp)
            .clip(RoundedCornerShape(10.dp)).background(AppColors.BgCard).padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart) {
            if (searchQuery.isEmpty()) Text("搜索项目...", color = AppColors.TextDisabled, fontSize = 12.sp)
            BasicTextField(value = searchQuery, onValueChange = { searchQuery = it; onSearchProject(it) },
                textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary),
                modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(AppColors.Accent, AppColors.AccentLight)))
            .clickable { onCreateProject() }, contentAlignment = Alignment.Center) {
            Text("+ 新建项目", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickEntryCard("草", "草稿箱", "恢复未完成项目", AppColors.Gold, Modifier.weight(1f), onOpenDraftBox)
            QuickEntryCard("模", "模板中心", "标题/转场模板", AppColors.Accent, Modifier.weight(1f), onOpenTemplateCenter)
            QuickEntryCard("教", "使用教程", "新手引导", AppColors.Green, Modifier.weight(1f), onOpenTutorial)
        }
        if (recentProjects.isNotEmpty()) {
            Text("最近", style = AppTypography.LabelLarge.copy(color = AppColors.TextTertiary),
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 10.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recentProjects) { RecentProjectCard(it) { onOpenProject(it.id) } }
            }
        }
        Text("全部项目", style = AppTypography.LabelLarge.copy(color = AppColors.TextTertiary),
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 10.dp))
        val filtered = if (searchQuery.isEmpty()) allProjects else allProjects.filter { it.name.contains(searchQuery, ignoreCase = true) }
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 40.dp), contentAlignment = Alignment.Center) {
                Text(if (searchQuery.isEmpty()) "还没有项目，点击上方按钮新建" else "没有找到匹配的项目", color = AppColors.TextDisabled, fontSize = 12.sp)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(filtered) { p -> ProjectListItem(p, { onOpenProject(p.id) }, { onRenameProject(p.id) }, { onDeleteProject(p.id) }, { onDuplicateProject(p.id) }) }
            }
        }
    }
}

@Composable
private fun QuickEntryCard(icon: String, label: String, subtitle: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.height(72.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.BgCard).clickable { onClick() }.padding(10.dp),
        verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
        Text(subtitle, fontSize = 8.sp, color = AppColors.TextTertiary)
    }
}

@Composable
private fun RecentProjectCard(project: ProjectItem, onClick: () -> Unit) {
    Box(modifier = Modifier.width(160.dp).height(100.dp).clip(RoundedCornerShape(14.dp)).clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(project.thumbnailColors)))
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)), startY = 60f)))
        if (project.isDraft) Text("草稿", fontSize = 7.sp, color = AppColors.Gold, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clip(RoundedCornerShape(3.dp)).background(Color(0x33E8A820)).padding(horizontal = 4.dp, vertical = 1.dp))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp, 6.dp)) {
            Text(project.name, style = AppTypography.LabelSmall.copy(fontSize = 11.sp, color = Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${project.duration}  ${project.resolution}", style = AppTypography.MonoSmall.copy(fontSize = 9.sp, color = AppColors.TextSecondary), modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun ProjectListItem(project: ProjectItem, onClick: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit, onDuplicate: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.BgCard)
        .combinedClickable(onClick = onClick, onLongClick = { showMenu = true }).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(project.thumbnailColors)))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(project.name, style = AppTypography.HeadingSmall.copy(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (project.isDraft) Text("草稿", fontSize = 7.sp, color = AppColors.Gold,
                    modifier = Modifier.padding(start = 4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0x33E8A820)).padding(horizontal = 3.dp, vertical = 1.dp))
            }
            Row(modifier = Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(project.duration, style = AppTypography.MonoSmall.copy(color = AppColors.TextTertiary))
                Text(project.resolution, style = AppTypography.MonoSmall.copy(color = AppColors.TextTertiary))
            }
        }
        Text(project.lastModified, style = AppTypography.LabelSmall.copy(fontSize = 9.sp, color = AppColors.AccentLight),
            modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(AppColors.AccentSubtle).padding(horizontal = 6.dp, vertical = 2.dp))
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(AppColors.BgCardAlt)) {
            DropdownMenuItem(text = { Text("重命名", color = AppColors.TextPrimary, fontSize = 12.sp) }, onClick = { showMenu = false; onRename() })
            DropdownMenuItem(text = { Text("复制", color = AppColors.TextPrimary, fontSize = 12.sp) }, onClick = { showMenu = false; onDuplicate() })
            DropdownMenuItem(text = { Text("删除", color = AppColors.Red, fontSize = 12.sp) }, onClick = { showMenu = false; onDelete() })
        }
    }
}
