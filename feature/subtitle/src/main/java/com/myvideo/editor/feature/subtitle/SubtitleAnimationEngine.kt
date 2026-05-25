package com.myvideo.editor.feature.subtitle

class SubtitleAnimationEngine {

    enum class AnimationType { None, FadeIn, FadeOut, Typewriter, SlideUp, SlideDown, SlideLeft, SlideRight, Scale, Bounce }

    data class AnimationConfig(val type: AnimationType, val durationMs: Long = 300, val delayMs: Long = 0)

    fun getOpacity(item: SubtitleItem, timeMs: Long, animIn: AnimationConfig, animOut: AnimationConfig): Float {
        val fadeInEnd = item.startMs + animIn.durationMs
        val fadeOutStart = item.endMs - animOut.durationMs
        return when {
            timeMs < item.startMs || timeMs > item.endMs -> 0f
            timeMs in item.startMs..fadeInEnd && animIn.type == AnimationType.FadeIn -> ((timeMs - item.startMs).toFloat() / animIn.durationMs).coerceIn(0f, 1f)
            timeMs in fadeOutStart..item.endMs && animOut.type == AnimationType.FadeOut -> ((item.endMs - timeMs).toFloat() / animOut.durationMs).coerceIn(0f, 1f)
            else -> 1f
        }
    }

    fun getOffset(item: SubtitleItem, timeMs: Long, animIn: AnimationConfig): Pair<Float, Float> {
        if (animIn.type == AnimationType.None || timeMs > item.startMs + animIn.durationMs) return Pair(0f, 0f)
        val t = ((timeMs - item.startMs).toFloat() / animIn.durationMs).coerceIn(0f, 1f)
        return when (animIn.type) {
            AnimationType.SlideUp -> Pair(0f, 50f * (1f - t))
            AnimationType.SlideDown -> Pair(0f, -50f * (1f - t))
            AnimationType.SlideLeft -> Pair(100f * (1f - t), 0f)
            AnimationType.SlideRight -> Pair(-100f * (1f - t), 0f)
            else -> Pair(0f, 0f)
        }
    }

    fun getScale(item: SubtitleItem, timeMs: Long, animIn: AnimationConfig): Float {
        if (animIn.type != AnimationType.Scale || timeMs > item.startMs + animIn.durationMs) return 1f
        val t = ((timeMs - item.startMs).toFloat() / animIn.durationMs).coerceIn(0f, 1f)
        return 0.5f + 0.5f * t
    }

    fun getTypewriterProgress(item: SubtitleItem, timeMs: Long, animIn: AnimationConfig): Float {
        if (animIn.type != AnimationType.Typewriter) return 1f
        return ((timeMs - item.startMs).toFloat() / animIn.durationMs).coerceIn(0f, 1f)
    }
}
