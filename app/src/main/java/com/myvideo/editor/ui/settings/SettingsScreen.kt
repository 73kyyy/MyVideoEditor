package com.myvideo.editor.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
private val T4 = Color(0xFF8A8A8E)
private val Gold = Color(0xFFD4AF37)
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
    var showLoginSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
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

            // ===== 用户信息 - 点击弹出登录 =====
            item {
                Row(modifier = Modifier.fillMaxWidth().clickable { showLoginSheet = true }
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
                }
            }

            // ===== 关于（会员隐藏在这里） =====
            item {
                SettingsGroup {
                    // 会员行 - 低调隐藏式
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOpenMemberCenter() }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("★", fontSize = 16.sp, color = T2, modifier = Modifier.width(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("NexClip 会员", fontSize = 14.sp, color = T4, modifier = Modifier.weight(1f))
                        // 小VIP标签
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Gold.copy(0.08f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DiamondIcon(size = 7.dp, tint = Gold)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("VIP", fontSize = 9.sp, color = Gold, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("›", fontSize = 16.sp, color = Arrow)
                    }
                    GroupSep()
                    SettingRow("ℹ️", "版本号", value = "v1.0.0", onClick = onOpenAbout)
                    GroupSep()
                    SettingRow("🔄", "检查更新", onClick = { /* check */ })
                    GroupSep()
                    SettingRow("💎", "开源许可", onClick = onOpenLicenses)
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

        // ===== 登录弹窗（底部滑出） =====
        AnimatedVisibility(
            visible = showLoginSheet,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Card)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .padding(bottom = 40.dp)
            ) {
                // 拖拽条
                Box(modifier = Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(Arrow).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(20.dp))
                Text("登录 NexClip", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = T1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                Text("登录后同步你的创作数据", fontSize = 13.sp, color = T2,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(28.dp))
                // 登录方式
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LoginBtn("📱", "手机号")
                    LoginBtn("💬", "微信")
                    LoginBtn("🐧", "QQ")
                    LoginBtn("🍎", "Apple")
                }
                Spacer(modifier = Modifier.height(20.dp))
                // 取消
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(CardHover).clickable { showLoginSheet = false }
                    .padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("取消", fontSize = 15.sp, color = T2, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("登录即表示同意 用户协议 和 隐私政策", fontSize = 10.sp, color = Color(0xFF48484A),
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }

        // 遮罩层
        if (showLoginSheet) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))
                .clickable { showLoginSheet = false })
        }
    }
}

// ===== 钻石图标 =====
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
        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(CardHover),
            contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, color = Icon)
    }
}

// ===== 设置分组 =====
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
        .clip(RoundedCornerShape(12.dp)).background(Card), content = content)
}

@Composable
private fun SettingRow(icon: String, title: String, value: String = "", onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, color = T1, modifier = Modifier.weight(1f))
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
