package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.ParametricTransition
import com.myvideo.editor.core.video.model.TransitionType

class TransitionEngine(private val executor: FFmpegExecutor) {

    fun apply(clip1: String, clip2: String, output: String,
              transition: ParametricTransition, cb: FFmpegExecutor.Callback) {
        val dur = transition.durationMs / 1000f
        val xfade = when (transition.type) {
            TransitionType.CrossFade -> "fade"
            TransitionType.SlideLeft -> "slideleft"
            TransitionType.SlideRight -> "slideright"
            TransitionType.SlideUp -> "slideup"
            TransitionType.SlideDown -> "slidedown"
            TransitionType.WipeLeft -> "wipeleft"
            TransitionType.WipeRight -> "wiperight"
            TransitionType.WipeUp -> "wipeup"
            TransitionType.WipeDown -> "wipedown"
            TransitionType.ZoomIn -> "zoomin"
            TransitionType.ZoomOut -> "zoomout"
            TransitionType.CircleOpen -> "circleopen"
            TransitionType.CircleClose -> "circleclose"
            TransitionType.Dissolve -> "dissolve"
            TransitionType.FlashWhite -> "fadeblack"
            TransitionType.FlashBlack -> "fadeblack"
            TransitionType.Blur -> "fadeblur"
            else -> "fade"
        }
        executor.execute("-i $clip1 -i $clip2 -filter_complex \"[0:v][1:v]xfade=transition=$xfade:duration=$dur:offset=0\" -c:v libx264 -preset fast -y $output", cb)
    }
}
