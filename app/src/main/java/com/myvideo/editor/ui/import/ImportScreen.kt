package com.myvideo.editor.ui.`import`

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object IC {
    val Bg = Color(0xFF0D0D0D); val Surf = Color(0xFF161616)
    val Card = Color(0xFF1E1E1E); val CardH = Color(0xFF282828)
    val Line = Color(0xFF2A2A2A); val Line2 = Color(0xFF3A3A3A)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4)
    val T3 = Color(0xFF6A6660); val T4 = Color(0xFF4A4A4A)
    val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6)
    val AccS = Color(0x1F4A90D9); val Gold = Color(0xFFE8A820)
    val Green = Color(0xFF7EC850); val Red = Color(0xFFE85050)
}

// ===== 数据模型 =====
data class VideoItem(
    val id: String, val name: String, val duration: String,
    val resolution: String, val thumbnailColor: Color = Color(0xFF2A3A4A)
)

data class ImageItem(
    val id: String, val name: String, val dimensions: String,
    val thumbnailColor: Color = Color(0xFF2A3A2A)
)

data class AudioItem(
    val id: String, val name: String, val duration: String,
    val size: String, val format: String
)

enum class MediaTab(val label: String) { VIDEO("视频"), IMAGE("图片"), AUDIO("音频") }
enum class VideoSort(val label: String) { NEWEST("最新"), OLDEST("最旧"), DURATION("时长") }

// ===== 主界面 =====
@Composable
fun ImportScreen(onBack: () -> Unit = {}, onImport: (List<String>) -> Unit = {}) {
    var currentTab by remember { mutableStateOf(MediaTab.VIDEO) }
    var searchQuery by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var videoSort by remember { mutableStateOf(VideoSort.NEWEST) }
    val selectedVideoIds = remember { mutableStateListOf<String>() }
    val selectedImageIds = remember { mutableStateListOf<String>() }
    var selectedAudioId by remember { mutableStateOf<String?>(null) }

    // 模拟加载
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            kotlinx.coroutines.delay(1200)
            isLoading = false
        }
    }

    // 模拟数据
    val videos = remember {
        listOf(
            VideoItem("v1", "VID_20240315.mp4", "01:23", "1920×1080", Color(0xFF1A2A3A)),
            VideoItem("v2", "VID_20240314.mp4", "00:45", "1280×720", Color(0xFF2A1A3A)),
            VideoItem("v3", "VID_20240312.mp4", "02:10", "1920×1080", Color(0xFF1A3A2A)),
            VideoItem("v4", "VID_20240310.mp4", "00:30", "3840×2160", Color(0xFF3A2A1A)),
            VideoItem("v5", "VID_20240308.mp4", "03:15", "1920×1080", Color(0xFF1A1A3A)),
            VideoItem("v6", "VID_20240305.mp4", "01:00", "1280×720", Color(0xFF2A3A1A)),
            VideoItem("v7", "VID_20240301.mp4", "00:15", "1080×1920", Color(0xFF3A1A2A)),
            VideoItem("v8", "VID_20240228.mp4", "04:22", "1920×1080", Color(0xFF1A2A2A)),
            VideoItem("v9", "VID_20240225.mp4", "00:55", "1280×720", Color(0xFF2A2A1A)),
        )
    }
    val images = remember {
        listOf(
            ImageItem("i1", "IMG_20240315.jpg", "4032×3024", Color(0xFF2A3A2A)),
            ImageItem("i2", "IMG_20240314.png", "1920×1080", Color(0xFF3A2A2A)),
            ImageItem("i3", "IMG_20240312.jpg", "3024×4032", Color(0xFF2A2A3A)),
            ImageItem("i4", "IMG_20240310.jpg", "2560×1440", Color(0xFF3A3A2A)),
            ImageItem("i5", "IMG_20240308.png", "1080×1080", Color(0xFF2A3A3A)),
            ImageItem("i6", "IMG_20240305.jpg", "4032×3024", Color(0xFF3A2A3A)),
            ImageItem("i7", "IMG_20240301.jpg", "1920×1280", Color(0xFF1A2A3A)),
            ImageItem("i8", "IMG_20240228.jpg", "3840×2160", Color(0xFF2A1A3A)),
        )
    }
    val audios = remember {
        listOf(
            AudioItem("a1", "背景音乐_轻快.mp3", "03:24", "4.2MB", "MP3"),
            AudioItem("a2", "旁白录音.wav", "01:15", "12.8MB", "WAV"),
            AudioItem("a3", "音效_鼓点.mp3", "00:03", "0.1MB", "MP3"),
            AudioItem("a4", "访谈录音.m4a", "15:30", "8.5MB", "M4A"),
            AudioItem("a5", "环境音_雨声.flac", "05:00", "28.3MB", "FLAC"),
            AudioItem("a6", "配音_开场.mp3", "00:12", "0.3MB", "MP3"),
        )
    }

    val totalSelected = when (currentTab) {
        MediaTab.VIDEO -> selectedVideoIds.size
        MediaTab.IMAGE -> selectedImageIds.size
        MediaTab.AUDIO -> if (selectedAudioId != null) 1 else 0
    }

    Column(modifier = Modifier.fillMaxSize().background(IC.Bg)) {
        // 顶部栏
        ImportTopBar(onBack = onBack)
        // 搜索栏
        ImportSearchBar(searchQuery) { searchQuery = it }
        // 权限请求
        if (!hasPermission) {
            PermissionSection { hasPermission = true }
        } else if (isLoading) {
            LoadingState()
        } else {
            // 标签栏
            MediaTabRow(currentTab) { currentTab = it }
            // 排序栏（仅视频）
            if (currentTab == MediaTab.VIDEO) {
                VideoSortBar(videoSort) { videoSort = it }
            }
            // 内容区
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentTab) {
                    MediaTab.VIDEO -> VideoGrid(
                        videos = videos, selectedIds = selectedVideoIds,
                        onToggleSelect = { id ->
                            if (selectedVideoIds.contains(id)) selectedVideoIds.remove(id)
                            else selectedVideoIds.add(id)
                        }
                    )
                    MediaTab.IMAGE -> ImageGrid(
                        images = images, selectedIds = selectedImageIds,
                        onToggleSelect = { id ->
                            if (selectedImageIds.contains(id)) selectedImageIds.remove(id)
                            else selectedImageIds.add(id)
                        }
                    )
                    MediaTab.AUDIO -> AudioList(
                        audios = audios, selectedId = selectedAudioId,
                        onSelect = { selectedAudioId = if (selectedAudioId == it) null else it }
                    )
                }
            }
        }
        // 底部栏
        ImportBottomBar(
            selectedCount = totalSelected,
            onImport = {
                val ids = when (currentTab) {
                    MediaTab.VIDEO -> selectedVideoIds.toList()
                    MediaTab.IMAGE -> selectedImageIds.toList()
                    MediaTab.AUDIO -> listOfNotNull(selectedAudioId)
                }
                onImport(ids)
            }
        )
    }
}

