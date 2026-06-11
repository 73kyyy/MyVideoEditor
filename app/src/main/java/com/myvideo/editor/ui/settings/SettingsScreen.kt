package com.myvideo.editor.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object SC {
    val Bg = Color(0xFF080808); val Surf = Color(0xFF111111); val Card = Color(0xFF161616)
    val CardAlt = Color(0xFF1E1E1E); val Acc = Color(0xFF4A90D9); val Acc2 = Color(0xFF6EC850)
    val Gold = Color(0xFFE8A820); val Green = Color(0xFF6EC850); val Red = Color(0xFFE84848)
    val Pink = Color(0xFFFF4D7A)
    val T1 = Color(0xFFF0ECE4); val T2 = Color(0xFFB0ACA4); val T3 = Color(0xFF6A6660)
    val T4 = Color(0xFF3A3A3A); val Line = Color(0xFF1A1A1A); val Line2 = Color(0xFF242424)
}

@Composable
fun SettingsScreen(
    onOpenMemberCenter: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isMember by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("未登录") }
    var analyticsEnabled by remember { mutableStateOf(false) }
    var appLockEnabled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(SC.Bg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(SC.Card)
                .clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("‹", fontSize = 20.sp, color = SC.T2)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("设置", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SC.T1)
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {

            // ===== 会员卡片 =====
            item { MemberCard(isMember = isMember, userName = userName, onOpenMember = onOpenMemberCenter) }

            // ===== 登录方式 =====
            item { SectionHeader("登录方式") }
            item { LoginMethodsCard() }

            // ===== 安全与隐私 =====
            item { SectionHeader("安全与隐私") }
            item {
                SettingsCard {
                    SettingIconRow(
                        icon = SettingIcon.Shield,
                        title = "隐私政策",
                        onClick = onOpenPrivacy
                    )
                    SettingDivider()
                    SettingIconRow(
                        icon = SettingIcon.Doc,
                        title = "用户协议",
                        onClick = onOpenTerms
                    )
                    SettingDivider()
                    ToggleIconRow(
                        icon = SettingIcon.Clock,
                        title = "使用数据统计",
                        checked = analyticsEnabled,
                        onCheckedChange = { analyticsEnabled = it }
                    )
                    SettingDivider()
                    ToggleIconRow(
                        icon = SettingIcon.Lock,
                        title = "应用锁",
                        checked = appLockEnabled,
                        onCheckedChange = { appLockEnabled = it }
                    )
                }
            }

            // ===== 关于 =====
            item { SectionHeader("关于") }
            item {
                SettingsCard {
                    SettingIconRow(
                        icon = SettingIcon.Info,
                        title = "版本号",
                        value = "v1.0.0",
                        onClick = onOpenAbout
                    )
                    SettingDivider()
                    SettingIconRow(
                        icon = SettingIcon.Update,
                        title = "检查更新",
                        onClick = { /* check */ }
                    )
                    SettingDivider()
                    SettingIconRow(
                        icon = SettingIcon.Diamond,
                        title = "开源许可",
                        onClick = onOpenLicenses
                    )
                }
            }

            item {
                Text("NexClip · AI 智能视频编辑器", fontSize = 9.sp, color = SC.T3,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}

// ===== 会员卡片 =====
@Composable
private fun MemberCard(isMember: Boolean, userName: String, onOpenMember: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF1A1020), Color(0xFF201018), Color(0xFF1A0C14))))
        .border(1.dp, SC.Pink.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
        .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 钻石图标
                Box(modifier = Modifier.size(36.dp)) {
                    DiamondIcon(size = 36.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                // 用户信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(userName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SC.T1)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(SC.Pink.copy(0.2f), Color(0xFFA050C8).copy(0.2f))))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DiamondIcon(size = 7.dp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(if (isMember) "会员" else "非会员", fontSize = 9.sp,
                                    color = SC.Pink, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                // 开通按钮
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(SC.Pink, Color(0xFFD94090))))
                    .clickable { onOpenMember() }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(if (isMember) "续费会员" else "开通会员", fontSize = 11.sp,
                        color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            // 权益图标行
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(0.06f)))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                BenefitItem(color = SC.Pink, label = "AI 抠图")
                BenefitItem(color = SC.Acc, label = "超分辨率")
                BenefitItem(color = SC.Green, label = "智能插帧")
                BenefitItem(color = SC.Gold, label = "更多功能")
            }
        }
    }
}

