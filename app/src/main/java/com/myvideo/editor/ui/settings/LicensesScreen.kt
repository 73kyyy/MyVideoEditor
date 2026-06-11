package com.myvideo.editor.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF000000)
private val Card = Color(0xFF1C1C1E)
private val Sep = Color(0xFF2C2C2E)
private val T1 = Color(0xFFE0E0E0)
private val T2 = Color(0xFF636366)
private val T3 = Color(0xFF8E8E93)
private val T4 = Color(0xFF48484A)

// 许可证颜色
private val MitColor = Color(0xFF34C759)
private val ApacheColor = Color(0xFF0A84FF)
private val BsdColor = Color(0xFFFF9F0A)
private val LgplColor = Color(0xFFAF52DE)

private data class LicenseEntry(
    val name: String,
    val desc: String,
    val licenseType: String,
    val licenseColor: Color,
    val licenseText: String
)

private val licenseSections = mapOf(
    "AI 框架" to listOf(
        LicenseEntry("ONNX Runtime", "跨平台高性能推理引擎", "MIT", MitColor, """
Copyright (c) Microsoft Corporation

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
        """.trimIndent()),
        LicenseEntry("TensorFlow Lite", "移动端机器学习推理框架", "Apache 2.0", ApacheColor, """
Copyright 2017 The TensorFlow Authors

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        """.trimIndent()),
        LicenseEntry("PyTorch Mobile", "端侧深度学习推理框架", "BSD 3-Clause", BsdColor, """
Copyright (c) 2016, Meta Platforms, Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice.
2. Redistributions in binary form must reproduce the above copyright notice.
3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
        """.trimIndent())
    ),
    "AI 模型" to listOf(
        LicenseEntry("OpenAI Whisper", "语音识别模型", "MIT", MitColor, """
Copyright (c) 2022 OpenAI

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.
        """.trimIndent()),
        LicenseEntry("RNNoise", "基于RNN的语音降噪", "BSD 3-Clause", BsdColor, """
Copyright (c) 2017, Mozilla Corporation

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the above copyright notice is retained.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES ARE DISCLAIMED.
        """.trimIndent()),
        LicenseEntry("RIFE", "实时视频插帧模型", "MIT", MitColor, """
Copyright (c) 2021 Megvii Research

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.
        """.trimIndent())
    ),
    "多媒体处理" to listOf(
        LicenseEntry("FFmpeg", "音视频编解码库", "LGPL 2.1+", LgplColor, """
FFmpeg is licensed under the GNU Lesser General Public License (LGPL) version 2.1 or later.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
        """.trimIndent()),
        LicenseEntry("OpenCV", "计算机视觉库", "Apache 2.0", ApacheColor, """
Copyright (c) 2000-2023 Intel Corporation

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        """.trimIndent())
    ),
    "Android 框架" to listOf(
        LicenseEntry("Jetpack Compose", "Android 现代 UI 工具包", "Apache 2.0", ApacheColor, """
Copyright 2019 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
        """.trimIndent()),
        LicenseEntry("Kotlin / Kotlin Coroutines", "编程语言与协程库", "Apache 2.0", ApacheColor, """
Copyright 2010-2023 JetBrains s.r.o.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
        """.trimIndent()),
        LicenseEntry("OkHttp", "HTTP 客户端", "Apache 2.0", ApacheColor, """
Copyright 2014 Square, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
        """.trimIndent())
    )
)

@Composable
fun LicensesScreen(onBack: () -> Unit = {}) {
    var selectedLic by remember { mutableStateOf<LicenseEntry?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            // 导航栏
            item {
                Row(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).clickable { onBack() },
                        contentAlignment = Alignment.Center) {
                        Text("‹", fontSize = 22.sp, color = Color.White)
                    }
                    Text("开源许可", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                        modifier = Modifier.weight(1f).padding(end = 28.dp), textAlign = TextAlign.Center)
                }
            }

            // 各分类
            licenseSections.forEach { (section, entries) ->
                item {
                    Text(section, fontSize = 11.sp, color = T4, fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp))
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Card)) {
                        entries.forEachIndexed { index, entry ->
                            if (index > 0) {
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                    .height(0.33.dp).background(Sep))
                            }
                            Column(modifier = Modifier.fillMaxWidth().clickable { selectedLic = entry }
                                .padding(horizontal = 16.dp, vertical = 14.dp)) {
                                Text(entry.name, fontSize = 14.sp, color = T1, fontWeight = FontWeight.Medium)
                                Text(entry.desc, fontSize = 11.sp, color = T2, modifier = Modifier.padding(top = 2.dp))
                                // 许可证标签
                                Box(modifier = Modifier.padding(top = 6.dp).clip(RoundedCornerShape(4.dp))
                                    .background(entry.licenseColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(entry.licenseType, fontSize = 9.sp, color = entry.licenseColor,
                                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 底部
            item {
                Text("NexClip 使用了上述开源软件。\n感谢开源社区的贡献。",
                    fontSize = 10.sp, color = Color(0xFF2C2C2E), lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp), textAlign = TextAlign.Center)
            }
        }

        // 许可证详情弹窗
        if (selectedLic != null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))
                .clickable { selectedLic = null })
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Card)
                .padding(horizontal = 20.dp, vertical = 20.dp).padding(bottom = 40.dp)) {
                Box(modifier = Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A3C)).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                Text(selectedLic!!.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(selectedLic!!.licenseType, fontSize = 12.sp, color = T2, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                Text(selectedLic!!.licenseText, fontSize = 11.sp, color = T3, lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E)).clickable { selectedLic = null }
                    .padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("关闭", fontSize = 15.sp, color = T3, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
