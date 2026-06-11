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

// 许可证颜色
private val MitColor = Color(0xFF34C759)
private val ApacheColor = Color(0xFF0A84FF)
private val BsdColor = Color(0xFFFF9F0A)
private val LgplColor = Color(0xFFAF52DE)

private data class LicenseEntry(
    val name: String, val desc: String, val licenseType: String, val licenseColor: Color,
    val licenseText: String
)

private val licenseSections = mapOf(
    "AI 框架" to listOf(
        LicenseEntry("ONNX Runtime", "跨平台高性能推理引擎", "MIT", MitColor, "Copyright (c) Microsoft Corporation\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT."),
        LicenseEntry("TensorFlow Lite", "移动端机器学习推理框架", "Apache 2.0", ApacheColor, "Copyright 2017 The TensorFlow Authors\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n\n    http://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."),
        LicenseEntry("PyTorch Mobile", "端侧深度学习推理框架", "BSD 3-Clause", BsdColor, "Copyright (c) 2016, Meta Platforms, Inc. All rights reserved.\n\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\n1. Redistributions of source code must retain the above copyright notice.\n2. Redistributions in binary form must reproduce the above copyright notice.\n3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES ARE DISCLAIMED.")
    ),
    "AI 模型" to listOf(
        LicenseEntry("OpenAI Whisper", "语音识别模型", "MIT", MitColor, "Copyright (c) 2022 OpenAI\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED."),
        LicenseEntry("RNNoise", "基于RNN的语音降噪", "BSD 3-Clause", BsdColor, "Copyright (c) 2017, Mozilla Corporation\n\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the above copyright notice is retained.\n\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES ARE DISCLAIMED."),
        LicenseEntry("RIFE", "实时视频插帧模型", "MIT", MitColor, "Copyright (c) 2021 Megvii Research\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.")
    ),
    "多媒体处理" to listOf(
        LicenseEntry("FFmpeg", "音视频编解码库", "LGPL 2.1+", LgplColor, "FFmpeg is licensed under the GNU Lesser General Public License (LGPL) version 2.1 or later.\n\nThis program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE."),
        LicenseEntry("OpenCV", "计算机视觉库", "Apache 2.0", ApacheColor, "Copyright (c) 2000-2023 Intel Corporation\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.")
    ),
    "Android 框架" to listOf(
        LicenseEntry("Jetpack Compose", "Android 现代 UI 工具包", "Apache 2.0", ApacheColor, "Copyright 2019 The Android Open Source Project\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License."),
        LicenseEntry("Kotlin / Kotlin Coroutines", "编程语言与协程库", "Apache 2.0", ApacheColor, "Copyright 2010-2023 JetBrains s.r.o.\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License."),
        LicenseEntry("OkHttp", "HTTP 客户端", "Apache 2.0", ApacheColor, "Copyright 2014 Square, Inc.\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.")
    )
)

@Composable
fun SettingsScreen(
    onOpenMemberCenter: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isMember by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("未登录") }
    var analyticsEnabled by remember { mutableStateOf(false) }
    var showLoginSheet by remember { mutableStateOf(false) }
    var showLicensesSheet by remember { mutableStateOf(false) }
    var selectedLic by remember { mutableStateOf<LicenseEntry?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {

            // 导航栏
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

            // 用户信息 - 点击弹出登录
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

            // VIP 会员卡
            item { VipCard(isMember = isMember, onOpenMember = onOpenMemberCenter) }

            // 安全与隐私
            item {
                SettingsGroup {
                    SettingRow("🛡", "隐私政策", onClick = onOpenPrivacy)
                    GroupSep()
                    SettingRow("📋", "用户协议", onClick = onOpenTerms)
                    GroupSep()
                    ToggleRow("⏱", "使用数据统计", checked = analyticsEnabled) { analyticsEnabled = it }
                    GroupSep()
                    SettingRow("💎", "开源许可", onClick = { showLicensesSheet = true })
                }
            }

            // 关于
            item {
                SettingsGroup {
                    SettingRow("ℹ️", "版本号", value = "v1.0.0", onClick = onOpenAbout)
                    GroupSep()
                    SettingRow("🔄", "检查更新", onClick = { /* check */ })
                }
            }

            // 退出登录
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Card)
                    .clickable { onLogout() }
                    .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center) {
                    Text("退出登录", fontSize = 15.sp, color = Red, fontWeight = FontWeight.Medium)
                }
            }

            // 底部
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

        // 登录弹窗
        AnimatedVisibility(
            visible = showLoginSheet,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Card)
                .padding(horizontal = 24.dp, vertical = 20.dp).padding(bottom = 40.dp)
            ) {
                Box(modifier = Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(Arrow).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(20.dp))
                Text("登录 NexClip", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = T1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                Text("登录后同步你的创作数据", fontSize = 13.sp, color = T2,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(28.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LoginBtn("📱", "手机号")
                    LoginBtn("💬", "微信")
                    LoginBtn("🐧", "QQ")
                }
                Spacer(modifier = Modifier.height(20.dp))
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

        // 开源许可弹窗
        AnimatedVisibility(
            visible = showLicensesSheet,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth().height(560.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Card)
                .padding(horizontal = 20.dp, vertical = 20.dp).padding(bottom = 40.dp)
            ) {
                Box(modifier = Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(Arrow).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    licenseSections.forEach { (section, entries) ->
                        item {
                            Text(section, fontSize = 11.sp, color = Color(0xFF48484A),
                                fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(entries) { entry ->
                            Column(modifier = Modifier.padding(vertical = 6.dp).clickable { selectedLic = entry }) {
                                Text(entry.name, fontSize = 13.sp, color = Color(0xFFE0E0E0), fontWeight = FontWeight.Medium)
                                Text(entry.desc, fontSize = 10.sp, color = T2, modifier = Modifier.padding(top = 1.dp))
                                Box(modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(3.dp))
                                    .background(entry.licenseColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)) {
                                    Text(entry.licenseType, fontSize = 9.sp, color = entry.licenseColor,
                                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(10.dp)) }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(CardHover).clickable { showLicensesSheet = false }
                    .padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("关闭", fontSize = 15.sp, color = T2, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 许可证详情弹窗
        if (selectedLic != null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))
                .clickable { selectedLic = null })
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Card)
                .padding(horizontal = 20.dp, vertical = 20.dp).padding(bottom = 40.dp)
            ) {
                Box(modifier = Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(Arrow).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                Text(selectedLic!!.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(selectedLic!!.licenseType, fontSize = 12.sp, color = T2, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                Text(selectedLic!!.licenseText, fontSize = 11.sp, color = Icon, lineHeight = 18.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(CardHover).clickable { selectedLic = null }
                    .padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("关闭", fontSize = 15.sp, color = T2, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 遮罩层
        if (showLoginSheet || showLicensesSheet) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))
                .clickable {
                    if (showLoginSheet) showLoginSheet = false
                    if (showLicensesSheet) showLicensesSheet = false
                })
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
                VipPerk("AI 抠图"); VipPerk("超分辨率"); VipPerk("智能插帧"); VipPerk("AI 降噪")
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
