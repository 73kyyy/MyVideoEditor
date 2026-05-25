package com.myvideo.editor.feature.effects.transition

class TransitionManager {
    private val transitions = mutableMapOf<String, TransitionConfig>()

    fun setTransition(clipId: String, config: TransitionConfig) { transitions[clipId] = config }
    fun getTransition(clipId: String): TransitionConfig? = transitions[clipId]
    fun removeTransition(clipId: String) { transitions.remove(clipId) }
    fun clear() { transitions.clear() }
    fun getFfmpegCommand(clip1: String, clip2: String, clipId: String, output: String): String? {
        val config = transitions[clipId] ?: return null
        val dur = config.durationMs / 1000f
        return "-i $clip1 -i $clip2 -filter_complex \"[0:v][1:v]xfade=transition=${config.type.ffmpegXfade}:duration=$dur:offset=0\" -c:v libx264 -preset fast -y $output"
    }
}
