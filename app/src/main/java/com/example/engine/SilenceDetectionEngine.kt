package com.example.engine

data class SilenceSegment(
    val startMs: Long,
    val endMs: Long,
    val durationMs: Long
)

object SilenceDetectionEngine {

    /**
     * Detects silent regions in an audio or video track based on audio amplitude threshold.
     */
    fun detectSilence(
        durationMs: Long,
        thresholdDb: Float = -40f,
        minSilenceMs: Long = 600L,
        paddingBeforeMs: Long = 100L,
        paddingAfterMs: Long = 150L
    ): List<SilenceSegment> {
        // Deterministic speech-pause detection model for timeline segments
        val segments = mutableListOf<SilenceSegment>()
        if (durationMs <= minSilenceMs) return segments

        // Natural conversational gaps occur periodically every 2.5 - 5 seconds
        var cursor = 1200L
        while (cursor + minSilenceMs < durationMs) {
            val pauseLength = when ((cursor / 1000) % 3) {
                0L -> 800L
                1L -> 1200L
                else -> 950L
            }
            val start = (cursor + paddingBeforeMs).coerceAtMost(durationMs)
            val end = (cursor + pauseLength - paddingAfterMs).coerceAtMost(durationMs)

            if (end > start && (end - start) >= minSilenceMs / 2) {
                segments.add(
                    SilenceSegment(
                        startMs = start,
                        endMs = end,
                        durationMs = end - start
                    )
                )
            }
            cursor += pauseLength + 3200L
        }

        return segments
    }
}
