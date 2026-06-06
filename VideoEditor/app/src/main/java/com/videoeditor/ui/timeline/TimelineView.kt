package com.videoeditor.ui.timeline

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videoeditor.data.model.*
import com.videoeditor.ui.theme.*

private const val PIXELS_PER_MICROSECOND = 0.05f // 50px per second
private const val TRACK_HEIGHT = 48f
private const val RULER_HEIGHT = 28f

@Composable
fun TimelineView(
    project: Project,
    currentPositionUs: Long,
    selectedClipId: String?,
    onPositionChange: (Long) -> Unit,
    onClipClick: (String) -> Unit,
    onClipMove: (clipId: String, newStartUs: Long) -> Unit,
    onClipTrim: (clipId: String, newTrimStartUs: Long, newTrimEndUs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val totalDurationUs = project.totalDurationUs()
    val timelineWidth = (totalDurationUs * PIXELS_PER_MICROSECOND).toFloat()
    val minWidth = 2000f
    val effectiveWidth = maxOf(timelineWidth, minWidth)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TimelineBackground)
    ) {
        // Time Ruler
        TimeRuler(
            totalDurationUs = totalDurationUs,
            widthPx = effectiveWidth,
            currentPositionUs = currentPositionUs,
            onSeek = onPositionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(RULER_HEIGHT.dp)
        )

        // Tracks
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier.width(with(androidx.compose.ui.platform.LocalDensity.current) {
                    effectiveWidth.toDp()
                })
            ) {
                // Video tracks
                project.videoTracks.forEach { track ->
                    TrackView(
                        track = track,
                        selectedClipId = selectedClipId,
                        totalWidthPx = effectiveWidth,
                        onClipClick = onClipClick,
                        onClipMove = onClipMove,
                        onClipTrim = onClipTrim,
                        clipColor = VideoClipColor,
                        selectedClipColor = VideoClipSelectedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TRACK_HEIGHT.dp)
                    )
                }

                // Audio tracks
                project.audioTracks.forEach { track ->
                    TrackView(
                        track = track,
                        selectedClipId = selectedClipId,
                        totalWidthPx = effectiveWidth,
                        onClipClick = onClipClick,
                        onClipMove = onClipMove,
                        onClipTrim = onClipTrim,
                        clipColor = AudioClipColor,
                        selectedClipColor = AudioClipSelectedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TRACK_HEIGHT.dp)
                    )
                }

                // Text clips track
                if (project.textClips.isNotEmpty()) {
                    TextTrackView(
                        textClips = project.textClips,
                        selectedClipId = selectedClipId,
                        totalWidthPx = effectiveWidth,
                        onClipClick = onClipClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TRACK_HEIGHT.dp)
                    )
                }
            }

            // Playhead
            PlayheadView(
                positionUs = currentPositionUs,
                totalHeight = (project.videoTracks.size + project.audioTracks.size +
                        if (project.textClips.isNotEmpty()) 1 else 0) * TRACK_HEIGHT.dp + RULER_HEIGHT.dp,
                pixelsPerMicrosecond = PIXELS_PER_MICROSECOND,
                modifier = Modifier.offset(
                    x = with(androidx.compose.ui.platform.LocalDensity.current) {
                        (currentPositionUs * PIXELS_PER_MICROSECOND).toDp()
                    }
                )
            )
        }
    }
}

@Composable
fun TimeRuler(
    totalDurationUs: Long,
    widthPx: Float,
    currentPositionUs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = TimeRulerTextColor, fontSize = 9.sp)

    Box(
        modifier = modifier
            .drawBehind {
                // Draw ruler marks
                val intervalUs = calculateIntervalUs(totalDurationUs)
                var currentUs = 0L
                while (currentUs <= totalDurationUs) {
                    val x = currentUs * PIXELS_PER_MICROSECOND
                    // Major tick
                    drawLine(
                        color = TimeRulerColor,
                        start = Offset(x, size.height * 0.6f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    // Time label
                    val seconds = currentUs / 1_000_000f
                    val text = formatTime(currentUs)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = text,
                        topLeft = Offset(x + 2f, 0f),
                        style = textStyle
                    )

                    // Minor ticks
                    val minorInterval = intervalUs / 5
                    for (i in 1 until 5) {
                        val minorUs = currentUs + minorInterval * i
                        if (minorUs <= totalDurationUs) {
                            val minorX = minorUs * PIXELS_PER_MICROSECOND
                            drawLine(
                                color = TimeRulerColor.copy(alpha = 0.5f),
                                start = Offset(minorX, size.height * 0.8f),
                                end = Offset(minorX, size.height),
                                strokeWidth = 0.5f
                            )
                        }
                    }
                    currentUs += intervalUs
                }
            }
            .pointerInput(totalDurationUs) {
                detectTapGestures { offset ->
                    val positionUs = (offset.x / PIXELS_PER_MICROSECOND).toLong()
                        .coerceIn(0, totalDurationUs)
                    onSeek(positionUs)
                }
            }
    )
}

