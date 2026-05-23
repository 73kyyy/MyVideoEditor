package com.myvideo.editor.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.myvideo.editor.ui.editor.ClipData
import com.myvideo.editor.ui.editor.ClipType
import com.myvideo.editor.ui.editor.EditorViewModel

/**
 * NexClip 视频导入管理器
 * 连接UI和VideoEngine
 */
class VideoImportManager(private val context: Context) {

    private val engine = VideoEngine(context)

    // 已导入的视频信息
    val importedVideos = mutableListOf<VideoEngine.VideoInfo>()

    /**
     * 从URI导入视频到编辑器
     */
    fun importToEditor(uri: Uri, vm: EditorViewModel): Boolean {
        val info = engine.getVideoInfo(uri) ?: return false
        importedVideos.add(info)

        // 计算片段宽度（基于时长和像素/秒）
        val durationSec = info.durationMs / 1000f
        val clipWidth = durationSec * vm.pixelsPerSecond

        // 找到最后一个片段的右边缘
        val lastRight = vm.clips.maxOfOrNull { it.leftPx + it.widthPx } ?: vm.rulerStartPx
        val gap = 10f

        // 创建新片段
        val clip = ClipData(
            id = "clip_${System.currentTimeMillis()}",
            name = uri.lastPathSegment ?: "video.mp4",
            leftPx = lastRight + gap,
            widthPx = clipWidth,
            trackIndex = vm.selectedTrackIndex,
            type = ClipType.Video
        )

        vm.clips.add(clip)
        return true
    }

    /**
     * 获取视频缩略图
     */
    fun getThumbnails(uri: Uri, count: Int = 10) = engine.extractThumbnails(uri, count)

    /**
     * 获取视频信息
     */
    fun getInfo(uri: Uri) = engine.getVideoInfo(uri)
}
