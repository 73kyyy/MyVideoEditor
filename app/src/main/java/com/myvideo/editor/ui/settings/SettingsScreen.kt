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

// ===== 配色 =====
private val Black = Color(0xFF000000)
private val Card = Color(0xFF1C1C1E)
private val CardHover = Color(0xFF2C2C2E)
private val Sep = Color(0xFF2C2C2E)
private val Icon = Color(0xFF8E8E93)
private val Arrow = Color(0xFF3A3A3C)
private val T1 = Color(0xFFFFFFFF)
private val T2 = Color(0xFF636366)
private val T3 = Color(0xFF555555)
private val Gold = Color(0xFFD4AF37)
private val GoldL = Color(0xFFE8C84A)
private val GoldT = Color(0xFFE8D5A0)
private val GoldD = Color(0xFF8A7A5A)
private val Red = Color(0xFFFF453A)
private val SwOff = Color(0xFF39393D)

@Composable
fun SettingsScreen(
    onOpenMemberCenter: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isMember by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("未登录") }
    var analyticsEnabled by remember { mutableStateOf(false) }
    var appLockEnabled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Black)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {

            // ===== 导航栏 =====
            item {
                Row(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).clickable { onBack() },
                        contentAlignment = Alignment.Center) {
                        Text("‹", fontSize = 22.sp, color = T1)
                    }
                    Text("设置", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = T1,
                        modifier = Modifier.weight(1f).padding(end = 28.dp), textAlign = TextAlign.Center)
                }
            }

            // ===== 用户信息 =====
            item {
                Row(modifier = Modifier.fillMaxWidth().clickable { /* profile */ }
                    .padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Card),
                        contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = T1)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(if (userName == "未登录") "点击登录账号" else "NexClip 用户",
                            fontSize = 12.sp, color = T3)
                    }
                    Text("›", fontSize = 20.sp, color = Arrow)
                }
            }

            // ===== VIP 会员卡 =====
            item { VipCard(isMember = isMember, onOpenMember = onOpenMemberCenter) }

            // ===== 第三方登录 =====
            item {
                Text("第三方登录", fontSize = 12.sp, color = T3, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LoginBtn("📱", "手机号")
                    LoginBtn("💬", "微信")
                    LoginBtn("🐧", "QQ")
                    LoginBtn("🍎", "Apple")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===== 安全与隐私 =====
            item {
                SettingsGroup {
                    SettingRow("🛡", "隐私政策", onClick = onOpenPrivacy)
                    GroupSep()
                    SettingRow("📋", "用户协议", onClick = onOpenTerms)
                    GroupSep()
                    ToggleRow("⏱", "使用数据统计", checked = analyticsEnabled) { analyticsEnabled = it }
                    GroupSep()
                    ToggleRow("🔒", "应用锁", checked = appLockEnabled) { appLockEnabled = it }
                    GroupSep()
                    // 开源许可 - 隐藏式，半透明灰色
                    SettingRow("💎", "开源许可", onClick = onOpenLicenses, subtle = true)
                }
            }

            // ===== 关于 =====
            item {
                SettingsGroup {
                    SettingRow("ℹ️", "版本号", value = "v1.0.0", onClick = onOpenAbout)
                    GroupSep()
                    SettingRow("🔄", "检查更新", onClick = { /* check */ })
                }
            }

            // ===== 退出登录 =====
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Card)
                    .clickable { onLogout() }
                    .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center) {
                    Text("退出登录", fontSize = 15.sp, color = Red, fontWeight = FontWeight.Medium)
                }
            }

            // ===== 底部 =====
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NexClip", fontSize = 13.sp, color = Color(0xFF2C2C2E),
                        fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                    Text("v1.0.0", fontSize = 11.sp, color = Color(0xFF1C1C1E),
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// ===== VIP 会员卡 =====
@Composable
private fun VipCard(isMember: Boolean, onOpenMember: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF2A1F0A), Color(0xFF1F170A), Color(0xFF18120A))))
        .clickable { onOpenMember() }
        .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Gold, Color(0xFFB8962E)))),
                    contentAlignment = Alignment.Center) {
                    Text("★", fontSize = 16.sp, color = Color(0xFF1A1008))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("NexClip 会员", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GoldT)
                    Text("解锁全部 AI 智能功能", fontSize = 11.sp, color = GoldD, modifier = Modifier.padding(top = 2.dp))
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Gold, GoldL)))
                    .clickable { onOpenMember() }
                    .padding(horizontal = 20.dp, vertical = 7.dp)
                ) {
                    Text(if (isMember) "续费" else "开通", fontSize = 13.sp,
                        color = Color(0xFF1A1008), fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Gold.copy(0.1f)))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                VipPerk("AI 抠图")
                VipPerk("超分辨率")
                VipPerk("智能插帧")
                VipPerk("AI 降噪")
            }
        }
    }
}

@Composable
private fun VipPerk(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
            .background(Gold.copy(0.1f)), contentAlignment = Alignment.Center) {
            DiamondIcon(size = 14.dp, tint = Gold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = GoldD)
    }
}

@Composable
private fun DiamondIcon(modifier: Modifier = Modifier, size: Dp = 8.dp, tint: Color = Gold) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx()
        val path = Path().apply {
            moveTo(w * 0.5f, 0f); lineTo(w, h * 0.4f)
            lineTo(w * 0.5f, h); lineTo(0f, h * 0.4f); close()
        }
        drawPath(path, tint)
    }
}

// ===== 登录按钮 =====
@Composable
private fun LoginBtn(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { /* login */ }) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Card),
            contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 10.sp, color = T3)
    }
}

// ===== 设置分组 =====
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
        .clip(RoundedCornerShape(12.dp)).background(Card), content = content)
}

@Composable
private fun SettingRow(icon: String, title: String, value: String = "", onClick: () -> Unit = {}, subtle: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, color = if (subtle) Icon else T1, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = T2, modifier = Modifier.padding(end = 2.dp))
        }
        Text("›", fontSize = 16.sp, color = Arrow)
    }
}

@Composable
private fun ToggleRow(icon: String, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, color = T1, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(46.dp).height(28.dp).clip(RoundedCornerShape(14.dp))
            .background(if (checked) Gold else SwOff).clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(modifier = Modifier.padding(2.dp).size(24.dp).clip(RoundedCornerShape(12.dp)).background(Color.White))
        }
    }
}

@Composable
private fun GroupSep() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(0.33.dp).background(Sep))
}
