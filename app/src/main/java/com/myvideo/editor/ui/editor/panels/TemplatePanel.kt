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
import com.myvideo.editor.ui.editor.EditorViewModel

@Composable
fun TemplatePanel(vm: EditorViewModel, onApply: (String) -> Unit, onClose: () -> Unit) {
    var selectedTemplate by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf("全部") }

    data class Template(val name: String, val category: String, val c1: Color, val c2: Color, val desc: String, val duration: String)

    val templates = listOf(
        Template("Vlog开场", "日常", Color(0xFF4A90D9), Color(0xFF6AAFE6), "适合日常Vlog开头", "5s"),
        Template("旅行记录", "旅行", Color(0xFF26A69A), Color(0xFF80CBC4), "旅行风景展示", "8s"),
        Template("美食分享", "美食", Color(0xFFFF7043), Color(0xFFFFAB91), "美食制作展示", "6s"),
        Template("产品展示", "商业", Color(0xFF5C6BC0), Color(0xFF9FA8DA), "产品360度展示", "10s"),
        Template("运动集锦", "运动", Color(0xFFEF5350), Color(0xFFE57373), "运动精彩瞬间", "7s"),
        Template("婚礼相册", "婚礼", Color(0xFFEC407A), Color(0xFFF48FB1), "浪漫婚礼回忆", "12s"),
        Template("生日祝福", "节日", Color(0xFFFFCA28), Color(0xFFFFE082), "生日派对视频", "8s"),
        Template("新年快乐", "节日", Color(0xFFE53935), Color(0xFFEF5350), "新年祝福视频", "6s"),
        Template("电影预告", "电影", Color(0xFF212121), Color(0xFF424242), "电影风格预告片", "15s"),
        Template("教学演示", "教育", Color(0xFF43A047), Color(0xFF66BB6A), "教学内容展示", "10s"),
        Template("音乐MV", "音乐", Color(0xFF7B1FA2), Color(0xFFAB47BC), "音乐视频模板", "20s"),
        Template("游戏集锦", "游戏", Color(0xFFE91E63), Color(0xFFFF4081), "游戏精彩时刻", "8s"),
        Template("时尚大片", "时尚", Color(0xFF000000), Color(0xFF424242), "时尚风格展示", "10s"),
        Template("科技产品", "科技", Color(0xFF0D47A1), Color(0xFF1976D2), "科技产品发布", "8s"),
        Template("宠物日常", "日常", Color(0xFFFF8A65), Color(0xFFFFAB91), "可爱宠物瞬间", "6s"),
        Template("宝宝成长", "家庭", Color(0xFF4FC3F7), Color(0xFF81D4FA), "宝宝成长记录", "10s"),
        Template("毕业季", "校园", Color(0xFF1565C0), Color(0xFF42A5F5), "毕业纪念视频", "12s"),
        Template("直播切片", "直播", Color(0xFF9C27B0), Color(0xFFCE93D8), "直播精彩片段", "5s"),
        Template("探店打卡", "美食", Color(0xFFFF9800), Color(0xFFFFB74D), "探店体验展示", "8s"),
        Template("健身教程", "运动", Color(0xFF388E3C), Color(0xFF66BB6A), "健身动作演示", "10s")
    )

    val categories = listOf("全部", "日常", "旅行", "美食", "商业", "运动", "婚礼", "节日", "电影", "教育", "音乐", "游戏", "时尚", "科技", "家庭", "校园", "直播")
    val filtered = if (category == "全部") templates else templates.filter { it.category == category }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("项目模板", fontSize = 9.sp, color = CG.T4, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("选择模板快速创建项目，自动匹配转场和音乐", fontSize = 8.sp, color = CG.T3)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("全部", "日常", "旅行", "美食", "商业", "运动", "婚礼", "节日").forEach { cat ->
                OptionChip(cat, category == cat) { category = cat }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("电影", "教育", "音乐", "游戏", "时尚", "科技", "家庭", "校园").forEach { cat ->
                OptionChip(cat, category == cat) { category = cat }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        filtered.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { template ->
                    val sel = selectedTemplate == template.name
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(template.c1, template.c2)))
                            .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier)
                            .clickable { selectedTemplate = template.name },
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(template.name, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(template.duration, fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(template.desc, fontSize = 7.sp, color = CG.T3)
                        Text("${template.category} · ${template.duration}", fontSize = 7.sp, color = CG.T4)
                    }
                }
                repeat(2 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        ApplyButton("套用模板") {
            if (selectedTemplate != null) {
                onApply(selectedTemplate!!)
            } else {
                vm.showToast("请选择模板")
            }
            onClose()
        }
    }
}
