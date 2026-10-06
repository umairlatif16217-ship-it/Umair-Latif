package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Clip
import com.example.model.MediaType
import com.example.model.Project
import com.example.model.TimelineTrack
import com.example.model.TrackType
import kotlin.math.roundToInt

@Composable
fun MultiTrackTimeline(
    project: Project?,
    playheadMs: Long,
    zoom: Float,
    selectedClipId: String?,
    onSelectClip: (String?) -> Unit,
    onSeek: (Long) -> Unit,
    onSplitClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pxPerSecond = (60f * zoom).coerceIn(30f, 220f)
    val totalDurationMs = (project?.durationMs ?: 10000L).coerceAtLeast(10000L)
    val timelineWidthDp = ((totalDurationMs / 1000f) * pxPerSecond).dp + 200.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F141C))
    ) {
        // Quick Action Timeline Bar
        Surface(
            color = Color(0xFF161E2E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Split Action
                IconButton(
                    onClick = onSplitClip,
                    enabled = selectedClipId != null,
                    modifier = Modifier.testTag("btn_timeline_split")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Split Clip",
                        tint = if (selectedClipId != null) Color(0xFF58A6FF) else Color(0xFF484F58)
                    )
                }

                // Duplicate Action
                IconButton(
                    onClick = onDuplicateClip,
                    enabled = selectedClipId != null,
                    modifier = Modifier.testTag("btn_timeline_duplicate")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Clip",
                        tint = if (selectedClipId != null) Color(0xFF38BDF8) else Color(0xFF484F58)
                    )
                }

                // Delete Action
                IconButton(
                    onClick = onDeleteClip,
                    enabled = selectedClipId != null,
                    modifier = Modifier.testTag("btn_timeline_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Clip",
                        tint = if (selectedClipId != null) Color(0xFFFF5252) else Color(0xFF484F58)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Zoom Out
                IconButton(
                    onClick = { onZoomChange(zoom - 0.25f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out Timeline",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${(zoom * 100).toInt()}%",
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Zoom In
                IconButton(
                    onClick = { onZoomChange(zoom + 0.25f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In Timeline",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Main Horizontal Scrollable Timeline Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .horizontalScroll(scrollState)
                .pointerInput(totalDurationMs, pxPerSecond) {
                    detectTapGestures { offset ->
                        val clickedMs = ((offset.x / (pxPerSecond * density)) * 1000f).toLong()
                        onSeek(clickedMs.coerceIn(0L, totalDurationMs))
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .width(timelineWidthDp)
                    .fillMaxHeight()
            ) {
                // Timecode Ruler
                TimelineRuler(
                    totalDurationMs = totalDurationMs,
                    pxPerSecond = pxPerSecond
                )

                // Render Tracks
                val tracks = project?.tracks ?: emptyList()
                tracks.forEach { track ->
                    TimelineTrackRow(
                        track = track,
                        pxPerSecond = pxPerSecond,
                        selectedClipId = selectedClipId,
                        onSelectClip = onSelectClip
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Red Playhead Needle & Scrubber
            val playheadOffsetDp = ((playheadMs / 1000f) * pxPerSecond).dp

            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp - 8.dp)
                    .fillMaxHeight()
                    .width(16.dp)
                    .pointerInput(totalDurationMs, pxPerSecond) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / (pxPerSecond * density)) * 1000f).toLong()
                            onSeek((playheadMs + deltaMs).coerceIn(0L, totalDurationMs))
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // Playhead Needle Head
                Box(
                    modifier = Modifier
                        .size(14.dp, 12.dp)
                        .background(Color(0xFFFF3366), RoundedCornerShape(2.dp))
                )

                // Needle Line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFFF3366))
                )
            }
        }
    }
}

@Composable
private fun TimelineRuler(
    totalDurationMs: Long,
    pxPerSecond: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFF161E2E))
    ) {
        val seconds = (totalDurationMs / 1000L).toInt() + 2
        for (sec in 0..seconds) {
            val x = sec * pxPerSecond * density

            // Major tick every second
            drawLine(
                color = Color(0xFF485467),
                start = Offset(x, size.height * 0.5f),
                end = Offset(x, size.height),
                strokeWidth = 1.5f
            )

            // Sub-ticks
            for (sub in 1..3) {
                val subX = x + (sub * (pxPerSecond / 4f) * density)
                drawLine(
                    color = Color(0xFF2E384D),
                    start = Offset(subX, size.height * 0.75f),
                    end = Offset(subX, size.height),
                    strokeWidth = 1f
                )
            }
        }
    }
}

@Composable
private fun TimelineTrackRow(
    track: TimelineTrack,
    pxPerSecond: Float,
    selectedClipId: String?,
    onSelectClip: (String?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF131924), RoundedCornerShape(4.dp))
    ) {
        // Track Clips
        track.clips.forEach { clip ->
            val startDp = ((clip.startInTimelineMs / 1000f) * pxPerSecond).dp
            val clipWidthDp = ((clip.durationMs / 1000f) * pxPerSecond).dp.coerceAtLeast(32.dp)
            val isSelected = clip.id == selectedClipId

            TimelineClipBox(
                clip = clip,
                isSelected = isSelected,
                modifier = Modifier
                    .offset(x = startDp)
                    .width(clipWidthDp)
                    .height(40.dp)
                    .clickable { onSelectClip(clip.id) }
            )
        }
    }
}

@Composable
private fun TimelineClipBox(
    clip: Clip,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val clipBgColor = when (clip.mediaType) {
        MediaType.VIDEO -> if (isSelected) Color(0xFF2563EB) else Color(0xFF1E3A8A)
        MediaType.AUDIO -> if (isSelected) Color(0xFF059669) else Color(0xFF065F46)
        MediaType.TEXT -> if (isSelected) Color(0xFF7C3AED) else Color(0xFF5B21B6)
        else -> Color(0xFF374151)
    }

    Surface(
        color = clipBgColor,
        shape = RoundedCornerShape(4.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF38BDF8)) else null,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = when (clip.mediaType) {
                    MediaType.VIDEO -> Icons.Default.Videocam
                    MediaType.AUDIO -> Icons.Default.MusicNote
                    MediaType.TEXT -> Icons.Default.TextFields
                    else -> Icons.Default.Movie
                },
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = clip.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 4K Badge indicator on timeline clip
            if (clip.sourceWidth >= 3840) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = Color(0xFF00E5FF),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = "4K",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            // Keyframe Dots Indicator on Timeline
            if (clip.keyframes.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Row {
                    repeat(clip.keyframes.size.coerceAtMost(3)) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .size(5.dp)
                                .background(Color(0xFFFFD700), CircleShape)
                        )
                    }
                }
            }
        }
    }
}
