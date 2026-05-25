package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.TimeRemapKeyframe
import com.myvideo.editor.core.video.model.TimeRemapper

class TimeRemapEngine {
    private val remappers = mutableMapOf<String, TimeRemapper>()

    fun createRemapper(clipId: String) { remappers[clipId] = TimeRemapper() }
    fun addKeyframe(clipId: String, compMs: Long, srcMs: Long) {
        remappers.getOrPut(clipId) { TimeRemapper() }.add(TimeRemapKeyframe(compMs, srcMs))
    }
    fun removeKeyframe(clipId: String, compMs: Long) { remappers[clipId]?.remove(compMs) }
    fun getSourceTime(clipId: String, compMs: Long): Long = remappers[clipId]?.getSourceTime(compMs) ?: compMs
    fun getKeyframes(clipId: String): List<TimeRemapKeyframe> = remappers[clipId]?.getKeyframes() ?: emptyList()
    fun hasRemap(clipId: String): Boolean = remappers.containsKey(clipId) && (remappers[clipId]?.getKeyframes()?.size ?: 0) > 1
    fun clear(clipId: String) { remappers.remove(clipId) }
}
