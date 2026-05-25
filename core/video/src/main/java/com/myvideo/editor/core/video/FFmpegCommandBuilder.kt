package com.myvideo.editor.core.video

class FFmpegCommandBuilder {
    private val parts = mutableListOf<String>()
    private val filters = mutableListOf<String>()

    fun input(path: String) = apply { parts.addAll(listOf("-i", path)) }
    fun output(path: String) = apply { parts.add(path) }
    fun overwrite() = apply { parts.add("-y") }
    fun videoCodec(codec: String) = apply { parts.addAll(listOf("-c:v", codec)) }
    fun audioCodec(codec: String) = apply { parts.addAll(listOf("-c:a", codec)) }
    fun bitrate(rate: String) = apply { parts.addAll(listOf("-b:v", rate)) }
    fun audioBitrate(rate: String) = apply { parts.addAll(listOf("-b:a", rate)) }
    fun preset(p: String) = apply { parts.addAll(listOf("-preset", p)) }
    fun crf(v: Int) = apply { parts.addAll(listOf("-crf", v.toString())) }
    fun fps(f: Int) = apply { filters.add("fps=$f") }
    fun scale(w: Int, h: Int) = apply { filters.add("scale=$w:$h:force_original_aspect_ratio=decrease,pad=$w:$h:(ow-iw)/2:(oh-ih)/2") }
    fun filter(vf: String) = apply { filters.add(vf) }
    fun ss(time: Float) = apply { parts.addAll(listOf("-ss", time.toString())) }
    fun t(time: Float) = apply { parts.addAll(listOf("-t", time.toString())) }
    fun movFlags() = apply { parts.addAll(listOf("-movflags", "+faststart")) }
    fun extra(vararg args: String) = apply { parts.addAll(args) }

    fun build(): String {
        val cmd = mutableListOf("ffmpeg")
        val inputArgs = parts.filterIndexed { i, _ -> i < parts.indexOfFirst { it == "-ss" || !listOf("-i").contains(parts.getOrNull(i-1)) } }
        val filterStr = if (filters.isNotEmpty()) "-vf,${filters.joinToString(",")}" else ""
        cmd.addAll(parts)
        if (filterStr.isNotEmpty()) {
            val insertIdx = cmd.indexOfLast { it == "-i" } + 2
            cmd.addAll(insertIdx, listOf("-vf", filters.joinToString(",")))
        }
        return cmd.joinToString(" ")
    }

    fun reset() { parts.clear(); filters.clear() }
}
