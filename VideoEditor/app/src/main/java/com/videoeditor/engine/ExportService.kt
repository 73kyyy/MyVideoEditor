package com.videoeditor.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.videoeditor.data.model.ExportConfig
import com.videoeditor.data.model.Project
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class ExportService : Service() {

    companion object {
        const val CHANNEL_ID = "export_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_EXPORT = "action_export"
        const val ACTION_CANCEL = "action_cancel"
        const val EXTRA_PROJECT = "extra_project"
        const val EXTRA_CONFIG = "extra_config"
        const val EXTRA_OUTPUT_PATH = "extra_output_path"

        val exportProgress = MutableStateFlow(0f)
        val isExporting = MutableStateFlow(false)
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var ffmpegEngine: FFmpegEngine? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ffmpegEngine = FFmpegEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_EXPORT -> {
                val project = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_PROJECT, Project::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_PROJECT)
                }
                val config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_CONFIG, ExportConfig::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_CONFIG)
                }
                val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH)

                if (project != null && config != null && outputPath != null) {
                    startExport(project, config, outputPath)
                }
            }
            ACTION_CANCEL -> {
                ffmpegEngine?.cancelExport()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startExport(project: Project, config: ExportConfig, outputPath: String) {
        isExporting.value = true
        val notification = buildNotification(0)
        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            ffmpegEngine?.exportProject(project, config, outputPath)
                ?.onSuccess {
                    exportProgress.value = 1f
                    stopSelf()
                }
                ?.onFailure {
                    exportProgress.value = -1f
                    stopSelf()
                }
            isExporting.value = false
        }

        // Monitor progress
        scope.launch {
            ffmpegEngine?.exportProgress?.collect { progress ->
                if (progress in 0f..1f) {
                    updateNotification((progress * 100).toInt())
                }
            }
        }
    }

    private fun buildNotification(progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在导出视频")
            .setContentText("进度: $progress%")
            .setSmallIcon(android.R.drawable.ic_media_render)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(progress: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(progress))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "视频导出",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "视频导出进度通知"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        ffmpegEngine = null
    }
}