@Composable
private fun BenefitItem(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.1f)), contentAlignment = Alignment.Center) {
            DiamondIcon(size = 12.dp, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, color = SC.T3)
    }
}

// ===== 钻石图标 =====
@Composable
private fun DiamondIcon(modifier: Modifier = Modifier, size: Dp = 8.dp, tint: Color = SC.Pink) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.4f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.4f)
            close()
        }
        drawPath(path, tint)
    }
}

// ===== 登录方式卡片 =====
@Composable
private fun LoginMethodsCard() {
    var selectedMethod by remember { mutableStateOf("phone") }
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(SC.Card).padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            LoginMethodBtn("phone", "手机", selectedMethod == "phone") { selectedMethod = it }
            LoginMethodBtn("wechat", "微信", selectedMethod == "wechat") { selectedMethod = it }
            LoginMethodBtn("qq", "QQ", selectedMethod == "qq") { selectedMethod = it }
            LoginMethodBtn("apple", "Apple", selectedMethod == "apple") { selectedMethod = it }
        }
    }
}

@Composable
private fun LoginMethodBtn(method: String, label: String, selected: Boolean, onSelect: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
            .background(if (selected) SC.Acc.copy(0.08f) else SC.CardAlt)
            .border(1.dp, if (selected) SC.Acc else Color(0xFF242424), RoundedCornerShape(12.dp))
            .clickable { onSelect(method) },
            contentAlignment = Alignment.Center
        ) {
            Text(when (method) {
                "phone" -> "📱"
                "wechat" -> "💬"
                "qq" -> "🐧"
                else -> ""
            }, fontSize = 18.sp)
            if (selected) {
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                    .size(14.dp).clip(CircleShape).background(SC.Acc),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, color = SC.T3)
    }
}

// ===== 设置图标类型 =====
private enum class SettingIcon { Shield, Doc, Clock, Lock, Info, Update, Diamond }

// ===== 带图标的设置行 =====
@Composable
private fun SettingIconRow(icon: SettingIcon, title: String, value: String = "", onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }
        .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // 彩色圆点图标
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
            .background(iconColor(icon).copy(0.1f)), contentAlignment = Alignment.Center) {
            Text(iconEmoji(icon), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 10.sp, color = SC.T2,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SC.CardAlt)
                    .padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text("›", fontSize = 16.sp, color = SC.T3)
    }
}

// ===== 带图标的开关行 =====
@Composable
private fun ToggleIconRow(icon: SettingIcon, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
            .background(iconColor(icon).copy(0.1f)), contentAlignment = Alignment.Center) {
            Text(iconEmoji(icon), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SC.T1, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(44.dp).height(24.dp).clip(RoundedCornerShape(12.dp))
            .background(if (checked) SC.Acc else SC.T4).clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(modifier = Modifier.padding(2.dp).size(20.dp).clip(RoundedCornerShape(10.dp)).background(Color.White))
        }
    }
}

private fun iconColor(icon: SettingIcon) = when (icon) {
    SettingIcon.Shield -> SC.Acc
    SettingIcon.Doc -> SC.Green
    SettingIcon.Clock -> SC.Gold
    SettingIcon.Lock -> SC.Pink
    SettingIcon.Info -> SC.Acc
    SettingIcon.Update -> SC.Green
    SettingIcon.Diamond -> SC.Gold
}

private fun iconEmoji(icon: SettingIcon) = when (icon) {
    SettingIcon.Shield -> "🛡"
    SettingIcon.Doc -> "📋"
    SettingIcon.Clock -> "⏱"
    SettingIcon.Lock -> "🔒"
    SettingIcon.Info -> "ℹ️"
    SettingIcon.Update -> "🔄"
    SettingIcon.Diamond -> "💎"
}

// ===== 通用组件 =====
@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 11.sp, color = SC.T3, letterSpacing = 1.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 6.dp, top = 16.dp, bottom = 6.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(SC.Card).padding(2.dp), content = content)
}

@Composable
private fun SettingDivider() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(0.5.dp).background(SC.Line))
}
