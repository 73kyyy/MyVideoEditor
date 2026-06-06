package com.videoeditor.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.videoeditor.data.model.ExportConfig
import com.videoeditor.data.model.VideoCodec
import com.videoeditor.ui.component.ExportDialog
import com.videoeditor.ui.component.FilterPanel
import com.videoeditor.ui.component.SpeedPanel
import com.videoeditor.ui.preview.VideoPreview
import com.videoeditor.ui.timeline.TimelineView
import com.videoeditor.viewmodel.EditorViewModel

enum class EditorTool {
    NONE, FILTER, TEXT, AUDIO, STICKER, ADJUST, SPEED, VOLUME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModel.Factory(LocalContext.current))
) {
    val project by viewModel.project.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionUs by viewModel.currentPositionUs.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()

    var currentTool by remember { mutableStateOf(EditorTool.NONE) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val videoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importVideo(it) }
    }

    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importAudio(it) }
    }

    // Auto-show import when project is empty
    LaunchedEffect(project.videoTracks.isEmpty()) {
        if (project.videoTracks.isEmpty() || project.videoTracks.all { it.clips.isEmpty() }) {
            showImportDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        project.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.canUndo
                    ) {
                        Icon(Icons.Default.Undo, "撤销")
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.canRedo
                    ) {
                        Icon(Icons.Default.Redo, "重做")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.FileDownload, "导出")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Video Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(androidx.compose.ui.graphics.Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPreview(
                    project = project,
                    isPlaying = isPlaying,
                    currentPositionUs = currentPositionUs,
                    onPositionChange = { viewModel.setCurrentPosition(it) },
                    onPlayPauseToggle = { viewModel.setPlaying(!isPlaying) },
                    modifier = Modifier.fillMaxSize()
                )

                // Play/Pause overlay button
                if (!isPlaying) {
                    IconButton(
                        onClick = { viewModel.setPlaying(true) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Tool Panel (expandable area above toolbar)
            AnimatedVisibility(
                visible = currentTool != EditorTool.NONE,
                enter = androidx.compose.animation.slideInVertically(),
                exit = androidx.compose.animation.slideOutVertically()
            ) {
                ToolPanel(
                    tool = currentTool,
                    selectedClipId = selectedClipId,
                    onAddFilter = { filter ->
                        selectedClipId?.let { viewModel.addFilter(it, filter) }
                    },
                    onSetSpeed = { speed ->
                        selectedClipId?.let { viewModel.setClipSpeed(it, speed) }
                    },
                    onSetVolume = { volume ->
                        selectedClipId?.let { viewModel.setClipVolume(it, volume) }
                    },
                    onImportAudio = {
                        audioLauncher.launch("audio/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            // Toolbar
            EditorToolbar(
                currentTool = currentTool,
                onToolSelected = { tool ->
                    currentTool = if (tool == currentTool) EditorTool.NONE else tool
                    if (tool == EditorTool.NONE) currentTool = EditorTool.NONE
                },
                onImportVideo = {
                    videoLauncher.launch("video/*")
                },
                onSplit = {
                    selectedClipId?.let {
                        viewModel.splitClip(it, currentPositionUs)
                    }
                },
                onDelete = {
                    selectedClipId?.let { viewModel.deleteClip(it) }
                },
                hasSelectedClip = selectedClipId != null
            )

            // Timeline
            TimelineView(
                project = project,
                currentPositionUs = currentPositionUs,
                selectedClipId = selectedClipId,
                onPositionChange = { viewModel.setCurrentPosition(it) },
                onClipClick = { viewModel.selectClip(it) },
                onClipMove = { clipId, newStartUs ->
                    viewModel.moveClip(clipId, newStartUs)
                },
                onClipTrim = { clipId, startUs, endUs ->
                    viewModel.trimClip(clipId, startUs, endUs)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            isExporting = isExporting,
            progress = exportProgress,
            onDismiss = { showExportDialog = false },
            onExport = { config ->
                viewModel.export(config, "")
                showExportDialog = false
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入素材") },
            text = { Text("选择要导入的视频或音频文件") },
            confirmButton = {
                TextButton(onClick = {
                    videoLauncher.launch("video/*")
                    showImportDialog = false
                }) {
                    Text("选择视频")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    audioLauncher.launch("audio/*")
                    showImportDialog = false
                }) {
                    Text("选择音频")
                }
            }
        )
    }
}

@Composable
private fun EditorToolbar(
    currentTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    onImportVideo: () -> Unit,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    hasSelectedClip: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolButton(icon = Icons.Default.LibraryAdd, label = "导入", onClick = onImportVideo)
        ToolButton(
            icon = Icons.Default.ContentCut,
            label = "分割",
            onClick = onSplit,
            enabled = hasSelectedClip
        )
        ToolButton(
            icon = Icons.Default.AutoFixHigh,
            label = "滤镜",
            isSelected = currentTool == EditorTool.FILTER,
            onClick = { onToolSelected(EditorTool.FILTER) }
        )
        ToolButton(
            icon = Icons.Default.TextFields,
            label = "文字",
            isSelected = currentTool == EditorTool.TEXT,
            onClick = { onToolSelected(EditorTool.TEXT) }
        )
        ToolButton(
            icon = Icons.Default.Audiotrack,
            label = "音频",
            isSelected = currentTool == EditorTool.AUDIO,
            onClick = { onToolSelected(EditorTool.AUDIO) }
        )
        ToolButton(
            icon = Icons.Default.EmojiEmotions,
            label = "贴纸",
            isSelected = currentTool == EditorTool.STICKER,
            onClick = { onToolSelected(EditorTool.STICKER) }
        )
        ToolButton(
            icon = Icons.Default.Tune,
            label = "调节",
            isSelected = currentTool == EditorTool.ADJUST,
            onClick = { onToolSelected(EditorTool.ADJUST) }
        )
        ToolButton(
            icon = Icons.Default.Speed,
            label = "变速",
            isSelected = currentTool == EditorTool.SPEED,
            onClick = { onToolSelected(EditorTool.SPEED) },
            enabled = hasSelectedClip
        )
        ToolButton(
            icon = Icons.Default.Delete,
            label = "删除",
            onClick = onDelete,
            enabled = hasSelectedClip
        )
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun ToolPanel(
    tool: EditorTool,
    selectedClipId: String?,
    onAddFilter: (com.videoeditor.data.model.Filter) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetVolume: (Float) -> Unit,
    onImportAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (tool) {
            EditorTool.FILTER -> FilterPanel(
                onFilterSelected = onAddFilter,
                modifier = Modifier.fillMaxSize()
            )
            EditorTool.SPEED -> SpeedPanel(
                onSpeedChange = onSetSpeed,
                modifier = Modifier.fillMaxSize()
            )
            EditorTool.VOLUME -> {
                // Volume slider
                var volume by remember { mutableFloatStateOf(1f) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("音量调节", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            onSetVolume(it)
                        },
                        valueRange = 0f..2f
                    )
                    Text("${(volume * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                }
            }
            EditorTool.AUDIO -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("音频", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onImportAudio) {
                        Icon(Icons.Default.Audiotrack, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导入音频")
                    }
                }
            }
            EditorTool.TEXT -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("文字", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("点击添加文字到视频", style = MaterialTheme.typography.bodyMedium)
                }
            }
            EditorTool.STICKER -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("贴纸", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("贴纸功能开发中", style = MaterialTheme.typography.bodyMedium)
                }
            }
            EditorTool.ADJUST -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("调节", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("亮度、对比度、饱和度调节", style = MaterialTheme.typography.bodyMedium)
                }
            }
            EditorTool.NONE -> {}
        }
    }
}
