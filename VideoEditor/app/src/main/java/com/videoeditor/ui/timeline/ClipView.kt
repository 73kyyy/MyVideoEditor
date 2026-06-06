package com.videoeditor.ui.timeline

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.videoeditor.data.model.Clip

@Composable
fun ClipView(
    clip: Clip,
    isSelected: Boolean,
    color: Color,
    widthPx: Float,
    onClick: () -> Unit,
    onMove: (deltaPx: Float) -> Unit,
    onTrimStart: (deltaPx: Float) -> Unit,
    onTrimEnd: (deltaPx: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val handleWidth = 12f

    Box(
        modifier = modifier
            .width(with(density) { widthPx.toDp() })
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .drawBehind {
                // Clip background
                drawRect(color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))

                // Film strip pattern for video clips
                val stripeWidth = 3.dp.toPx()
                val stripeSpacing = 12.dp.toPx()
                var x = stripeSpacing
                while (x < size.width - stripeSpacing) {
                    drawRect(
                        Color.White.copy(alpha = 0.15f),
                        topLeft = Offset(x, 0f),
                        size = Size(stripeWidth, size.height * 0.2f)
                    )
                    drawRect(
                        Color.White.copy(alpha = 0.15f),
                        topLeft = Offset(x, size.height * 0.8f),
                        size = Size(stripeWidth, size.height * 0.2f)
                    )
                    x += stripeSpacing
                }

                // Selection border
                if (isSelected) {
                    drawRect(
                        Color.White,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, 2.dp.toPx())
                    )
                    drawRect(
                        Color.White,
                        topLeft = Offset(0f, size.height - 2.dp.toPx()),
                        size = Size(size.width, 2.dp.toPx())
                    )
                }
            }
            .pointerInput(clip.id) {
                detectTapGestures { onClick() }
            }
            .pointerInput(clip.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x)
                }
            }
            .padding(horizontal = with(density) { handleWidth.toDp() })
    ) {
        // Clip label
        val fileName = clip.sourcePath.substringAfterLast("/")
        Text(
            fileName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(horizontal = 4.dp)
        )

        // Left trim handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = with(density) { (-handleWidth).toDp() })
                .width(with(density) { handleWidth.toDp() })
                .fillMaxHeight()
                .pointerInput(clip.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onTrimStart(dragAmount.x)
                    }
                }
                .drawBehind {
                    drawRect(
                        Color.White.copy(alpha = 0.5f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                    )
                }
        )

        // Right trim handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(with(density) { handleWidth.toDp() })
                .fillMaxHeight()
                .pointerInput(clip.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onTrimEnd(dragAmount.x)
                    }
                }
                .drawBehind {
                    drawRect(
                        Color.White.copy(alpha = 0.5f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                    )
                }
        )
    }
}
