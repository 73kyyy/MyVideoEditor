package com.myvideo.editor.engine

import android.content.Context
import com.myvideo.editor.core.ai.AIIntegrationBridge
import com.myvideo.editor.core.security.membership.MembershipValidator
import com.myvideo.editor.ui.editor.AIFeatureUIHelper
import com.myvideo.editor.ui.editor.EditorViewModel
import java.io.File

class EditorBridge(private val context: Context) {

    private val renderEngine = FFmpegRenderEngine(context)
    private val filterEngine = FFmpegFilterEngine(renderEngine)
    private val audioEngine = FFmpegAudioEngine(renderEngine)
    private val exportEngine = FFmpegExportEngine(context, renderEngine)
    private val aiBridge = AIIntegrationBridge(context)
    private val validator = MembershipValidator()
    private val aiHelper = AIFeatureUIHelper(context)

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

    // ===== AI功能（3次免费+录屏检测+联网验证）=====
    private fun checkAI(vm: EditorViewModel, featureId: String, isOnline: Boolean): Boolean {
        aiHelper.checkScreenState(context)
        val error = aiHelper.checkAIAccess(featureId, isOnline)
        if (error != null) { vm.showToast(error); return false }
        return true
    }

    fun aiSegment(vm: EditorViewModel, isOnline: Boolean,
                  onComplete: (String) -> Unit, onError: (String) -> Unit) {
        if (!checkAI(vm, "segment", isOnline)) return
        aiHelper.recordAIUsage("segment")
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("ai_segment_${System.currentTimeMillis()}.mp4")
        val result = aiBridge.segment(android.graphics.BitmapFactory.decodeFile(input), isOnline)
        if (result.success) onComplete(output) else onError(result.errorMessage ?: "AI抠图失败")
    }

    fun aiSuperRes(vm: EditorViewModel, isOnline: Boolean,
                   onComplete: (String) -> Unit, onError: (String) -> Unit) {
        if (!checkAI(vm, "superres", isOnline)) return
        aiHelper.recordAIUsage("superres")
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("ai_superres_${System.currentTimeMillis()}.mp4")
        val result = aiBridge.superResolution(android.graphics.BitmapFactory.decodeFile(input), isOnline)
        if (result.success) onComplete(output) else onError(result.errorMessage ?: "AI超分失败")
    }

    fun aiInterpolate(vm: EditorViewModel, isOnline: Boolean,
                      onComplete: (String) -> Unit, onError: (String) -> Unit) {
        if (!checkAI(vm, "interpolate", isOnline)) return
        aiHelper.recordAIUsage("interpolate")
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("ai_interp_${System.currentTimeMillis()}.mp4")
        val bmp = android.graphics.BitmapFactory.decodeFile(input)
        val result = aiBridge.interpolate(bmp, bmp, isOnline)
        if (result.success) onComplete(output) else onError(result.errorMessage ?: "AI插帧失败")
    }

    fun aiWhisper(vm: EditorViewModel, isOnline: Boolean,
                  onComplete: (String) -> Unit, onError: (String) -> Unit) {
        if (!checkAI(vm, "whisper", isOnline)) return
        aiHelper.recordAIUsage("whisper")
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val result = aiBridge.whisperTranscribe(FloatArray(0), isOnline)
        if (result.success) onComplete("语音识别完成") else onError(result.errorMessage ?: "语音识别失败")
    }

    fun aiDenoise(vm: EditorViewModel, isOnline: Boolean,
                  onComplete: (String) -> Unit, onError: (String) -> Unit) {
        if (!checkAI(vm, "denoise", isOnline)) return
        aiHelper.recordAIUsage("denoise")
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("ai_denoise_${System.currentTimeMillis()}.mp4")
        val result = aiBridge.denoise(FloatArray(0), isOnline)
        if (result.success) onComplete(output) else onError(result.errorMessage ?: "AI降噪失败")
    }

    fun aiSeparate(vm: EditorViewModel, isOnline: Boolean,
                   onComplete: (String) -> Unit, onError: (String) -> Unit) {
        if (!checkAI(vm, "separate", isOnline)) return
        aiHelper.recordAIUsage("separate")
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val result = aiBridge.separate(FloatArray(0), isOnline)
        if (result.success) onComplete("人声分离完成") else onError(result.errorMessage ?: "人声分离失败")
    }