// ===== 顶部栏 =====
@Composable
private fun ImportTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp)
            .background(IC.Surf).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
            .background(IC.Card).clickable { onBack() },
            contentAlignment = Alignment.Center) {
            Text("←", fontSize = 18.sp, color = IC.T2)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text("导入素材", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = IC.T1)
    }
}

// ===== 搜索栏 =====
@Composable
private fun ImportSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
        .height(38.dp).clip(RoundedCornerShape(10.dp))
        .background(IC.Card).border(1.dp, IC.Line, RoundedCornerShape(10.dp))
        .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart) {
        if (query.isEmpty()) {
            Text("🔍 搜索媒体文件", fontSize = 12.sp, color = IC.T4)
        }
        BasicTextField(
            value = query, onValueChange = onQueryChange,
            textStyle = TextStyle(
                fontSize = 12.sp, color = IC.T1
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ===== 权限请求 =====
@Composable
private fun PermissionSection(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📂", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("需要访问存储权限", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = IC.T1)
        Spacer(modifier = Modifier.height(8.dp))
        Text("NexClip 需要访问您的媒体文件以导入素材", fontSize = 12.sp, color = IC.T3, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)
            .clip(RoundedCornerShape(10.dp)).background(IC.Acc)
            .clickable { onGrant() },
            contentAlignment = Alignment.Center) {
            Text("授予权限", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("仅用于读取媒体文件，不会上传您的数据", fontSize = 10.sp, color = IC.T4)
    }
}

// ===== 加载状态 =====
@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = IC.Acc, strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("正在扫描媒体库...", fontSize = 12.sp, color = IC.T3)
        }
    }
}

// ===== 标签栏 =====
@Composable
private fun MediaTabRow(selected: MediaTab, onSelect: (MediaTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(42.dp)
        .background(IC.Surf).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        MediaTab.values().forEach { tab ->
            val isSelected = tab == selected
            Box(modifier = Modifier.weight(1f).fillMaxHeight()
                .padding(vertical = 6.dp, horizontal = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) IC.AccS else Color.Transparent)
                .then(if (isSelected) Modifier.border(1.dp, IC.Acc, RoundedCornerShape(8.dp)) else Modifier)
                .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center) {
                Text(tab.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) IC.AccL else IC.T3)
            }
        }
    }
}

// ===== 视频排序栏 =====
@Composable
private fun VideoSortBar(selected: VideoSort, onSelect: (VideoSort) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(32.dp)
        .background(IC.Surf).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("排序:", fontSize = 10.sp, color = IC.T4)
        Spacer(modifier = Modifier.width(8.dp))
        VideoSort.values().forEach { sort ->
            val isSelected = sort == selected
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) IC.AccS else Color.Transparent)
                .clickable { onSelect(sort) }
                .padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(sort.label, fontSize = 10.sp,
                    color = if (isSelected) IC.AccL else IC.T3,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}

