package com.example.engine

data class AudioEffectPreset(
    val name: String,
    val category: String, // Space/Reverb, Voice Pitch/Character, EQ/Tone, Dynamics/Vintage, Utility
    val description: String,
    val pitchShift: Float = 0f, // semitones (-12 to +12)
    val echoDelayMs: Int = 0,
    val echoFeedback: Float = 0f,
    val reverbWet: Float = 0f,
    val highCutHz: Float = 20000f,
    val lowCutHz: Float = 20f,
    val bassBoostDb: Float = 0f,
    val trebleBoostDb: Float = 0f,
    val distortion: Float = 0f,
    val voiceClarityDb: Float = 0f
)

object AudioEffectLibrary {

    val allPresets: List<AudioEffectPreset> = listOf(
        // Reverb & Ambience
        AudioEffectPreset("Echo", "Space & Reverb", "Classic temporal echo reflection", echoDelayMs = 250, echoFeedback = 0.4f),
        AudioEffectPreset("Reverb", "Space & Reverb", "Smooth acoustic natural reverberation", reverbWet = 0.5f),
        AudioEffectPreset("Studio", "Space & Reverb", "Acoustically treated dry vocal studio", reverbWet = 0.1f, voiceClarityDb = 3f),
        AudioEffectPreset("Room", "Space & Reverb", "Natural medium living room ambience", reverbWet = 0.35f),
        AudioEffectPreset("Hall", "Space & Reverb", "Large wooden symphonic hall ambience", reverbWet = 0.65f),
        AudioEffectPreset("Cathedral", "Space & Reverb", "Grand stone cathedral massive reflection", reverbWet = 0.85f),
        AudioEffectPreset("Cave", "Space & Reverb", "Deep cavern hollow echoes", echoDelayMs = 450, echoFeedback = 0.6f, reverbWet = 0.7f),
        AudioEffectPreset("Tunnel", "Space & Reverb", "Long subterranean concrete tunnel reverb", echoDelayMs = 320, echoFeedback = 0.5f, highCutHz = 5000f),
        AudioEffectPreset("Bathroom", "Space & Reverb", "Tiled bright slapback reflections", echoDelayMs = 60, echoFeedback = 0.4f, trebleBoostDb = 4f),
        AudioEffectPreset("Underground", "Space & Reverb", "Damped deep underground chamber", reverbWet = 0.6f, highCutHz = 4000f, bassBoostDb = 3f),
        AudioEffectPreset("Mountain", "Space & Reverb", "High altitude distant mountain peak echo", echoDelayMs = 700, echoFeedback = 0.7f),
        AudioEffectPreset("Space", "Space & Reverb", "Infinite cosmic space shimmer", echoDelayMs = 500, echoFeedback = 0.65f, reverbWet = 0.9f),
        AudioEffectPreset("Stadium", "Space & Reverb", "Massive open arena sports stadium crowd echo", echoDelayMs = 380, echoFeedback = 0.55f, reverbWet = 0.75f),
        AudioEffectPreset("Long Reverb", "Space & Reverb", "Extended 8-second tail decay", reverbWet = 0.95f),
        AudioEffectPreset("Short Reverb", "Space & Reverb", "Tight punchy 0.5-second ambience", reverbWet = 0.25f),
        AudioEffectPreset("Slapback", "Space & Reverb", "Vintage 50s rockabilly single tape slap", echoDelayMs = 110, echoFeedback = 0.2f),
        AudioEffectPreset("Slow Echo", "Space & Reverb", "Spacious 600ms delayed repetitions", echoDelayMs = 600, echoFeedback = 0.5f),
        AudioEffectPreset("Fast Echo", "Space & Reverb", "Rapid flutter ping reflections", echoDelayMs = 90, echoFeedback = 0.45f),

        // Voice Pitch & Character
        AudioEffectPreset("Deep Voice", "Character & Pitch", "Resonant low-pitched cinematic vocal", pitchShift = -5f, bassBoostDb = 5f),
        AudioEffectPreset("High Voice", "Character & Pitch", "Bright high-pitched upbeat vocal", pitchShift = 6f, trebleBoostDb = 3f),
        AudioEffectPreset("Robot", "Character & Pitch", "Metallic ring-modulated android speech", pitchShift = -2f, distortion = 0.35f, highCutHz = 4500f),
        AudioEffectPreset("Monster", "Character & Pitch", "Guttural demonic low beast vocal", pitchShift = -9f, distortion = 0.4f, bassBoostDb = 7f),
        AudioEffectPreset("Alien", "Character & Pitch", "Extraterrestrial frequency modulated voice", pitchShift = 4f, echoDelayMs = 120, echoFeedback = 0.5f),
        AudioEffectPreset("Child-like", "Character & Pitch", "Gentle higher cheerful register", pitchShift = 4.5f, trebleBoostDb = 2f),
        AudioEffectPreset("Whisper", "Character & Pitch", "Soft close-mic dynamic vocal presence", highCutHz = 8000f, lowCutHz = 300f, voiceClarityDb = 5f),
        AudioEffectPreset("Double Voice", "Character & Pitch", "Dual tracked thick vocal unison", echoDelayMs = 35, echoFeedback = 0.25f),
        AudioEffectPreset("Whisper Echo", "Character & Pitch", "Ethereal whispered delay tails", echoDelayMs = 300, echoFeedback = 0.45f, highCutHz = 6000f),
        AudioEffectPreset("Megaphone", "Character & Pitch", "Horn loudspeaker punchy midrange distortion", lowCutHz = 500f, highCutHz = 3500f, distortion = 0.45f),
        AudioEffectPreset("Telephone", "Character & Pitch", "Classic narrow band telephone line filter", lowCutHz = 400f, highCutHz = 3000f, distortion = 0.2f),
        AudioEffectPreset("Radio", "Character & Pitch", "Over-the-air communication transmission", lowCutHz = 350f, highCutHz = 4000f, distortion = 0.15f),
        AudioEffectPreset("Walkie-talkie", "Character & Pitch", "Tactical handheld radio squelch band", lowCutHz = 600f, highCutHz = 2800f, distortion = 0.5f),

        // Tonal & EQ
        AudioEffectPreset("Bass Boost", "EQ & Tone", "Sub-bass low end power amplifier", bassBoostDb = 8f),
        AudioEffectPreset("Treble Boost", "EQ & Tone", "Airy high-frequency shimmer and presence", trebleBoostDb = 7f),
        AudioEffectPreset("Vocal Boost", "EQ & Tone", "Lead vocal forward center push", voiceClarityDb = 6f, trebleBoostDb = 3f),
        AudioEffectPreset("Vocal Clarity", "EQ & Tone", "De-mudded transparent dialogue intelligibility", lowCutHz = 120f, voiceClarityDb = 5f, trebleBoostDb = 4f),
        AudioEffectPreset("Vocal Presence", "EQ & Tone", "In-your-face upfront intimate vocal", voiceClarityDb = 7f),
        AudioEffectPreset("Warm Voice", "EQ & Tone", "Velvety vintage tube microphone warmth", bassBoostDb = 3.5f, highCutHz = 14000f),
        AudioEffectPreset("Bright Voice", "EQ & Tone", "Crisp modern condenser mic sheen", trebleBoostDb = 5.5f, lowCutHz = 100f),
        AudioEffectPreset("Dark Voice", "EQ & Tone", "Heavy subdued moody low profile", highCutHz = 4000f, bassBoostDb = 4f),
        AudioEffectPreset("Podcast Voice", "EQ & Tone", "Broadcast standard broadcast microphone leveling", lowCutHz = 90f, voiceClarityDb = 4f, bassBoostDb = 2.5f),
        AudioEffectPreset("Broadcast Voice", "EQ & Tone", "Full FM radio presenter richness and warmth", lowCutHz = 80f, voiceClarityDb = 5f, bassBoostDb = 4f, trebleBoostDb = 3f),
        AudioEffectPreset("Cinematic Voice", "EQ & Tone", "Hollywood trailer narrator deep presence", pitchShift = -1.5f, bassBoostDb = 6f, voiceClarityDb = 4f),

        // Vintage & Stylized
        AudioEffectPreset("Vintage", "Vintage & Lo-Fi", "Warm analogue magnetic tape saturation", distortion = 0.25f, highCutHz = 8000f, lowCutHz = 80f),
        AudioEffectPreset("Old Recording", "Vintage & Lo-Fi", "Gramophone 78 RPM scratchy bandwidth", lowCutHz = 300f, highCutHz = 3800f, distortion = 0.35f),
        AudioEffectPreset("VHS", "Vintage & Lo-Fi", "1980s magnetic videocassette tracking wobble", highCutHz = 6500f, distortion = 0.2f),
        AudioEffectPreset("Lo-fi", "Vintage & Lo-Fi", "Relaxed chilled lo-fi study beat vibe", highCutHz = 5000f, lowCutHz = 150f, distortion = 0.15f),
        AudioEffectPreset("AM Radio", "Vintage & Lo-Fi", "Heterodyne amplitude modulation carrier band", lowCutHz = 400f, highCutHz = 3200f, distortion = 0.3f),
        AudioEffectPreset("FM Radio", "Vintage & Lo-Fi", "Commercial broadcast stereo curve", trebleBoostDb = 4f, bassBoostDb = 3f),
        AudioEffectPreset("Underwater", "Vintage & Lo-Fi", "Submerged low-pass muffled aquatic feel", highCutHz = 750f, lowCutHz = 40f, reverbWet = 0.5f),
        AudioEffectPreset("Metallic", "Vintage & Lo-Fi", "High harmonic comb-filter metallic resonance", echoDelayMs = 25, echoFeedback = 0.7f, trebleBoostDb = 6f),

        // Modulation & Creative
        AudioEffectPreset("Chorus", "Modulation", "Lush multi-voice ensemble stereo width", echoDelayMs = 28, echoFeedback = 0.3f),
        AudioEffectPreset("Flanger", "Modulation", "Sweeping jet-engine comb filter sweep", echoDelayMs = 12, echoFeedback = 0.6f),
        AudioEffectPreset("Phaser", "Modulation", "Psychedelic swooshing phase cancellation", echoDelayMs = 18, echoFeedback = 0.45f),
        AudioEffectPreset("Distortion", "Modulation", "Hard clipped electric guitar overdrive", distortion = 0.75f, bassBoostDb = 4f),
        AudioEffectPreset("Overdrive", "Modulation", "Smooth warm valve saturation push", distortion = 0.45f),
        AudioEffectPreset("Ping-pong Delay", "Modulation", "Alternating left-right stereo rhythmic bounces", echoDelayMs = 340, echoFeedback = 0.55f),
        AudioEffectPreset("Stereo Widening", "Modulation", "Expanded holographic acoustic soundstage", echoDelayMs = 15, echoFeedback = 0.2f),
        AudioEffectPreset("Dream", "Modulation", "Weightless floating surreal ambient wash", reverbWet = 0.85f, echoDelayMs = 400, echoFeedback = 0.5f, highCutHz = 9000f)
    )

    // Dedicated Voice Enhancer 1-tap modes
    val voiceEnhancerModes = listOf(
        "Clean Voice" to "Removes hum, hiss and rumble while keeping speech completely natural",
        "Podcast" to "Warm low-end proximity effect with crisp dialogue intelligibility",
        "Studio" to "Treated vocal booth sound with controlled dynamic compression",
        "Interview" to "Optimized speech clarity for field recordings and conversational speech",
        "Phone Recording Fix" to "Restores restricted smartphone audio to full-bodied richness",
        "Outdoor Voice" to "Cuts wind rumble and environmental outdoor ambience",
        "Noisy Environment" to "Aggressive multi-band noise reduction with speech preservation",
        "Deep Voice Enhancement" to "Accentuates masculine chest resonance without muddiness",
        "Female Voice Enhancement" to "Silky smooth high frequencies without harsh sibilance",
        "Arabic Voice" to "Fine-tuned for emphatic guttural and emphatic consonant articulation",
        "English Voice" to "Balanced studio standard for crisp English dialogue clarity"
    )
}
