package com.myvideo.editor.ui.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterTab(selected: String, onSelect: (String) -> Unit) {
    var category by remember { mutableStateOf("全部") }
    val categories = listOf("全部", "电影", "风景", "人像", "复古", "艺术", "特效")

    val filters = listOf(
        FilterItem("自然", "基础", Color(0xFF8BC34A), Color(0xFF4CAF50)),
        FilterItem("黑白", "基础", Color(0xFF616161), Color(0xFF9E9E9E)),
        FilterItem("复古", "复古", Color(0xFFD4A574), Color(0xFF8D6E63)),
        FilterItem("冷色", "风景", Color(0xFF4A90D9), Color(0xFF1565C0)),
        FilterItem("暖色", "风景", Color(0xFFE8A820), Color(0xFFFF8F00)),
        FilterItem("高对比", "基础", Color(0xFF424242), Color(0xFF757575)),
        FilterItem("柔和", "人像", Color(0xFFB0BEC5), Color(0xFFCFD8DC)),
        FilterItem("鲜艳", "风景", Color(0xFFFF7043), Color(0xFFE64A19)),
        FilterItem("怀旧", "复古", Color(0xFFA1887F), Color(0xFF6D4C41)),
        FilterItem("胶片", "复古", Color(0xFF8D6E63), Color(0xFF5D4037)),
        FilterItem("HDR", "风景", Color(0xFF78909C), Color(0xFF37474F)),
        FilterItem("电影", "电影", Color(0xFF5C6BC0), Color(0xFF283593)),
        FilterItem("模糊", "特效", Color(0xFF90A4AE), Color(0xFF546E7A)),
        FilterItem("锐化", "基础", Color(0xFF26A69A), Color(0xFF00695C)),
        FilterItem("马赛克", "特效", Color(0xFFEF5350), Color(0xFFC62828)),
        FilterItem("像素化", "特效", Color(0xFFFFCA28), Color(0xFFF9A825)),
        FilterItem("浮雕", "艺术", Color(0xFF7E57C2), Color(0xFF4527A0)),
        FilterItem("老电影", "复古", Color(0xFF6D4C41), Color(0xFF3E2723)),
        FilterItem("故障", "特效", Color(0xFFEC407A), Color(0xFFAD1457)),
        FilterItem("霓虹", "艺术", Color(0xFFAB47BC), Color(0xFF6A1B9A)),
        FilterItem("油画", "艺术", Color(0xFF66BB6A), Color(0xFF2E7D32)),
        FilterItem("素描", "艺术", Color(0xFF455A64), Color(0xFF263238)),
        FilterItem("卡通", "艺术", Color(0xFF29B6F6), Color(0xFF0277BD)),
        FilterItem("LOMO", "复古", Color(0xFFE86020), Color(0xFFBF360C)),
        FilterItem("日系", "人像", Color(0xFFE8E0D0), Color(0xFFD7CCC8)),
        FilterItem("青橙", "电影", Color(0xFFE86A20), Color(0xFFBF5B00)),
        FilterItem("黑金", "电影", Color(0xFFE8A820), Color(0xFFBF8A00)),
        FilterItem("粉紫", "电影", Color(0xFFE870A0), Color(0xFFAD1457)),
        FilterItem("森林", "风景", Color(0xFF2A4A1A), Color(0xFF1B5E20)),
        FilterItem("海洋", "风景", Color(0xFF1A5A7E), Color(0xFF0D47A1)),
        FilterItem("黄昏", "风景", Color(0xFFE85030), Color(0xFFBF360C)),
        FilterItem("雪景", "风景", Color(0xFFD0D8E0), Color(0xFFECEFF1)),
        FilterItem("电影蓝", "电影", Color(0xFF2A3A5E), Color(0xFF1A237E)),
        FilterItem("暗调", "电影", Color(0xFF1A1A1A), Color(0xFF212121)),
        FilterItem("街头", "艺术", Color(0xFF4A4A3A), Color(0xFF3E3E2E)),
        FilterItem("赛博", "特效", Color(0xFF2A0A4E), Color(0xFF4A148C))
    )

    val filtered = if (category == "全部") filters else filters.filter { it.category == category }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("滤镜预设", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            categories.forEach { cat ->
                OptionChip(cat, category == cat) { category = cat }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        filtered.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { f ->
                    val sel = selected == f.name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(f.color1, f.color2)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { onSelect(f.name) },
                            contentAlignment = Alignment.Center) {
                            Text(f.name, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                repeat(4 - row.size) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.weight(1f).fillMaxWidth().height(52.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("当前: ${selected}", fontSize = 9.sp, color = CG.T3)
    }
}

private data class FilterItem(val name: String, val category: String, val color1: Color, val color2: Color)
