package com.myvideo.editor.core.common.constants

object AppConstants {
    const val APP_NAME = "NexClip"
    const val APP_VERSION = "1.0.0"
    const val DATABASE_NAME = "nexclip.db"
    const val PREFS_NAME = "nexclip_prefs"
    const val CACHE_DIR = "nexclip_cache"
    const val EXPORT_DIR = "NexClip"
    const val MAX_VIDEO_DURATION_MS = 600_000L
    const val MAX_EXPORT_WIDTH = 3840
    const val MAX_EXPORT_HEIGHT = 2160
    const val DEFAULT_FPS = 30
    const val MAX_TRACKS = 10
    const val MAX_LAYERS = 5
    const val AUTO_SAVE_INTERVAL_MS = 30_000L
    const val THUMBNAIL_WIDTH = 128
    const val THUMBNAIL_HEIGHT = 72
    const val MAX_UNDO_STEPS = 50
    const val SAMPLE_RATE = 44100
    const val AUDIO_CHANNELS = 2
}

object FeatureFlags {
    const val AI_ENABLED = true
    const val CLOUD_SYNC = false
    const val COLLAB_EDIT = false
    const val ADVANCED_COLOR = true
    const val STABILIZATION = true
    const val AUTO_SUBTITLE = true
}