// ===== 视频网格 =====
@Composable
private fun VideoGrid(videos: List<VideoItem>, selectedIds: List<String>, onToggleSelect: (String) -> Unit) {
    if (videos.isEmpty()) {
        EmptyState("没有找到视频文件")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(videos, key = { it.id }) { video ->
            val isSelected = selectedIds.contains(video.id)
            Box(modifier = Modifier.aspectRatio(9f / 12f)
                .clip(RoundedCornerShape(6.dp))
                .background(video.thumbnailColor)
                .border(if (isSelected) 2.dp else 0.dp, IC.Acc, RoundedCornerShape(6.dp))
                .clickable { onToggleSelect(video.id) }) {
                // 缩略图占位
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    .background(video.thumbnailColor.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center) {
                    Text("▶", fontSize = 20.sp, color = Color.White.copy(alpha = 0.3f))
                }
                // 时长
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text(video.duration, fontSize = 8.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                }
                // 选中复选框
                Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                    .size(18.dp).clip(CircleShape)
                    .background(if (isSelected) IC.Acc else Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, if (isSelected) IC.AccL else Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    if (isSelected) Text("✓", fontSize = 10.sp, color = Color.White)
                }
                // 底部信息
                Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f)).padding(4.dp)) {
                    Text(video.name, fontSize = 8.sp, color = IC.T2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(video.resolution, fontSize = 7.sp, color = IC.T4, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// ===== 图片网格 =====
@Composable
private fun ImageGrid(images: List<ImageItem>, selectedIds: List<String>, onToggleSelect: (String) -> Unit) {
    if (images.isEmpty()) {
        EmptyState("没有找到图片文件")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(images, key = { it.id }) { image ->
            val isSelected = selectedIds.contains(image.id)
            Box(modifier = Modifier.aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(image.thumbnailColor)
                .border(if (isSelected) 2.dp else 0.dp, IC.Acc, RoundedCornerShape(4.dp))
                .clickable { onToggleSelect(image.id) }) {
                // 选中复选框
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp)
                    .size(16.dp).clip(CircleShape)
                    .background(if (isSelected) IC.Acc else Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, if (isSelected) IC.AccL else Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    if (isSelected) Text("✓", fontSize = 9.sp, color = Color.White)
                }
                // 尺寸信息
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f)).padding(3.dp),
                    contentAlignment = Alignment.Center) {
                    Text(image.dimensions, fontSize = 7.sp, color = IC.T3, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// ===== 音频列表 =====
@Composable
private fun AudioList(audios: List<AudioItem>, selectedId: String?, onSelect: (String) -> Unit) {
    if (audios.isEmpty()) {
        EmptyState("没有找到音频文件")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
        items(audios, key = { it.id }) { audio ->
            val isSelected = selectedId == audio.id
            Row(modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) IC.AccS else IC.Card)
                .then(if (isSelected) Modifier.border(1.dp, IC.Acc, RoundedCornerShape(8.dp)) else Modifier)
                .clickable { onSelect(audio.id) }
                .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                // 格式图标
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(IC.AccS),
                    contentAlignment = Alignment.Center) {
                    Text(audio.format, fontSize = 8.sp, color = IC.AccL, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(audio.name, fontSize = 12.sp, color = IC.T1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Text(audio.duration, fontSize = 10.sp, color = IC.T3, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(audio.size, fontSize = 10.sp, color = IC.T4)
                    }
                }
                if (isSelected) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(IC.Acc),
                        contentAlignment = Alignment.Center) {
                        Text("✓", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ===== 空状态 =====
@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📭", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, fontSize = 13.sp, color = IC.T3)
            Spacer(modifier = Modifier.height(4.dp))
            Text("请确认设备中有对应类型的媒体文件", fontSize = 10.sp, color = IC.T4)
        }
    }
}

// ===== 底部栏 =====
@Composable
private fun ImportBottomBar(selectedCount: Int, onImport: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp)
        .background(IC.Surf).border(1.dp, IC.Line)
        .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // 已选数量
        if (selectedCount > 0) {
            Text("已选 ", fontSize = 12.sp, color = IC.T3)
            Text("$selectedCount", fontSize = 14.sp, color = IC.AccL, fontWeight = FontWeight.Bold)
            Text(" 项", fontSize = 12.sp, color = IC.T3)
        } else {
            Text("未选择素材", fontSize = 12.sp, color = IC.T4)
        }
        Spacer(modifier = Modifier.weight(1f))
        // 存储空间
        Column(horizontalAlignment = Alignment.End) {
            Text("可用空间", fontSize = 8.sp, color = IC.T4)
            Text("23.5 GB", fontSize = 10.sp, color = IC.T3, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.width(12.dp))
        // 导入按钮
        Box(modifier = Modifier.height(38.dp).width(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedCount > 0) IC.Acc else IC.Line2)
            .clickable(enabled = selectedCount > 0) { onImport() },
            contentAlignment = Alignment.Center) {
            Text("导入", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = if (selectedCount > 0) Color.White else IC.T4)
        }
    }
}
