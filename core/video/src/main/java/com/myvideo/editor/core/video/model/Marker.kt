package com.myvideo.editor.core.video.model

data class Marker(
    val id: String, val timeMs: Long, val label: String = "",
    val color: Long = 0xFF4A90D9, val type: MarkerType = MarkerType.General
)

enum class MarkerType { General, Chapter, Edit, Todo, In, Out }

class MarkerManager {
    private val markers = mutableListOf<Marker>()
    fun add(m: Marker) { markers.add(m); markers.sortBy { it.timeMs } }
    fun remove(id: String) { markers.removeAll { it.id == id } }
    fun getAll(): List<Marker> = markers.toList()
    fun getAt(timeMs: Long, tol: Long = 500): Marker? =
        markers.minByOrNull { kotlin.math.abs(it.timeMs - timeMs) }?.takeIf { kotlin.math.abs(it.timeMs - timeMs) <= tol }
    fun clear() { markers.clear() }
    fun move(id: String, t: Long) { val i = markers.indexOfFirst { it.id == id }; if (i >= 0) markers[i] = markers[i].copy(timeMs = t); markers.sortBy { it.timeMs } }
}
