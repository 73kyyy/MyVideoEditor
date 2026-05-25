package com.myvideo.editor.core.vision

class OpenCVManager {
    var isInitialized = false; private set
    external fun nativeInit(): Boolean
    external fun nativeRelease()

    fun init(): Boolean {
        return try { isInitialized = nativeInit(); isInitialized }
        catch (e: Exception) { false }
    }

    fun release() { if (isInitialized) { nativeRelease(); isInitialized = false } }
}
