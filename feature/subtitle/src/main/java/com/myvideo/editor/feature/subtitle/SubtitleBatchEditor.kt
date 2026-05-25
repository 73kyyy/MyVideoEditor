package com.myvideo.editor.feature.subtitle

class SubtitleBatchEditor(private val manager: SubtitleManager) {

    data class BatchResult(val success: Int, val failed: Int)

    fun shiftAll(offsetMs: Long) {
        manager.getAll().forEach { item ->
            manager.move(item.id, item.startMs + offsetMs, item.endMs + offsetMs)
        }
    }

    fun scaleTimings(factor: Float) {
        manager.getAll().forEach { item ->
            manager.move(item.id, (item.startMs * factor).toLong(), (item.endMs * factor).toLong())
        }
    }

    fun updateAllStyle(style: SubtitleStyle) {
        manager.getAll().forEach { manager.updateStyle(it.id, style) }
    }

    fun findAndReplace(find: String, replace: String): BatchResult {
        var success = 0
        manager.getAll().forEach { item ->
            if (item.text.contains(find)) {
                manager.update(item.id, item.text.replace(find, replace))
                success++
            }
        }
        return BatchResult(success, 0)
    }

    fun autoSplit(maxChars: Int = 20): Int {
        var splitCount = 0
        manager.getAll().toList().forEach { item ->
            if (item.text.length > maxChars) {
                val mid = item.text.length / 2
                val spaceIdx = item.text.indexOf(' ', mid).takeIf { it > 0 } ?: mid
                val midMs = (item.startMs + item.endMs) / 2
                manager.remove(item.id)
                manager.add(SubtitleItem("${item.id}_a", item.text.substring(0, spaceIdx).trim(), item.startMs, midMs, item.style, item.position))
                manager.add(SubtitleItem("${item.id}_b", item.text.substring(spaceIdx).trim(), midMs, item.endMs, item.style, item.position))
                splitCount++
            }
        }
        return splitCount
    }

    fun adjustDuration(minMs: Long = 1000, maxMs: Long = 5000) {
        manager.getAll().forEach { item ->
            val duration = item.endMs - item.startMs
            if (duration < minMs) manager.move(item.id, item.startMs, item.startMs + minMs)
            else if (duration > maxMs) manager.move(item.id, item.startMs, item.startMs + maxMs)
        }
    }
}
