package com.myvideo.editor.engine

import android.content.Context
import com.myvideo.editor.ui.editor.EditorViewModel
import java.io.File

class EditorBridge(private val context: Context) {

    private val renderEngine = FFmpegRenderEngine(context)
    private val filterEngine = FFmpegFilterEngine(renderEngine)
    private val speedEngine = FFmpegSpeedEngine(renderEngine)
    private val audioEngine = FFmpegAudioEngine(renderEngine)
    private val exportEngine = FFmpegExportEngine(context, renderEngine)

    private fun getClipPath(vm: EditorViewModel, clipId: String): String? {
        return vm.videoUris[clipId]?.replace("file://", "")
    }

    private fun getOutputPath(filename: String): String {
        val dir = File(context.cacheDir, "rendered"); dir.mkdirs()
        return File(dir, filename).absolutePath
    }

    private fun makeCallback(vm: EditorViewModel, clipId: String?, outputPath: String,
                             onComplete: (String) -> Unit, onError: (String) -> Unit) = object : FFmpegRenderEngine.RenderCallback {
        override fun onProgress(p: Float) { vm.exportProgress = p }
        override fun onComplete(o: String) { clipId?.let { vm.videoUris[it] = "file://$outputPath" }; onComplete(outputPath) }
        override fun onError(e: String) { onError(e) }
        override fun onLog(m: String) {}
    }

