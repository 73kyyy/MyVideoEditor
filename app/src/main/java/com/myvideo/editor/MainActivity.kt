package com.myvideo.editor

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.myvideo.editor.security.UIProtector
import com.myvideo.editor.navigation.NavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UIProtector：防截屏+防录屏+防最近任务泄露
        UIProtector.enableScreenProtection(this)
        UIProtector.protectAgainstTapjacking(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
                    NavGraph()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        UIProtector.enableScreenProtection(this)
    }

    override fun onPause() {
        super.onPause()
        // 最近任务保护：隐藏敏感内容
        window.decorView.visibility = android.view.View.INVISIBLE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // 失去焦点：可能进入最近任务
            try {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } catch (_: Exception) {}
        } else {
            window.decorView.visibility = android.view.View.VISIBLE
        }
    }
}
