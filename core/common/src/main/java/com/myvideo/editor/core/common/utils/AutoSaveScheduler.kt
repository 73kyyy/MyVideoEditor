package com.myvideo.editor.core.common.utils

import android.content.Context
import java.util.Timer
import java.util.TimerTask

class AutoSaveScheduler(private val context: Context) {
    private var timer: Timer? = null
    var intervalMs: Long = 30_000L
    var onSave: (() -> Unit)? = null
    private var isRunning = false

    fun start() {
        if (isRunning) return
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() { onSave?.invoke() }
            }, intervalMs, intervalMs)
        }
        isRunning = true
    }

    fun stop() { timer?.cancel(); timer = null; isRunning = false }
    fun setInterval(ms: Long) { intervalMs = ms; if (isRunning) { stop(); start() } }
    fun isRunning(): Boolean = isRunning
    fun saveNow() { onSave?.invoke() }
}
