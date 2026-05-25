package com.myvideo.editor.feature.tracking

class TrackingDataExporter {
    data class TrackPoint(val frame: Int, val x: Float, val y: Float, val confidence: Float)

    fun export(points: List<TrackPoint>): String {
        return points.joinToString("\n") { "${it.frame},${it.x},${it.y},${it.confidence}" }
    }

    fun import(csv: String): List<TrackPoint> {
        return csv.lines().mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 3) TrackPoint(parts[0].toInt(), parts[1].toFloat(), parts[2].toFloat(), parts.getOrNull(3)?.toFloatOrNull() ?: 1f)
            else null
        }
    }
}
