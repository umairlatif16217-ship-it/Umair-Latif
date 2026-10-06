package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.KeyframeEngine
import com.example.engine.VisualEffectsLibrary
import com.example.model.AspectRatio
import com.example.model.Clip
import com.example.model.MaskType
import com.example.model.MediaType
import com.example.model.Project
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun VideoPreviewPlayer(
    project: Project?,
    playheadMs: Long,
    isPlaying: Boolean,
    previewEffectName: String? = null,
    previewEffectIntensity: Float = 0.7f,
    onPlayPauseToggle: () -> Unit,
    onStepFrame: (Boolean) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showInfoBadge by remember { mutableStateOf(true) }

    val activeClips = remember(project, playheadMs) {
        project?.tracks?.filter { !it.isHidden }?.flatMap { track ->
            track.clips.filter { clip ->
                playheadMs >= clip.startInTimelineMs && playheadMs < (clip.startInTimelineMs + clip.durationMs)
            }
        } ?: emptyList()
    }

    val primaryVideoClip = activeClips.find { it.mediaType == MediaType.VIDEO }
    val is4KSource = (primaryVideoClip?.sourceWidth ?: 0) >= 3840

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117))
    ) {
        // Top Player Canvas Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                // Resolution Badge
                Surface(
                    color = if (is4KSource) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF21262D),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStrokeOrNull(is4KSource, Color(0xFF00E5FF))
                ) {
                    Text(
                        text = if (is4KSource) "4K MASTER (3840×2160)" else "${project?.aspectRatio?.label?.take(10) ?: "1080p"}",
                        color = if (is4KSource) Color(0xFF00E5FF) else Color(0xFF8B949E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (previewEffectName != null) {
                    Surface(
                        color = Color(0xFFFF5252).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LIVE VFX: $previewEffectName ${(previewEffectIntensity * 100).toInt()}%",
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Timecode Display
            Text(
                text = formatTimecode(playheadMs) + " / " + formatTimecode(project?.durationMs ?: 0L),
                color = Color(0xFFC9D1D9),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // Viewport Box with dynamic Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val aspect = project?.aspectRatio?.ratio ?: (16f / 9f)

            Box(
                modifier = Modifier
                    .aspectRatio(aspect)
                    .fillMaxSize()
                    .background(Color(0xFF161B22))
                    .clip(RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Render Composite Layers
                VideoCanvasRenderer(
                    clips = activeClips,
                    playheadMs = playheadMs,
                    previewEffectName = previewEffectName,
                    previewEffectIntensity = previewEffectIntensity,
                    isPlaying = isPlaying
                )
            }
        }

        // Playback Controller Toolbar
        Surface(
            color = Color(0xFF161B22),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Rewind 1 frame
                IconButton(
                    onClick = { onStepFrame(false) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_step_rewind")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Frame",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Main Button
                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF238636), CircleShape)
                        .testTag("btn_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Forward 1 frame
                IconButton(
                    onClick = { onStepFrame(true) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_step_forward")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Frame",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mute toggle
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute Audio",
                        tint = if (isMuted) Color(0xFFFF5252) else Color(0xFF8B949E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Speed indicator
                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        playbackSpeed = when (playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.5f
                            else -> 1.0f
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = Color(0xFF58A6FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${playbackSpeed}x",
                            color = Color(0xFF58A6FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCanvasRenderer(
    clips: List<Clip>,
    playheadMs: Long,
    previewEffectName: String? = null,
    previewEffectIntensity: Float = 0.7f,
    isPlaying: Boolean
) {
    if (clips.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Empty",
                    tint = Color(0xFF484F58),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Timeline Empty • Tap + to Import Media",
                    color = Color(0xFF8B949E),
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw each active layer (video, graphic, effect, text)
        clips.forEach { clip ->
            when (clip.mediaType) {
                MediaType.VIDEO, MediaType.IMAGE -> {
                    RenderVisualClipLayer(
                        clip = clip,
                        currentTimeMs = playheadMs,
                        previewEffectName = previewEffectName,
                        previewEffectIntensity = previewEffectIntensity,
                        isPlaying = isPlaying
                    )
                }
                MediaType.TEXT -> {
                    RenderTextLayer(clip = clip, currentTimeMs = playheadMs)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun RenderVisualClipLayer(
    clip: Clip,
    currentTimeMs: Long,
    previewEffectName: String?,
    previewEffectIntensity: Float,
    isPlaying: Boolean
) {
    // Evaluate Keyframe Transforms
    val kfValues = remember(clip.keyframes, currentTimeMs) {
        KeyframeEngine.evaluate(
            keyframes = clip.keyframes,
            currentTimeMs = currentTimeMs,
            defaultPosX = clip.positionX,
            defaultPosY = clip.positionY,
            defaultScale = clip.scale,
            defaultRotation = clip.rotation,
            defaultOpacity = clip.opacity
        )
    }

    val finalOpacity = kfValues.opacity.coerceIn(0f, 1f)
    val finalScale = kfValues.scale.coerceAtLeast(0.1f)
    val finalRotation = (clip.rotation + kfValues.rotation) % 360f

    // Calculate Animated Shaders / Canvas
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = finalOpacity
                scaleX = finalScale * (if (clip.flipHorizontal) -1f else 1f)
                scaleY = finalScale * (if (clip.flipVertical) -1f else 1f)
                rotationZ = finalRotation
                translationX = kfValues.positionX * 3f
                translationY = kfValues.positionY * 3f
            }
    ) {
        val w = size.width
        val h = size.height

        // Base gradient simulating rich cinematic video frame
        val videoBaseBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF0284C7)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        drawRect(brush = videoBaseBrush)

        // Draw animated stylized waveforms or cinematic scene geometry
        val timeRatio = ((currentTimeMs % 4000L).toFloat() / 4000f)
        val waveCenter = Offset(w * 0.5f + (cos(timeRatio * 6.28f) * 40f), h * 0.5f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color.Transparent),
                center = waveCenter,
                radius = w * 0.45f
            ),
            center = waveCenter,
            radius = w * 0.45f
        )

        // Draw horizon / grid lines for visual video content feel
        val path = Path().apply {
            moveTo(0f, h * 0.65f)
            cubicTo(
                w * 0.35f, h * (0.6f + sin(timeRatio * 6.28f) * 0.05f),
                w * 0.7f, h * (0.7f - cos(timeRatio * 6.28f) * 0.05f),
                w, h * 0.65f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF0F172A))
            )
        )

        // Apply Active Video Effects & Real-Time Preview
        val activeEffects = clip.effects.map { it.name } + listOfNotNull(previewEffectName)

        activeEffects.forEach { effectName ->
            val intensity = if (effectName == previewEffectName) previewEffectIntensity else 0.7f

            when {
                effectName.contains("Grain", ignoreCase = true) || effectName.contains("Film", ignoreCase = true) -> {
                    // 35mm Film Grain overlay simulation
                    for (i in 0 until (180 * intensity).toInt()) {
                        val gx = ((i * 7919 + currentTimeMs) % w.toInt()).toFloat()
                        val gy = ((i * 4909 + currentTimeMs) % h.toInt()).toFloat()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f * intensity),
                            radius = 1.2f,
                            center = Offset(gx, gy)
                        )
                    }
                }
                effectName.contains("Vignette", ignoreCase = true) -> {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f * intensity)),
                            center = Offset(w / 2f, h / 2f),
                            radius = w * 0.7f
                        )
                    )
                }
                effectName.contains("Glitch", ignoreCase = true) -> {
                    val barY = ((currentTimeMs * 2) % h.toInt()).toFloat()
                    drawRect(
                        color = Color(0xFF00FFCC).copy(alpha = 0.35f * intensity),
                        topLeft = Offset(0f, barY),
                        size = Size(w, 24f * intensity)
                    )
                    drawRect(
                        color = Color(0xFFFF0055).copy(alpha = 0.35f * intensity),
                        topLeft = Offset(12f * intensity, (barY + 30f) % h),
                        size = Size(w, 14f * intensity)
                    )
                }
                effectName.contains("RGB", ignoreCase = true) || effectName.contains("Chromatic", ignoreCase = true) -> {
                    // RGB Split fringe lines
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0x33FF0000), Color.Transparent, Color(0x3300FFFF))
                        )
                    )
                }
                effectName.contains("Neon", ignoreCase = true) || effectName.contains("Cyber", ignoreCase = true) -> {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFD946EF).copy(alpha = 0.3f * intensity), Color.Transparent),
                            radius = w * 0.6f
                        )
                    )
                }
                effectName.contains("Warmth", ignoreCase = true) || effectName.contains("Golden", ignoreCase = true) -> {
                    drawRect(
                        color = Color(0xFFF59E0B).copy(alpha = 0.25f * intensity)
                    )
                }
                effectName.contains("Teal", ignoreCase = true) -> {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.25f * intensity), Color(0xFFF97316).copy(alpha = 0.2f * intensity))
                        )
                    )
                }
                effectName.contains("Noir", ignoreCase = true) || effectName.contains("Black and White", ignoreCase = true) -> {
                    drawRect(
                        color = Color(0xFF888888).copy(alpha = 0.5f * intensity)
                    )
                }
            }
        }

        // Mask processing
        if (clip.mask.type != MaskType.NONE) {
            when (clip.mask.type) {
                MaskType.CIRCLE -> {
                    val radius = (w.coerceAtMost(h) * 0.4f) * clip.mask.scaleX
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = radius,
                        center = Offset(w / 2f + clip.mask.positionX, h / 2f + clip.mask.positionY),
                        style = Stroke(width = 3f)
                    )
                }
                MaskType.RECTANGLE -> {
                    val rw = w * 0.7f * clip.mask.scaleX
                    val rh = h * 0.7f * clip.mask.scaleY
                    drawRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = Offset((w - rw) / 2f + clip.mask.positionX, (h - rh) / 2f + clip.mask.positionY),
                        size = Size(rw, rh),
                        style = Stroke(width = 3f)
                    )
                }
                else -> {}
            }
        }

        // Chroma Key indicator border if active
        if (clip.chromaKey.enabled) {
            drawRect(
                color = Color.Green.copy(alpha = 0.2f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun RenderTextLayer(clip: Clip, currentTimeMs: Long) {
    val style = clip.textStyle
    val text = clip.textContent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = when (style.alignment) {
            "Left" -> Alignment.CenterStart
            "Right" -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Surface(
            color = if (style.backgroundColorHex != null) Color(android.graphics.Color.parseColor(style.backgroundColorHex)) else Color.Transparent,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                color = try { Color(android.graphics.Color.parseColor(style.textColorHex)) } catch (_: Exception) { Color.White },
                fontSize = style.fontSizeSp.sp,
                fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                textAlign = when (style.alignment) {
                    "Left" -> TextAlign.Start
                    "Right" -> TextAlign.End
                    else -> TextAlign.Center
                },
                modifier = Modifier
                    .padding(4.dp)
                    .graphicsLayer {
                        // Text shadow/glow
                        shadowElevation = style.shadowRadius
                    }
            )
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val frames = ((ms % 1000) * 30 / 1000).toInt()
    return String.format("%02d:%02d:%02d", minutes, seconds, frames)
}

@Composable
private fun BorderStrokeOrNull(condition: Boolean, color: Color): androidx.compose.foundation.BorderStroke? {
    return if (condition) androidx.compose.foundation.BorderStroke(1.dp, color) else null
}
