package com.myvideo.editor.core.security

import android.content.Context
import java.io.File

class SecurityChecker(private val context: Context) {

    external fun nativeCheckRoot(): Boolean
    external fun nativeCheckEmulator(): Boolean
    external fun nativeCheckDebugger(): Boolean
    external fun nativeCheckHook(): Boolean
    external fun nativeCheckTamper(): Boolean

    fun isRooted(): Boolean {
        val paths = arrayOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/app/Superuser.apk")
        if (paths.any { File(it).exists() }) return true
        return try { nativeCheckRoot() } catch (e: Exception) { false }
    }

    fun isEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT
        if (fingerprint.contains("generic") || fingerprint.contains("sdk")) return true
        return try { nativeCheckEmulator() } catch (e: Exception) { false }
    }

    fun isDebuggerAttached(): Boolean {
        return try { android.os.Debug.isDebuggerConnected() || nativeCheckDebugger() }
        catch (e: Exception) { false }
    }

    fun isHooked(): Boolean {
        return try { nativeCheckHook() } catch (e: Exception) { false }
    }

    fun isTampered(): Boolean {
        return try { nativeCheckTamper() } catch (e: Exception) { false }
    }
}
