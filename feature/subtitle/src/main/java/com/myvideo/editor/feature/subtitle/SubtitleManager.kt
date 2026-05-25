package com.myvideo.editor.feature.subtitle

class SubtitleManager {
    private val subtitles = mutableListOf<SubtitleItem>()

    fun add(item: SubtitleItem) { subtitles.add(item); subtitles.sortBy { it.startMs } }
    fun remove(id: String) { subtitles.removeAll { it.id == id } }
    fun update(id: String, text: String) {
        val idx = subtitles.indexOfFirst { it.id == id }
        if (idx >= 0) subtitles[idx] = subtitles[idx].copy(text = text)
    }
    fun updateStyle(id: String, style: SubtitleStyle) {
        val idx = subtitles.indexOfFirst { it.id == id }
        if (idx >= 0) subtitles[idx] = subtitles[idx].copy(style = style)
    }
    fun move(id: String, startMs: Long, endMs: Long) {
        val idx = subtitles.indexOfFirst { it.id == id }
        if (idx >= 0) subtitles[idx] = subtitles[idx].copy(startMs = startMs, endMs = endMs)
        subtitles.sortBy { it.startMs }
    }
    fun getAll(): List<SubtitleItem> = subtitles.toList()
    fun getAt(timeMs: Long): SubtitleItem? = subtitles.find { timeMs in it.startMs..it.endMs }
    fun getInRange(startMs: Long, endMs: Long): List<SubtitleItem> = subtitles.filter { it.startMs < endMs && it.endMs > startMs }
    fun clear() { subtitles.clear() }
    fun getCount(): Int = subtitles.size
}
