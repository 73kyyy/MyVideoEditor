package com.myvideo.editor.feature.project

import android.content.Context
import java.util.Timer
import java.util.TimerTask

class AutoSaveManager(private val context: Context, private val projectManager: ProjectManager) {
    private var timer: Timer? = null
    var intervalMs = 30_000L
    var onSaved: (() -> Unit)? = null

    fun start() {
        timer = Timer().apply { scheduleAtFixedRate(object : TimerTask() { override fun run() { save() } }, intervalMs, intervalMs) }
    }
    fun stop() { timer?.cancel(); timer = null }
    fun save() { projectManager.save(); onSaved?.invoke() }
    fun setInterval(ms: Long) { intervalMs = ms; stop(); start() }
}