@Composable
fun TrackView(
    track: Track,
    selectedClipId: String?,
    totalWidthPx: Float,
    onClipClick: (String) -> Unit,
    onClipMove: (clipId: String, newStartUs: Long) -> Unit,
    onClipTrim: (clipId: String, newTrimStartUs: Long, newTrimEndUs: Long) -> Unit,
    clipColor: Color,
    selectedClipColor: Color,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current

    Box(
        modifier = modifier
            .background(TrackBackground)
            .padding(vertical = 2.dp)
    ) {
        track.clips.forEach { clip ->
            val isSelected = clip.id == selectedClipId
            val clipStartPx = clip.startUs * PIXELS_PER_MICROSECOND
            val clipWidthPx = clip.durationUs * PIXELS_PER_MICROSECOND

            ClipView(
                clip = clip,
                isSelected = isSelected,
                color = if (isSelected) selectedClipColor else clipColor,
                widthPx = clipWidthPx,
                onClick = { onClipClick(clip.id) },
                onMove = { deltaPx ->
                    val deltaUs = (deltaPx / PIXELS_PER_MICROSECOND).toLong()
                    val newStart = (clip.startUs + deltaUs).coerceAtLeast(0)
                    onClipMove(clip.id, newStart)
                },
                onTrimStart = { deltaPx ->
                    val deltaUs = (deltaPx / PIXELS_PER_MICROSECOND).toLong()
                    val newTrimStart = (clip.trimStartUs + deltaUs).coerceAtLeast(0)
                    val newTrimEnd = clip.trimEndUs
                    if (newTrimStart < newTrimEnd) {
                        onClipTrim(clip.id, newTrimStart, newTrimEnd)
                    }
                },
                onTrimEnd = { deltaPx ->
                    val deltaUs = (deltaPx / PIXELS_PER_MICROSECOND).toLong()
                    val newTrimEnd = (clip.trimEndUs + deltaUs).coerceAtLeast(clip.trimStartUs + 100_000)
                    onClipTrim(clip.id, clip.trimStartUs, newTrimEnd)
                },
                modifier = Modifier.offset(
                    x = with(density) { clipStartPx.toDp() }
                )
            )
        }
    }
}

// Type alias for generic track
typealias Track = com.videoeditor.data.model.VideoTrack

@Composable
fun TextTrackView(
    textClips: List<TextClip>,
    selectedClipId: String?,
    totalWidthPx: Float,
    onClipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current

    Box(
        modifier = modifier
            .background(TrackBackground)
            .padding(vertical = 2.dp)
    ) {
        textClips.forEach { clip ->
            val isSelected = clip.id == selectedClipId
            val clipStartPx = clip.startUs * PIXELS_PER_MICROSECOND
            val clipWidthPx = (clip.endUs - clip.startUs) * PIXELS_PER_MICROSECOND

            Box(
                modifier = Modifier
                    .offset(x = with(density) { clipStartPx.toDp() })
                    .width(with(density) { clipWidthPx.toDp() })
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) VideoClipSelectedColor else TextClipColor,
                        RoundedCornerShape(4.dp)
                    )
                    .pointerInput(clip.id) {
                        detectTapGestures { onClipClick(clip.id) }
                    }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    clip.text.ifEmpty { "文字" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

private fun calculateIntervalUs(totalDurationUs: Long): Long {
    val totalSec = totalDurationUs / 1_000_000f
    return when {
        totalSec <= 10 -> 1_000_000L       // 1s
        totalSec <= 30 -> 2_000_000L       // 2s
        totalSec <= 60 -> 5_000_000L       // 5s
        totalSec <= 300 -> 10_000_000L     // 10s
        totalSec <= 600 -> 30_000_000L     // 30s
        else -> 60_000_000L                // 1min
    }
}

private fun formatTime(us: Long): String {
    val totalSec = us / 1_000_000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}

private fun Modifier.background(color: Color) = this.then(
    Modifier.drawBehind { drawRect(color) }
)
