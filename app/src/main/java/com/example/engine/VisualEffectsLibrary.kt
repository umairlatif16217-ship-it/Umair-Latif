package com.example.engine

data class EffectDefinition(
    val id: String,
    val name: String,
    val category: String, // Color & Grading, Blur, Distortion, Stylized, Motion
    val description: String,
    val paramName: String = "Intensity",
    val defaultParam: Float = 0.7f,
    val minParam: Float = 0.0f,
    val maxParam: Float = 1.0f
)

object VisualEffectsLibrary {

    val categories = listOf(
        "Color & Grading",
        "Blur",
        "Distortion",
        "Stylized",
        "Motion"
    )

    val allEffects = listOf(
        // Color & Grading
        EffectDefinition("vfx_brightness", "Brightness Boost", "Color & Grading", "Enhances overall scene luminescence", "Brightness", 0.3f, -1f, 1f),
        EffectDefinition("vfx_contrast", "Punchy Contrast", "Color & Grading", "Deepens blacks and sharpens dynamic range", "Contrast", 0.5f, 0f, 1f),
        EffectDefinition("vfx_saturation", "Vibrant Saturation", "Color & Grading", "Amplifies spectral color richness", "Vibrance", 0.8f, 0f, 2f),
        EffectDefinition("vfx_cinematic_warmth", "Golden Hour Warmth", "Color & Grading", "Infuses warm sunset amber tones", "Warmth", 0.6f, 0f, 1f),
        EffectDefinition("vfx_cyber_cyan", "Teal & Orange Grade", "Color & Grading", "Hollywood blockbuster color grading", "Split Tone", 0.75f, 0f, 1f),
        EffectDefinition("vfx_vignette", "Cinematic Vignette", "Color & Grading", "Subtle darkening around outer frame edges", "Falloff", 0.5f, 0f, 1f),
        EffectDefinition("vfx_film_grain", "35mm Film Grain", "Color & Grading", "Authentic analog cinematic film grain texture", "Grain Size", 0.4f, 0f, 1f),

        // Blur
        EffectDefinition("vfx_gaussian_blur", "Gaussian Softness", "Blur", "Velvety smooth image defocus blur", "Radius", 0.5f, 0f, 1f),
        EffectDefinition("vfx_radial_blur", "Radial Spin Blur", "Blur", "Circular dynamic speed blur from center", "Spin Angle", 0.6f, 0f, 1f),
        EffectDefinition("vfx_zoom_blur", "Hyperspace Zoom Blur", "Blur", "High-velocity explosive zoom trails", "Velocity", 0.65f, 0f, 1f),
        EffectDefinition("vfx_lens_blur", "Anamorphic Bokeh", "Blur", "Cinematic shallow depth of field bokeh", "Aperture", 0.7f, 0f, 1f),

        // Distortion
        EffectDefinition("vfx_rgb_split", "Chromatic Aberration", "Distortion", "Splits Red, Green & Blue color channels", "Offset", 0.55f, 0f, 1f),
        EffectDefinition("vfx_glitch", "Cyber Digital Glitch", "Distortion", "Horizontal digital scanline signal artifacts", "Intensity", 0.7f, 0f, 1f),
        EffectDefinition("vfx_fish_eye", "Ultra Fish-Eye Lens", "Distortion", "Curved wide-angle action camera optics", "Curvature", 0.6f, 0f, 1f),
        EffectDefinition("vfx_ripple", "Liquid Wave Ripple", "Distortion", "Organic fluid water surface oscillation", "Frequency", 0.5f, 0f, 1f),
        EffectDefinition("vfx_swirl", "Vortex Swirl", "Distortion", "Twisting rotational focal vortex", "Twist", 0.45f, 0f, 1f),

        // Stylized
        EffectDefinition("vfx_vhs", "1984 Vintage VHS", "Stylized", "Retro tape tracking noise, phosphor lines & glow", "Tracking", 0.8f, 0f, 1f),
        EffectDefinition("vfx_neon_glow", "Neon Cyberpunk Glow", "Stylized", "Luminescent electric neon contour edge glow", "Glow Radius", 0.75f, 0f, 1f),
        EffectDefinition("vfx_noir_bw", "Noir Black & White", "Stylized", "High-contrast vintage monochrome cinema", "Contrast", 0.9f, 0f, 1f),
        EffectDefinition("vfx_comic", "Pop Art Halftone", "Stylized", "Inked comic book cel-shaded graphics", "Halftone Dots", 0.6f, 0f, 1f),
        EffectDefinition("vfx_dream", "Ethereal Dream Glow", "Stylized", "Hazy romantic blooming highlights", "Bloom", 0.65f, 0f, 1f),

        // Motion
        EffectDefinition("vfx_camera_shake", "Handheld Camera Shake", "Motion", "Realistic cinematic documentary camera sway", "Instability", 0.5f, 0f, 1f),
        EffectDefinition("vfx_pulse_beat", "Bass Pulse Zoom", "Motion", "Rhythmic impact zoom beat reaction", "Beat Scale", 0.6f, 0f, 1f),
        EffectDefinition("vfx_spin_impact", "Rotational Spin Whip", "Motion", "Rapid kinetic whip transition movement", "Spin Rate", 0.7f, 0f, 1f)
    )
}
