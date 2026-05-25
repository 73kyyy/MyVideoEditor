package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.TrackClip

class ClipGroupManager {
    data class ClipGroup(val id: String, val clipIds: MutableSet<String> = mutableSetOf())
    private val groups = mutableMapOf<String, ClipGroup>()

    fun createGroup(id: String, clipIds: List<String>): ClipGroup {
        val group = ClipGroup(id, clipIds.toMutableSet())
        groups[id] = group; return group
    }

    fun addToGroup(groupId: String, clipId: String) { groups[groupId]?.clipIds?.add(clipId) }
    fun removeFromGroup(groupId: String, clipId: String) { groups[groupId]?.clipIds?.remove(clipId) }
    fun dissolveGroup(groupId: String) { groups.remove(groupId) }
    fun getGroup(groupId: String): ClipGroup? = groups[groupId]
    fun getGroupForClip(clipId: String): ClipGroup? = groups.values.find { it.clipIds.contains(clipId) }
    fun isInGroup(clipId: String): Boolean = groups.values.any { it.clipIds.contains(clipId) }
    fun getGroupMembers(clipId: String): Set<String> = getGroupForClip(clipId)?.clipIds ?: setOf(clipId)
    fun getAllGroups(): List<ClipGroup> = groups.values.toList()
}
