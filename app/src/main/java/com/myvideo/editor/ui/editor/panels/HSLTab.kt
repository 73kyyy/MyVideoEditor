package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.EditorBridge
import com.myvideo.editor.ui.editor.EditorViewModel

private data class HSLParams(var hue: Int = 0, var saturation: Int = 0, var luminance: Int = 0)

@Composable
fun HSLTab(vm: EditorViewModel, bridge: EditorBridge) {
    val trackId = vm.selectedClipId ?: "default"
    val colorEntries = listOf(
        "红" to Color(0xFFEF5350), "橙" to Color(0xFFFF7043), "黄" to Color(0xFFFFCA28),
        "绿" to Color(0xFF66BB6A), "青" to Color(0xFF26C6DA), "蓝" to Color(0xFF42A5F5),
        "紫" to Color(0xFFAB47BC), "洋红" to Color(0xFFEC407A)
    )
    var expandedChannel by remember { mutableStateOf<String?>(null) }
    val channelParams = remember { mutableStateMapOf<String, HSLParams>() }

    // Push a single channel's values to EditorBridge
    fun pushChannel(channelName: String) {
        val params = channelParams[channelName] ?: return
        val key = when (channelName) {
            "红" -> "red"; "橙" -> "orange"; "黄" -> "yellow"; "绿" -> "green"
            "青" -> "cyan"; "蓝" -> "blue"; "紫" -> "purple"; "洋红" -> "magenta"
            else -> channelName
        }
        bridge.setTrackProperty(trackId, "hsl_${key}_hue", params.hue.toFloat())
        bridge.setTrackProperty(trackId, "hsl_${key}_sat", params.saturation.toFloat())
        bridge.setTrackProperty(trackId, "hsl_${key}_lum", params.luminance.toFloat())
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text("HSL 调整", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // Color channel selector row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            colorEntries.forEach { (name, color) ->
                val sel = expandedChannel == name
                Box(modifier = Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = if (sel) 0.8f else 0.2f))
                    .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(6.dp)) else Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(6.dp)))
                    .clickable { expandedChannel = if (expandedChannel == name) null else name },
                    contentAlignment = Alignment.Center) {
                    Text(name, fontSize = 8.sp, color = if (sel) Color.White else color)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Expandable section for selected channel
        val currentChannel = expandedChannel
        if (currentChannel != null) {
            val params = channelParams.getOrPut(currentChannel) { HSLParams() }

            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(CG.Card).border(1.dp, CG.Line, RoundedCornerShape(8.dp))
                .padding(12.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp))
                            .background(colorEntries.find { it.first == currentChannel }?.second ?: Color.White))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$currentChannel 通道", fontSize = 10.sp, color = CG.T1, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    CgSlider("色相偏移", -180, params.hue, 180) {
                        params.hue = it
                        channelParams[currentChannel] = params.copy(hue = it)
                        pushChannel(currentChannel)
                    }
                    CgSlider("饱和度", -100, params.saturation, 100) {
                        params.saturation = it
                        channelParams[currentChannel] = params.copy(saturation = it)
                        pushChannel(currentChannel)
                    }
                    CgSlider("明度", -100, params.luminance, 100) {
                        params.luminance = it
                        channelParams[currentChannel] = params.copy(luminance = it)
                        pushChannel(currentChannel)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("重置当前", fontSize = 9.sp, color = CG.Acc,
                        modifier = Modifier.clickable {
                            channelParams[currentChannel] = HSLParams()
                            pushChannel(currentChannel)
                        })
                }
            }
        } else {
            // Show all channels in compact form
            colorEntries.forEach { (name, color) ->
                val params = channelParams[name] ?: HSLParams()
                val hasAdjustment = params.hue != 0 || params.saturation != 0 || params.luminance != 0
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clickable { expandedChannel = name }) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp))
                        .background(color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, fontSize = 9.sp, color = if (hasAdjustment) CG.AccL else CG.T3,
                        modifier = Modifier.weight(1f))
                    if (hasAdjustment) {
                        Text("H${params.hue} S${params.saturation} L${params.luminance}",
                            fontSize = 7.sp, color = CG.T3)
                    } else {
                        Text("--", fontSize = 7.sp, color = CG.T4)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("重置全部", fontSize = 9.sp, color = CG.Acc, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    colorEntries.forEach { (name, _) ->
                        channelParams[name] = HSLParams()
                        pushChannel(name)
                    }
                })
        }
    }
}
