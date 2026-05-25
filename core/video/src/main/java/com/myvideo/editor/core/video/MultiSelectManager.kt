package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.TrackClip

class MultiSelectManager {
    private val selectedIds = mutableSetOf<String>()
    private var anchorId: String? = null

    fun select(clipId: String) { selectedIds.clear(); selectedIds.add(clipId); anchorId = clipId }
    fun addToSelection(clipId: String) { selectedIds.add(clipId); anchorId = clipId }
    fun toggleSelection(clipId: String) { if (selectedIds.contains(clipId)) selectedIds.remove(clipId) else { selectedIds.add(clipId); anchorId = clipId } }
    fun selectRange(fromId: String, toId: String, allClips: List<TrackClip>) {
        val fromIdx = allClips.indexOfFirst { it.id == fromId }
        val toIdx = allClips.indexOfFirst { it.id == toId }
        if (fromIdx >= 0 && toIdx >= 0) {
            val range = if (fromIdx <= toIdx) allClips.subList(fromIdx, toIdx + 1) else allClips.subList(toIdx, fromIdx + 1)
            selectedIds.clear(); selectedIds.addAll(range.map { it.id })
        }
    }
    fun selectAll(ids: List<String>) { selectedIds.clear(); selectedIds.addAll(ids) }
    fun deselectAll() { selectedIds.clear(); anchorId = null }
    fun isSelected(clipId: String): Boolean = selectedIds.contains(clipId)
    fun getSelectedIds(): Set<String> = selectedIds.toSet()
    fun getAnchor(): String? = anchorId
    fun getCount(): Int = selectedIds.size
}