    // ===== 导出（检查AI使用权限）=====
    fun export(vm: EditorViewModel, width: Int, height: Int, fps: Int, bitrate: String,
               onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val permissionError = aiHelper.checkExportPermission()
        if (permissionError != null) return onError(permissionError)
        val clip = vm.clips.firstOrNull() ?: return onError("没有片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("export_${System.currentTimeMillis()}.mp4")
        vm.isExporting = true; vm.exportProgress = 0f; vm.exportError = null
        renderEngine.run("-i $input -c:v libx264 -preset fast -c:a copy -y $output", object : FFmpegRenderEngine.RenderCallback {
            override fun onProgress(p: Float) { vm.exportProgress = p }
            override fun onComplete(o: String) { vm.isExporting = false; vm.exportDone = true; onComplete(output) }
            override fun onError(e: String) { vm.isExporting = false; vm.exportError = e; onError(e) }
            override fun onLog(m: String) {}
        })
    }

    // ===== 传统功能（无限制）=====
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

    fun applySpeed(vm: EditorViewModel, speed: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("speed_${System.currentTimeMillis()}.mp4")
        val speedStr = "%.1f".format(speed)
        renderEngine.run("-i $input -filter_complex "[0:v]setpts=PTS/$speedStr[v];[0:a]atempo=$speedStr[a]" -map "[v]" -map "[a]" -c:v libx264 -preset fast -y $output", makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyReverse(vm: EditorViewModel, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("reverse_${System.currentTimeMillis()}.mp4")
        renderEngine.run("-i $input -vf reverse -af areverse -c:v libx264 -preset fast -y $output", makeCallback(vm, clip.id, output, onComplete, onError))
    }

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

    fun addTextOverlay(vm: EditorViewModel, text: String, fontSize: Int, color: String, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("text_${System.currentTimeMillis()}.mp4")
        val safe = text.replace("'", "").replace(":", " ")
        renderEngine.run("-i $input -vf \"drawtext=text='$safe':fontsize=$fontSize:fontcolor=$color:x=(w-text_w)/2:y=h-th-20\" -c:v libx264 -preset fast -c:a copy -y $output",
            makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyChromaKey(vm: EditorViewModel, color: String, similarity: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("chroma_${System.currentTimeMillis()}.mp4")
        renderEngine.run("-i $input -vf \"chromakey=$color:$similarity:0.1\" -c:v libx264 -preset fast -c:a copy -y $output",
            makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyMotionBlur(vm: EditorViewModel, strength: Float, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val output = getOutputPath("mblur_${System.currentTimeMillis()}.mp4")
        val frames = (strength / 100f * 5).toInt().coerceIn(1, 5)
        renderEngine.run("-i $input -vf \"tmix=frames=$frames:weights='1'\" -c:v libx264 -preset fast -c:a copy -y $output",
            makeCallback(vm, clip.id, output, onComplete, onError))
    }

    fun applyStabilize(vm: EditorViewModel, smoothing: Int, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip = vm.selectedClip() ?: return onError("请先选择片段")
        val input = getClipPath(vm, clip.id) ?: return onError("找不到视频文件")
        val stabFile = getOutputPath("stab_${System.currentTimeMillis()}.txt")
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

    fun applyTransition(vm: EditorViewModel, type: String, durationMs: Long, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val clip1 = vm.clips.getOrNull(0) ?: return onError("需要至少两个片段")
        val clip2 = vm.clips.getOrNull(1) ?: return onError("需要至少两个片段")
        val path1 = getClipPath(vm, clip1.id) ?: return onError("找不到第一个视频")
        val path2 = getClipPath(vm, clip2.id) ?: return onError("找不到第二个视频")
        val output = getOutputPath("transition_${System.currentTimeMillis()}.mp4")
        val tt = com.myvideo.editor.core.video.model.TransitionType.values().find { it.name.contains(type, true) }
            ?: com.myvideo.editor.core.video.model.TransitionType.CrossFade
        val config = com.myvideo.editor.core.video.model.ParametricTransition("t1", type, tt, durationMs)
        renderEngine.run("-i $path1 -i $path2 -filter_complex xfade=transition=${tt.ffmpegXfade}:duration=${durationMs/1000.0} -c:v libx264 -preset fast -c:a copy -y $output", makeCallback(vm, null, output, onComplete, onError))
    }

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

    fun getVideoInfo(path: String): String? = renderEngine.getMediaInfo(path)
    fun getAIHelper() = aiHelper
    fun release() { renderEngine.release(); aiBridge.release(); aiHelper.release() }
}
