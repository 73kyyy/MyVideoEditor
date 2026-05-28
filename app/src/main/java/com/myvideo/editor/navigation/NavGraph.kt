package com.myvideo.editor.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myvideo.editor.engine.VideoImportManager
import com.myvideo.editor.engine.VideoPicker
import com.myvideo.editor.security.ComplianceAuditor
import com.myvideo.editor.startup.SecurityInitRunner
import com.myvideo.editor.startup.SplashViewModel
import com.myvideo.editor.theme.AppColors
import com.myvideo.editor.ui.dashboard.DashboardScreen
import com.myvideo.editor.ui.editor.EditorScreen
import com.myvideo.editor.ui.editor.EditorViewModel
import com.myvideo.editor.ui.settings.*
import com.myvideo.editor.ui.color.ColorScreen

@Composable
fun NavGraph() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("splash") }
    var showTutorial by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(ComplianceAuditor.isPrivacyAccepted(context)) }
    val editorVm = remember { EditorViewModel() }
    val importManager = remember { VideoImportManager(context) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = VideoPicker.extractUri(result.data)
            if (uri != null) {
                val success = importManager.importToEditor(uri, editorVm)
                if (success) currentTab = "editor"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.BgPrimary)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                !privacyAccepted -> {
                    PrivacyAgreeScreen(
                        onAccept = {
                            privacyAccepted = true
                            currentTab = "splash"
                            SecurityInitRunner.init(context as Activity) {}
                        },
                        onReject = { (context as? Activity)?.finish() }
                    )
                }
                currentTab == "splash" -> {
                    SplashScreen(onReady = { currentTab = "dashboard" })
                }
                showPrivacy -> PrivacyScreen(onBack = { showPrivacy = false })
                showTerms -> TermsScreen(onBack = { showTerms = false })
                showAbout -> AboutScreen(
                    onPrivacyPolicy = { showPrivacy = true; showAbout = false },
                    onTerms = { showTerms = true; showAbout = false },
                    onBack = { showAbout = false }
                )
                showTutorial -> TutorialScreen(onBack = { showTutorial = false })
                currentTab == "dashboard" -> DashboardScreen(
                    recentProjects = emptyList(), allProjects = emptyList(),
                    onCreateProject = { videoPickerLauncher.launch(VideoPicker.getPickIntent()) },
                    onOpenProject = { currentTab = "editor" },
                    onOpenDraftBox = {}, onOpenTemplateCenter = {},
                    onOpenTutorial = { showTutorial = true },
                    onSearchProject = {}, onRenameProject = {},
                    onDeleteProject = {}, onDuplicateProject = {}
                )
                currentTab == "editor" -> EditorScreen(vm = editorVm)
                currentTab == "color" -> ColorScreen(onBack = { currentTab = "dashboard" })
                currentTab == "audio" -> PagePlaceholder("音频编辑")
                currentTab == "settings" -> SettingsScreen(
                    onOpenExportSettings = {}, onOpenAiModelManager = {},
                    onOpenPerformanceMonitor = {}, onOpenMemberCenter = {},
                    onOpenTutorial = { showTutorial = true }, onOpenLicenses = {},
                    onClearCache = {},
                    onOpenPrivacy = { showPrivacy = true },
                    onOpenTerms = { showTerms = true },
                    onOpenAbout = { showAbout = true }
                )
            }
        }
        if (!showTutorial && !showPrivacy && !showTerms && !showAbout && currentTab != "splash") {
            BottomNavBar(currentTab) { currentTab = it }
        }
    }
}

@Composable
private fun BottomNavBar(currentTab: String, onTabChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(60.dp).background(AppColors.BgSurface),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically) {
        val tabs = listOf("dashboard" to "首页", "editor" to "剪辑", "color" to "调色", "audio" to "音频", "settings" to "设置")
        tabs.forEach { (route, label) ->
            val isSelected = currentTab == route
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable { onTabChange(route) }.padding(vertical = 8.dp)) {
                Text(label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) AppColors.Accent else AppColors.TextTertiary)
            }
        }
    }
}

@Composable
private fun SplashScreen(onReady: () -> Unit) {
    val context = LocalContext.current
    val splashVm = remember { SplashViewModel(context.applicationContext as android.app.Application) }

    LaunchedEffect(splashVm.state) {
        if (splashVm.state == SplashViewModel.SplashState.READY) {
            kotlinx.coroutines.delay(800)
            onReady()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NexClip", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("AI视频编辑器", fontSize = 14.sp, color = Color(0xFF888888))
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = Color(0xFF4CAF50), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(splashVm.statusText, fontSize = 12.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
            splashVm.deviceInfo?.let { info ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("设备: ${info.tier.label} | RAM: ${info.ramMb}MB | ${info.cpuCores}核",
                    fontSize = 10.sp, color = Color(0xFF666666))
            }
            splashVm.errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(16.dp))
                Text("错误: $err", fontSize = 11.sp, color = Color(0xFFE85050))
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { splashVm.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("重试", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun PrivacyAgreeScreen(onAccept: () -> Unit, onReject: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("用户协议和隐私政策", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Text("欢迎使用NexClip！\n\n我们非常重视您的个人信息保护。在使用前，请您阅读并同意我们的《用户协议》和《隐私政策》。\n\n我们仅收集必要的设备信息和使用数据，用于提供和改进服务。",
            fontSize = 14.sp, color = Color(0xFFAAAAAA), textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
            Text("同意并继续", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onReject) {
            Text("拒绝并退出", fontSize = 14.sp, color = Color(0xFF888888))
        }
    }
}

@Composable
private fun PagePlaceholder(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, color = AppColors.TextPrimary, fontSize = 20.sp)
    }
}
