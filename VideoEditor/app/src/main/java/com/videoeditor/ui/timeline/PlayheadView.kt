package com.videoeditor.ui.timeline

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.videoeditor.ui.theme.PlayheadColor

@Composable
fun PlayheadView(
    positionUs: Long,
    totalHeight: androidx.compose.ui.unit.Dp,
    pixelsPerMicrosecond: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(totalHeight)
            .drawBehind {
                val triangleSize = 8.dp.toPx()
                val lineWidth = 1.5.dp.toPx()

                // Triangle at top
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(triangleSize / 2, triangleSize)
                    lineTo(-triangleSize / 2, triangleSize)
                    close()
                }
                drawPath(
                    path = path,
                    color = PlayheadColor
                )

                // Vertical line
                drawRect(
                    color = PlayheadColor,
                    topLeft = Offset(-lineWidth / 2, triangleSize),
                    size = Size(lineWidth, size.height - triangleSize)
                )
            }
    )
}
