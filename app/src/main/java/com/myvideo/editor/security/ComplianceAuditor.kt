package com.myvideo.editor.security

import android.content.Context

object ComplianceAuditor {
    private const val PREFS_NAME = "nexclip_compliance"
    private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"

    fun isPrivacyAccepted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PRIVACY_ACCEPTED, false)
    }

    fun setPrivacyAccepted(context: Context, accepted: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_PRIVACY_ACCEPTED, accepted).apply()
    }
}
