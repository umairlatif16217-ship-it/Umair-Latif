package com.example.model

import java.util.UUID

enum class MediaType {
    VIDEO, IMAGE, AUDIO, TEXT, STICKER, SHAPE
}

enum class TrackType {
    MAIN_VIDEO,
    OVERLAY_VIDEO,
    AUDIO,
    VOICEOVER,
    TEXT_CAPTION,
    EFFECTS
}

enum class AspectRatio(val label: String, val ratio: Float, val width: Int, val height: Int) {
    RATIO_16_9("16:9 Landscape", 16f / 9f, 1920, 1080),
    RATIO_9_16("9:16 Vertical (TikTok/Reels/Shorts)", 9f / 16f, 1080, 1920),
    RATIO_1_1("1:1 Square (Instagram)", 1f, 1080, 1080),
    RATIO_4_5("4:5 Feed Portrait", 4f / 5f, 1080, 1350),
    RATIO_4_3("4:3 Standard", 4f / 3f, 1440, 1080),
    RATIO_21_9("21:9 Ultra-Wide Cinematic", 21f / 9f, 2560, 1080)
}

enum class ExportResolution(val label: String, val width: Int, val height: Int, val is4K: Boolean = false) {
    RES_ORIGINAL("Original Resolution", 0, 0),
    RES_4K("4K Ultra HD (3840×2160)", 3840, 2160, true),
    RES_2K("2K Quad HD (2560×1440)", 2560, 1440),
    RES_1080P("1080p Full HD (1920×1080)", 1920, 1080),
    RES_720P("720p HD (1280×720)", 1280, 720),
    RES_480P("480p SD (854×480)", 854, 480)
}

enum class ExportFps(val label: String, val fps: Int) {
    FPS_ORIGINAL("Original FPS", 0),
    FPS_24("24 fps (Cinematic)", 24),
    FPS_25("25 fps (PAL)", 25),
    FPS_30("30 fps (Standard)", 30),
    FPS_50("50 fps", 50),
    FPS_60("60 fps (Smooth / High Frame Rate)", 60)
}

enum class ExportCodec(val label: String, val mime: String) {
    H264("H.264 / AVC (Most Compatible)", "video/avc"),
    H265("H.265 / HEVC (Higher Quality, Smaller Size)", "video/hevc"),
    AV1("AV1 (Modern High Efficiency)", "video/av01")
}

enum class ExportQualityPreset(val label: String, val bitrateMultiplier: Float) {
    EFFICIENT("Efficient (Small File)", 0.6f),
    BALANCED("Balanced (Recommended)", 1.0f),
    HIGH("High Quality", 1.5f),
    MAXIMUM("Maximum Quality (Pro / Master)", 2.5f)
}

enum class InterpolationType {
    LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT
}

data class Keyframe(
    val id: String = UUID.randomUUID().toString(),
    val timeMs: Long,
    val positionX: Float = 0f, // -100f to +100f or normalized
    val positionY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val interpolation: InterpolationType = InterpolationType.EASE_IN_OUT
)

enum class MaskType {
    NONE, RECTANGLE, CIRCLE, ELLIPSE, LINEAR_GRADIENT, FREEFORM
}

data class MaskConfig(
    val type: MaskType = MaskType.NONE,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val rotation: Float = 0f,
    val feather: Float = 10f, // pixels / blur
    val opacity: Float = 1.0f,
    val invert: Boolean = false
)

data class ChromaKeyConfig(
    val enabled: Boolean = false,
    val targetColorHex: String = "#00FF00", // Green screen default
    val tolerance: Float = 0.25f, // 0.0 to 1.0
    val edgeSoftness: Float = 0.15f,
    val spillReduction: Float = 0.5f,
    val shadowProtection: Float = 0.2f
)

data class ColorAdjustments(
    val exposure: Float = 0f, // -1f to 1f
    val contrast: Float = 1f, // 0.5f to 2f
    val brightness: Float = 0f, // -0.5f to 0.5f
    val saturation: Float = 1f, // 0f to 2f
    val vibrance: Float = 0f, // -1f to 1f
    val temperature: Float = 0f, // -1f (cool/blue) to +1f (warm/amber)
    val tint: Float = 0f, // -1f (green) to +1f (magenta)
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val sharpness: Float = 0f,
    val vignette: Float = 0f, // 0f to 1f
    val grain: Float = 0f // 0f to 1f
)

data class VideoEffect(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // Basic, Blur, Distortion, Stylized, Motion
    val intensity: Float = 0.7f,
    val isEnabled: Boolean = true
)

data class Transition(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // Fade, Cross Dissolve, Slide, Push, Zoom, Wipe, Glitch, Flash
    val durationMs: Long = 500L,
    val direction: String = "Left"
)

data class TextStyle(
    val fontName: String = "SansSerif",
    val fontSizeSp: Float = 28f,
    val textColorHex: String = "#FFFFFF",
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val strokeColorHex: String = "#000000",
    val strokeWidth: Float = 2f,
    val shadowColorHex: String = "#80000000",
    val shadowRadius: Float = 4f,
    val backgroundColorHex: String? = null,
    val alignment: String = "Center", // Left, Center, Right
    val letterSpacing: Float = 0f,
    val lineSpacing: Float = 1.2f,
    val isRtl: Boolean = false,
    val animationIn: String = "Fade", // Pop, Slide, Typewriter, Bounce, Zoom, None
    val animationDurationMs: Long = 400L
)

data class CaptionItem(
    val id: String = UUID.randomUUID().toString(),
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val confidence: Float = 0.95f
)

data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val mediaType: MediaType,
    val sourceUri: String? = null,
    val assetPlaceholderRes: Int? = null,
    val startInTimelineMs: Long = 0L,
    val durationMs: Long = 5000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isReversed: Boolean = false,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val opacity: Float = 1.0f,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val scale: Float = 1.0f,
    // Original Video Metadata
    val sourceWidth: Int = 1920,
    val sourceHeight: Int = 1080,
    val sourceFps: Int = 30,
    val sourceBitrate: Long = 12_000_000L,
    val sourceCodec: String = "H.264",
    // Features
    val keyframes: List<Keyframe> = emptyList(),
    val mask: MaskConfig = MaskConfig(),
    val chromaKey: ChromaKeyConfig = ChromaKeyConfig(),
    val colorAdjustments: ColorAdjustments = ColorAdjustments(),
    val effects: List<VideoEffect> = emptyList(),
    val audioEffects: List<String> = emptyList(),
    // Audio specifics
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val pitchSemiTones: Float = 0f,
    val voiceEnhancerPreset: String? = null, // "Clean Voice", "Podcast", "Studio", etc.
    val advancedAudioMix: AdvancedAudioMix = AdvancedAudioMix(),
    // Text / Caption specifics
    val textContent: String = "",
    val textStyle: TextStyle = TextStyle(),
    val captions: List<CaptionItem> = emptyList(),
    val stickerEmoji: String? = null
)

data class TimelineTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TrackType,
    val isLocked: Boolean = false,
    val isHidden: Boolean = false,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val clips: List<Clip> = emptyList()
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Project",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val durationMs: Long = 0L,
    val tracks: List<TimelineTrack> = emptyList(),
    val transitions: List<Transition> = emptyList(),
    val isFavorite: Boolean = false
)
