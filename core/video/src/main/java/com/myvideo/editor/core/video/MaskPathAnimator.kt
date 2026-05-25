package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.Keyframe
import com.myvideo.editor.core.video.model.KeyframeInterpolator
import com.myvideo.editor.core.video.model.MaskData
import com.myvideo.editor.core.video.model.MaskPoint

class MaskPathAnimator {

    fun getKeyframedMask(mask: MaskData, timeMs: Long): MaskData {
        val kfs = mask.points.mapIndexed { i, _ ->
            val px = mask.points.map { p ->
                Keyframe(timeMs, "x${i}", p.x)
            }
            val py = mask.points.map { p ->
                Keyframe(timeMs, "y${i}", p.y)
            }
            Pair(px, py)
        }
        val animatedPoints = mask.points.mapIndexed { i, point ->
            val xKfs = mask.points.indices.map { Keyframe(mask.points[it].let { timeMs }, "x$i", point.x) }
            val yKfs = mask.points.indices.map { Keyframe(mask.points[it].let { timeMs }, "y$i", point.y) }
            MaskPoint(
                x = KeyframeInterpolator.interpolateList(xKfs, timeMs),
                y = KeyframeInterpolator.interpolateList(yKfs, timeMs),
                handleInX = point.handleInX, handleInY = point.handleInY,
                handleOutX = point.handleOutX, handleOutY = point.handleOutY
            )
        }
        return mask.copy(points = animatedPoints)
    }

    fun animatePosition(mask: MaskData, fromMs: Long, toMs: Long, offsetX: Float, offsetY: Float, timeMs: Long): MaskData {
        if (timeMs <= fromMs) return mask
        val t = ((timeMs - fromMs).toFloat() / (toMs - fromMs)).coerceIn(0f, 1f)
        val movedPoints = mask.points.map { p ->
            p.copy(x = p.x + offsetX * t, y = p.y + offsetY * t)
        }
        return mask.copy(points = movedPoints)
    }
}
