package com.myvideo.editor.core.common.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardHelper {
    data class ClipItem(val type: String, val data: String, val timestamp: Long = System.currentTimeMillis())

    private var internalClipboard: ClipItem? = null

    fun copy(context: Context, type: String, data: String) {
        internalClipboard = ClipItem(type, data)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NexClip", data))
    }

    fun paste(): ClipItem? = internalClipboard

    fun hasContent(): Boolean = internalClipboard != null

    fun clear() { internalClipboard = null }

    fun getType(): String? = internalClipboard?.type
}
