package com.myvideo.editor.core.video.color.model

data class ColorGradingNode(
    val id: String,
    var x: Float = 0.5f,
    var y: Float = 0.5f,
    var curveType: CurveType = CurveType.RGB,
    val points: MutableList<CurvePoint> = mutableListOf()
)

data class CurvePoint(var x: Float, var y: Float)

enum class CurveType { RGB, Red, Green, Blue, Hue, Saturation, Luminance }

class ColorGradingNodeGraph {
    private val nodes = mutableMapOf<String, ColorGradingNode>()
    fun addNode(node: ColorGradingNode) { nodes[node.id] = node }
    fun removeNode(id: String) { nodes.remove(id) }
    fun getNode(id: String): ColorGradingNode? = nodes[id]
    fun getAllNodes(): List<ColorGradingNode> = nodes.values.toList()
    fun evaluate(type: CurveType, input: Float): Float {
        val node = nodes.values.find { it.curveType == type } ?: return input
        if (node.points.size < 2) return input
        val sorted = node.points.sortedBy { it.x }
        val before = sorted.lastOrNull { it.x <= input } ?: sorted.first()
        val after = sorted.firstOrNull { it.x > input } ?: sorted.last()
        if (before.x == after.x) return before.y
        val t = (input - before.x) / (after.x - before.x)
        return before.y + (after.y - before.y) * t
    }
    fun clear() { nodes.clear() }
}
