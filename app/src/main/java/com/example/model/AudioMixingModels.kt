package com.example.model

data class EqualizerBand(
    val frequencyLabel: String, // "60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz"
    val gainDb: Float = 0f // -12dB to +12dB
)

data class AudioCompressorConfig(
    val enabled: Boolean = false,
    val thresholdDb: Float = -20f, // -60dB to 0dB
    val ratio: Float = 4f, // 1:1 to 20:1
    val attackMs: Float = 15f, // 1ms to 200ms
    val releaseMs: Float = 120f // 10ms to 1000ms
)

data class AudioLimiterConfig(
    val enabled: Boolean = false,
    val ceilingDb: Float = -0.5f, // -12dB to 0dB
    val releaseMs: Float = 50f
)

data class AdvancedAudioMix(
    val equalizerBands: List<EqualizerBand> = listOf(
        EqualizerBand("60Hz (Sub-Bass)", 0f),
        EqualizerBand("230Hz (Bass/Warmth)", 0f),
        EqualizerBand("910Hz (Mid/Body)", 0f),
        EqualizerBand("3.6kHz (Presence)", 0f),
        EqualizerBand("14kHz (Air/Brilliance)", 0f)
    ),
    val compressor: AudioCompressorConfig = AudioCompressorConfig(),
    val limiter: AudioLimiterConfig = AudioLimiterConfig()
)
