package com.myvideo.editor.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.ui.editor.EditorViewModel
import com.myvideo.editor.ui.editor.PreviewCanvas
import com.myvideo.editor.ui.editor.TimelineView
import com.myvideo.editor.ui.editor.PlaybackBar
import com.myvideo.editor.ui.editor.PanelContent
import com.myvideo.editor.ui.editor.panelTitle

private object LC {
    val Bg = Color(0xFF1E1E1E); val Surf = Color(0xFF282828)
    val Card = Color(0xFF2C2C2C); val Line = Color(0xFF3A3A3A); val Line2 = Color(0xFF444444)
    val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6); val AccS = Color(0x1F4A90D9)
    val T1 = Color(0xFFCCCCCC); val T2 = Color(0xFF999999); val T3 = Color(0xFF666666)
    val T4 = Color(0xFF4A4A4A)
}

@Composable
fun TabletLayout(vm: EditorViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left column: Toolbar (vertical)
        Column(modifier = Modifier.width(48.dp).fillMaxHeight().background(LC.Surf)
            .border(1.dp, LC.Line), horizontalAlignment = Alignment.CenterHorizontally) {
            TabletToolbarButtons(vm)
        }
        // Middle column: Preview + Timeline + Playback
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Preview
            Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(Color.Black)) {
                PreviewCanvas(vm)
            }
            // Timeline
            Box(modifier = Modifier.fillMaxWidth().weight(0.4f).background(LC.Card)) {
                TimelineView(vm)
            }
            // Playback bar
            PlaybackBar(vm)
        }
        // Right column: Panels (3-column layout)
        Box(modifier = Modifier.width(280.dp).fillMaxHeight().background(LC.Surf)
            .border(1.dp, LC.Line)) {
            TabletPanelsColumn(vm)
        }
    }
    // No PanelOverlay needed — panels are shown inline in the right column
}

@Composable
private fun TabletToolbarButtons(vm: EditorViewModel) {
    Spacer(modifier = Modifier.height(4.dp))
    TabletToolBtn("撤销") { vm.activePanel = "history" }
    TabletToolBtn("重做") { vm.activePanel = "history" }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("分割") { vm.splitSelectedClip(); vm.showToast("已分割") }
    TabletToolBtn("删除") { vm.deleteSelectedClip(); vm.showToast("已删除") }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("钢笔") { vm.penMode = !vm.penMode }
    TabletToolBtn("清除") { vm.maskPoints.clear(); vm.maskClosed = false }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("关键帧") { vm.activePanel = "keyframe" }
    TabletToolBtn("裁剪") { vm.activePanel = "crop" }
    TabletToolBtn("变速") { vm.activePanel = "speed_curve" }
    TabletToolBtn("倒放") { vm.activePanel = "reverse" }
    TabletToolBtn("比例") { vm.activePanel = "aspect_ratio" }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("调色") { vm.activePanel = "color" }
    TabletToolBtn("效果") { vm.activePanel = "fx" }
    TabletToolBtn("文字") { vm.activePanel = "text" }
    TabletToolBtn("音频") { vm.activePanel = "audio" }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("AI抠图") { vm.activePanel = "ai_segment" }
    TabletToolBtn("AI超分") { vm.activePanel = "ai_superres" }
    TabletToolBtn("AI插帧") { vm.activePanel = "ai_interpolation" }
    TabletToolBtn("AI语音") { vm.activePanel = "ai_speech" }
    TabletToolBtn("AI降噪") { vm.activePanel = "ai_denoise" }
    TabletToolBtn("AI分离") { vm.activePanel = "ai_separation" }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("混合") { vm.activePanel = "blend" }
    TabletToolBtn("画中画") { vm.activePanel = "pip" }
    TabletToolBtn("导出") { vm.activePanel = "export" }
    TabletToolBtn("转场") { vm.activePanel = "transitions" }
    Spacer(modifier = Modifier.height(2.dp))
    TabletToolBtn("历史") { vm.activePanel = "history" }
    TabletToolBtn("设置") { vm.activePanel = "settings" }
}

@Composable
private fun TabletToolBtn(label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.width(44.dp).height(32.dp)
        .clip(RoundedCornerShape(6.dp))
        .clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, fontSize = 7.sp, color = LC.T2, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun TabletPanelsColumn(vm: EditorViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Panel header
        Text(panelTitle(vm.activePanel),
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LC.T1)
        Spacer(modifier = Modifier.height(8.dp))
        // Inline panel content (same as PanelOverlay but rendered inline)
        if (vm.activePanel != null) {
            PanelContent(vm)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("选择工具以查看面板", fontSize = 11.sp, color = LC.T3)
            }
        }
    }
}
