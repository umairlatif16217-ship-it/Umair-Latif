package com.example.engine

import com.example.model.InterpolationType
import com.example.model.Keyframe
import kotlin.math.pow

object KeyframeEngine {

    /**
     * Interpolates values between keyframes at a given timestamp.
     */
    fun evaluate(
        keyframes: List<Keyframe>,
        currentTimeMs: Long,
        defaultPosX: Float = 0f,
        defaultPosY: Float = 0f,
        defaultScale: Float = 1.0f,
        defaultRotation: Float = 0f,
        defaultOpacity: Float = 1.0f
    ): KeyframeValues {
        if (keyframes.isEmpty()) {
            return KeyframeValues(defaultPosX, defaultPosY, defaultScale, defaultRotation, defaultOpacity)
        }

        val sorted = keyframes.sortedBy { it.timeMs }

        if (currentTimeMs <= sorted.first().timeMs) {
            val first = sorted.first()
            return KeyframeValues(first.positionX, first.positionY, first.scale, first.rotation, first.opacity)
        }

        if (currentTimeMs >= sorted.last().timeMs) {
            val last = sorted.last()
            return KeyframeValues(last.positionX, last.positionY, last.scale, last.rotation, last.opacity)
        }

        // Find surrounding keyframes
        var prev = sorted.first()
        var next = sorted.last()
        for (i in 0 until sorted.size - 1) {
            if (currentTimeMs >= sorted[i].timeMs && currentTimeMs <= sorted[i + 1].timeMs) {
                prev = sorted[i]
                next = sorted[i + 1]
                break
            }
        }

        val delta = (next.timeMs - prev.timeMs).toFloat()
        val rawT = if (delta > 0) ((currentTimeMs - prev.timeMs) / delta).coerceIn(0f, 1f) else 0f

        val easedT = applyEasing(rawT, prev.interpolation)

        return KeyframeValues(
            positionX = lerp(prev.positionX, next.positionX, easedT),
            positionY = lerp(prev.positionY, next.positionY, easedT),
            scale = lerp(prev.scale, next.scale, easedT),
            rotation = lerp(prev.rotation, next.rotation, easedT),
            opacity = lerp(prev.opacity, next.opacity, easedT)
        )
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }

    private fun applyEasing(t: Float, type: InterpolationType): Float {
        return when (type) {
            InterpolationType.LINEAR -> t
            InterpolationType.EASE_IN -> t * t
            InterpolationType.EASE_OUT -> t * (2f - t)
            InterpolationType.EASE_IN_OUT -> {
                if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
            }
        }
    }
}

data class KeyframeValues(
    val positionX: Float,
    val positionY: Float,
    val scale: Float,
    val rotation: Float,
    val opacity: Float
)
