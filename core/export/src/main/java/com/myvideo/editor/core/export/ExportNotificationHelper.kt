package com.myvideo.editor.core.export

import android.content.Context
import com.myvideo.editor.core.export.model.ExportStatus

class ExportNotificationHelper(private val context: Context) {
    private var notificationId = 1001

    fun createNotificationChannel() {
        // Android 8.0+ notification channel setup
    }

    fun showProgress(jobId: String, progress: Float, status: ExportStatus) {
        when (status) {
            ExportStatus.Preparing -> notify("正在准备导出...")
            ExportStatus.Encoding -> notify("导出中 ${(progress * 100).toInt()}%")
            ExportStatus.Muxing -> notify("正在合成视频...")
            ExportStatus.Completed -> notify("导出完成")
            ExportStatus.Failed -> notify("导出失败")
            ExportStatus.Cancelled -> notify("导出已取消")
            else -> {}
        }
    }

    private fun notify(message: String) {
        // Show Android notification with message
    }

    fun cancelNotification() {
        // Cancel the export notification
    }
}
