package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class BlendMode(
    val name: String,
    val id: String,
    val previewBase: Color,
    val previewBlend: Color
)

@Composable
fun BlendPanel(vm: EditorViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val bridge = remember { EditorBridge(context) }
    var selectedMode by remember { mutableStateOf("normal") }
    var opacity by remember { mutableStateOf(100) }

    val blendModes = remember {
        listOf(
            BlendMode("正常",     "normal",      Color(0xFF666666), Color(0xFF666666)),
            BlendMode("正片叠底", "multiply",    Color(0xFF2A2A2A), Color(0xFF442244)),
            BlendMode("滤色",     "screen",      Color(0xFFCCCCCC), Color(0xFFDDEEFF)),
            BlendMode("叠加",     "overlay",     Color(0xFF886644), Color(0xFFAA7744)),
            BlendMode("柔光",     "softlight",   Color(0xFF997755), Color(0xFFBB9977)),
            BlendMode("强光",     "hardlight",   Color(0xFFAA6633), Color(0xFFDD8844)),
            BlendMode("颜色减淡", "colordodge",  Color(0xFFE0E0E0), Color(0xFFFFEECC)),
            BlendMode("颜色加深", "colorburn",   Color(0xFF1A1A1A), Color(0xFF331111)),
            BlendMode("变暗",     "darken",      Color(0xFF3A3A3A), Color(0xFF222233)),
            BlendMode("变亮",     "lighten",     Color(0xFFAAAAAA), Color(0xFFDDBB99)),
            BlendMode("差值",     "difference",  Color(0xFF5555AA), Color(0xFF8855AA)),
            BlendMode("排除",     "exclusion",   Color(0xFF777788), Color(0xFF998877)),
            BlendMode("色相",     "hue",         Color(0xFF44AA66), Color(0xFF66BB88)),
            BlendMode("饱和度",   "saturation",  Color(0xFFAA6644), Color(0xFFCC8866)),
            BlendMode("颜色",     "color",       Color(0xFF6644AA), Color(0xFF8866CC)),
            BlendMode("明度",     "luminosity",  Color(0xFFAA8866), Color(0xFFCCAA88)),
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("混合模式", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("控制片段与下方图层的混合方式", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(280.dp).fillMaxWidth(),
            userScrollEnabled = true
        ) {
            items(blendModes) { mode ->
                val selected = selectedMode == mode.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CG.Card)
                        .then(
                            if (selected) Modifier.border(1.5.dp, CG.Acc, RoundedCornerShape(8.dp))
                            else Modifier.border(1.dp, CG.Line, RoundedCornerShape(8.dp))
                        )
                        .clickable { selectedMode = mode.id },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        // Color preview box showing blend effect
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(mode.previewBase)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .align(Alignment.TopCenter)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(mode.previewBlend)
                            )
                        }
                        Text(
                            mode.name,
                            fontSize = 9.sp,
                            color = if (selected) Color.White else CG.T2,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Start,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        CgSlider("不透明度", 0, opacity, 100) { opacity = it }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("应用混合模式") {
            val params = mapOf<String, Any>(
                "mode" to selectedMode,
                "opacity" to opacity / 100f
            )
            val result = bridge.applyEffect("blend", params)
            if (result) {
                vm.showToast("混合模式已应用: ${blendModes.find { it.id == selectedMode }?.name}")
            } else {
                vm.showToast("应用混合模式失败")
            }
            onClose()
        }
    }
}
