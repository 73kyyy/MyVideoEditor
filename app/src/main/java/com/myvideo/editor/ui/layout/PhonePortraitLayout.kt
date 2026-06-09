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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.ui.editor.EditorViewModel
import com.myvideo.editor.ui.editor.PreviewCanvas
import com.myvideo.editor.ui.editor.EditorToolbar
import com.myvideo.editor.ui.editor.TimelineView
import com.myvideo.editor.ui.editor.PlaybackBar
import com.myvideo.editor.ui.editor.PanelOverlay

private object LC {
    val Bg = Color(0xFF1E1E1E); val Surf = Color(0xFF282828)
    val Card = Color(0xFF2C2C2C); val Line = Color(0xFF3A3A3A); val Line2 = Color(0xFF444444)
    val Acc = Color(0xFF4A90D9); val AccL = Color(0xFF6AAFE6); val AccS = Color(0x1F4A90D9)
    val T1 = Color(0xFFCCCCCC); val T2 = Color(0xFF999999); val T3 = Color(0xFF666666)
}

@Composable
fun PhonePortraitLayout(vm: EditorViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Preview area
        Box(modifier = Modifier.fillMaxWidth().height(vm.previewHeightPx.dp).background(Color.Black)) {
            PreviewCanvas(vm)
        }
        // Drag handle
        Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(LC.Surf)
            .border(1.dp, LC.Line), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(36.dp).height(3.dp)
                .clip(RoundedCornerShape(2.dp)).background(LC.Line2))
        }
        // Toolbar
        EditorToolbar(vm)
        // Timeline
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(LC.Card)) {
            TimelineView(vm)
        }
        // Playback bar
        PlaybackBar(vm)
    }
    // Panel overlay
    PanelOverlay(vm)
    // FAB for adding effects when clip is selected
    if (vm.selectedClipId != null) {
        Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            .padding(bottom = 60.dp)
            .size(36.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(LC.Acc, LC.AccL)))
            .clickable { vm.showFxPopup = !vm.showFxPopup }, contentAlignment = Alignment.Center) {
            Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