    // ===== 滤镜 =====
    fun applyFilter(vm: EditorViewModel, filterName: String, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("filter_${System.currentTimeMillis()}.mp4")
        filterEngine.apply(input, output, filterName, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyMultipleFilters(vm: EditorViewModel, filters: List<String>, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("multifilter_${System.currentTimeMillis()}.mp4")
        filterEngine.applyMultiple(input, output, filters, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyVignette(vm: EditorViewModel, strength: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("vignette_${System.currentTimeMillis()}.mp4")
        filterEngine.applyVignette(input, output, strength, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyFilmGrain(vm: EditorViewModel, grain: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("grain_${System.currentTimeMillis()}.mp4")
        filterEngine.applyFilmGrain(input, output, grain, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    // ===== 变速/倒放 =====
    fun applySpeed(vm: EditorViewModel, speed: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("speed_${System.currentTimeMillis()}.mp4")
        speedEngine.applySpeed(input, output, speed, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyReverse(vm: EditorViewModel, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("reverse_${System.currentTimeMillis()}.mp4")
        speedEngine.applyReverse(input, output, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    // ===== 转场 =====
    fun applyTransition(vm: EditorViewModel, type: String, durationMs: Long, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip1 = vm.clips.getOrNull(0) ?: return onError("需要至少两个片段")
        val clip2 = vm.clips.getOrNull(1) ?: return onError("需要至少两个片段")
        val path1 = getClipPath(vm, clip1.id) ?: return onError("找不到第一个视频")
        val path2 = getClipPath(vm, clip2.id) ?: return onError("找不到第二个视频")
        val output = getOutputPath("transition_${System.currentTimeMillis()}.mp4")
        val tt = com.myvideo.editor.core.video.model.TransitionType.values().find { it.name.contains(type, true) } ?: com.myvideo.editor.core.video.model.TransitionType.CrossFade
        val config = com.myvideo.editor.core.video.model.ParametricTransition("t1", type, tt, durationMs)
        com.myvideo.editor.core.video.TransitionEngine(renderEngine).apply(path1, path2, output, config, makeCallback(vm, null, output, onComplete, onError))
    }

    // ===== 音频 =====
    fun applyAudioDenoise(vm: EditorViewModel, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("denoise_${System.currentTimeMillis()}.mp4")
        audioEngine.applyDenoise(input, output, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyAudioVolume(vm: EditorViewModel, volume: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("volume_${System.currentTimeMillis()}.mp4")
        audioEngine.applyVolume(input, output, volume, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun addBgm(vm: EditorViewModel, bgmPath: String, bgmVolume: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("bgm_${System.currentTimeMillis()}.mp4")
        audioEngine.addBgm(input, bgmPath, output, bgmVolume, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    // ===== 文字叠加 =====
    fun addTextOverlay(vm: EditorViewModel, text: String, fontSize: Int, color: String, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("text_${System.currentTimeMillis()}.mp4")
        val safe = text.replace("'", "").replace(":", " ")
        renderEngine.run("-i $input -vf \"drawtext=text='$safe':fontsize=$fontSize:fontcolor=$color:x=(w-text_w)/2:y=h-th-20\" -c:v libx264 -preset fast -c:a copy -y $output",
            makeCallback(vm, clip.id, output, onComplete, onError))
    }

    // ===== 绿幕抠像 =====
    fun applyChromaKey(vm: EditorViewModel, color: String, similarity: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("chroma_${System.currentTimeMillis()}.mp4")
        renderEngine.run("-i $input -vf \"chromakey=$color:$similarity:0.1\" -c:v libx264 -preset fast -c:a copy -y $output",
            makeCallback(vm, clip.id, output, onComplete, onError))
    }

    // ===== 动态模糊 =====
    fun applyMotionBlur(vm: EditorViewModel, strength: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("mblur_${System.currentTimeMillis()}.mp4")
        val frames = (strength / 100f * 5).toInt().coerceIn(1, 5)
        renderEngine.run("-i $input -vf \"tmix=frames=$frames:weights='1'\" -c:v libx264 -preset fast -c:a copy -y $output",
            makeCallback(vm, clip.id, output, onComplete, onError))
    }

    // ===== 视频稳定 =====
    fun applyStabilize(vm: EditorViewModel, smoothing: Int, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val stabFile = getOutputPath("stab_data_${System.currentTimeMillis()}.txt")
        val output = getOutputPath("stabilized_${System.currentTimeMillis()}.mp4")
        renderEngine.run("-i $input -vf \"vidstabdetect=shakiness=5:accuracy=15:result=$stabFile\" -f null -",
            object : FFmpegRenderEngine.RenderCallback {
                override fun onProgress(p: Float) {}
                override fun onComplete(o: String) {
                    renderEngine.run("-i $input -vf \"vidstabtransform=input=$stabFile:smoothing=$smoothing:zoom=1\" -c:v libx264 -preset fast -c:a copy -y $output",
                        makeCallback(vm, clip.id, output, onComplete, onError))
                }
                override fun onError(e: String) { onError(e) }
                override fun onLog(m: String) {}
            })
    }

    // ===== 剪切/拼接 =====
    fun trimClip(vm: EditorViewModel, startMs: Long, endMs: Long, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("trim_${System.currentTimeMillis()}.mp4")
        renderEngine.trim(input, output, startMs, endMs, makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun concatClips(vm: EditorViewModel, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val paths = vm.clips.mapNotNull { getClipPath(vm, it.id) }
        if (paths.size < 2) return onError("需要至少两个片段")
        val output = getOutputPath("concat_${System.currentTimeMillis()}.mp4")
        renderEngine.concat(paths, output, makeCallback(vm, null, output, onComplete, onError))
    }

    // ===== 导出 =====
    fun export(vm: EditorViewModel, width: Int, height: Int, fps: Int, bitrate: String,
               onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.clips.firstOrNull() ?: return onError("没有片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("export_${System.currentTimeMillis()}.mp4")
        vm.isExporting = true; vm.exportProgress = 0f; vm.exportError = null
        exportEngine.exportWithProfile(input, output, "1080p", "", object : FFmpegRenderEngine.RenderCallback {
            override fun onProgress(p: Float) { vm.exportProgress = p }
            override fun onComplete(o: String) { vm.isExporting = false; vm.exportDone = true; onComplete(outputPath) }
            override fun onError(e: String) { vm.isExporting = false; vm.exportError = e; onError(e) }
            override fun onLog(m: String) {}
        })
    }

    fun getVideoInfo(path: String): String? = renderEngine.getMediaInfo(path)
    fun release() { renderEngine.release() }
}
