package com.myvideo.editor.ui.settings

import androidx.compose.foundation.background
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
    val Bg = Color(0xFF0D0D0D)
    val Card = Color(0xFF1A1A1A)
    val CardAlt = Color(0xFF1A1A1A)
    val Gold = Color(0xFFD4AF37)
    val GoldL = Color(0xFFE8C84A)
    val GoldT = Color(0xFFE8D5A0)
    val GoldD = Color(0xFF8A7A5A)
    val T1 = Color(0xFFE8E8E8)
    val T2 = Color(0xFFE0E0E0)
    val T3 = Color(0xFF666666)
    val T4 = Color(0xFF555555)
    val T5 = Color(0xFF444444)
    val T6 = Color(0xFF333333)
    val Line = Color(0xFF252525)
    val Icon = Color(0xFF888888)
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
        Row(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 22.sp, color = SC.T3,
                modifier = Modifier.clickable { onBack() })
            Spacer(modifier = Modifier.width(4.dp))
            Text("设置", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = SC.T1)
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {

            // ===== 用户头像区 =====
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    // 头像
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))))
                        .border(2.dp, Color(0xFF333333), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    // 用户信息
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = SC.T1)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(if (userName == "未登录") "点击登录账号" else "NexClip 用户",
                            fontSize = 11.sp, color = SC.T3)
                    }
                    Text("›", fontSize = 18.sp, color = SC.T5)
                }
            }

            // ===== VIP 会员卡片 =====
            item { VipCard(isMember = isMember, onOpenMember = onOpenMemberCenter) }

            // ===== 登录方式 =====
            item {
                Text("登录方式", fontSize = 12.sp, color = SC.T4, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 10.dp))
            }
            item { LoginMethodsRow() }

            // ===== 安全与隐私 =====
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                SettingsGroup {
                    SettingIconRow("隐私政策", onClick = onOpenPrivacy)
                    SettingDivider()
                    SettingIconRow("用户协议", onClick = onOpenTerms)
                    SettingDivider()
                    ToggleIconRow("使用数据统计", checked = analyticsEnabled) { analyticsEnabled = it }
                    SettingDivider()
                    ToggleIconRow("应用锁", checked = appLockEnabled) { appLockEnabled = it }
                }
            }

            // ===== 关于 =====
            item {
                SettingsGroup {
                    SettingIconRow("版本号", value = "v1.0.0", onClick = onOpenAbout)
                    SettingDivider()
                    SettingIconRow("检查更新", onClick = { /* check */ })
                    SettingDivider()
                    SettingIconRow("开源许可", onClick = onOpenLicenses)
                }
            }

            // Footer
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NexClip", fontSize = 12.sp, color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                    Text("v1.0.0", fontSize = 10.sp, color = Color(0xFF2A2A2A),
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// ===== VIP 会员卡片 =====
@Composable
private fun VipCard(isMember: Boolean, onOpenMember: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF1C1510), Color(0xFF2A1A0E), Color(0xFF1A1008))))
        .border(1.dp, SC.Gold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
        .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // VIP 徽章
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(SC.Gold, Color(0xFFC49B30))))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DiamondIcon(size = 10.dp, tint = Color(0xFF1A1008))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("VIP", fontSize = 11.sp, color = Color(0xFF1A1008),
                            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("NexClip 会员", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = SC.GoldT, modifier = Modifier.weight(1f))
                // 开通按钮
                Box(modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(SC.Gold, SC.GoldL)))
                    .clickable { onOpenMember() }
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(if (isMember) "续费" else "开通", fontSize = 12.sp,
                        color = Color(0xFF1A1008), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("开通会员，解锁全部 AI 智能功能", fontSize = 11.sp, color = SC.GoldD, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                VipFeature("AI 抠图")
                VipFeature("超分辨率")
                VipFeature("智能插帧")
                VipFeature("AI 降噪")
            }
        }
    }
}

@Composable
private fun VipFeature(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SC.Gold))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = SC.GoldD)
    }
}

// ===== 钻石图标 =====
@Composable
private fun DiamondIcon(modifier: Modifier = Modifier, size: Dp = 8.dp, tint: Color = SC.Gold) {
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

// ===== 登录方式 =====
@Composable
private fun LoginMethodsRow() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LoginBtn("📱", "手机号")
        LoginBtn("💬", "微信")
        LoginBtn("🐧", "QQ")
        LoginBtn("🍎", "Apple")
    }
}

@Composable
private fun LoginBtn(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { /* login */ }) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape)
            .background(SC.CardAlt).border(1.dp, Color(0xFF252525), CircleShape),
            contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 10.sp, color = SC.T4)
    }
}

// ===== 设置组 =====
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(SC.Card).padding(0.dp), content = content)
}

@Composable
private fun SettingIconRow(title: String, value: String = "", onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 14.sp, color = SC.T2, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 12.sp, color = SC.T3, modifier = Modifier.padding(end = 4.dp))
        }
        Text("›", fontSize = 16.sp, color = SC.T6)
    }
}

@Composable
private fun ToggleIconRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 14.sp, color = SC.T2, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(42.dp).height(24.dp).clip(RoundedCornerShape(12.dp))
            .background(if (checked) SC.Gold else SC.T6).clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(modifier = Modifier.padding(2.dp).size(20.dp).clip(RoundedCornerShape(10.dp)).background(Color.White))
        }
    }
}

@Composable
private fun SettingDivider() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(0.5.dp).background(SC.Line))
}
