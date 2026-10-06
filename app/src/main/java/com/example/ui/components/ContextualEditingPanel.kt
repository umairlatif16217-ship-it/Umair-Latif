package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AudioEffectLibrary
import com.example.engine.CaptionLanguage
import com.example.engine.EffectDefinition
import com.example.engine.VisualEffectsLibrary
import com.example.model.ChromaKeyConfig
import com.example.model.Clip
import com.example.model.ColorAdjustments
import com.example.model.MaskConfig
import com.example.model.MaskType
import com.example.model.AdvancedAudioMix
import com.example.model.AudioCompressorConfig
import com.example.model.AudioLimiterConfig
import com.example.model.EqualizerBand
import com.example.viewmodel.EditorTab

@Composable
fun ContextualEditingPanel(
    selectedClip: Clip?,
    activeTab: EditorTab,
    isProMode: Boolean,
    onTabSelected: (EditorTab) -> Unit,
    // Basic transforms
    onSetSpeed: (Float) -> Unit,
    onRotate: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    // Keyframes
    onAddKeyframe: () -> Unit,
    onUpdateKeyframe: (Float, Float, Float, Float, Float) -> Unit,
    // Mask
    onUpdateMask: (MaskConfig) -> Unit,
    // Chroma
    onUpdateChromaKey: (ChromaKeyConfig) -> Unit,
    // Effects preview & apply
    onPreviewEffect: (String?, Float) -> Unit,
    onToggleEffect: (String, String) -> Unit,
    // Color
    onUpdateColor: (ColorAdjustments) -> Unit,
    // Audio & Voice Enhancer
    onToggleAudioEffect: (String) -> Unit,
    onSetVoiceEnhancer: (String?) -> Unit,
    onUpdateAudioMix: (AdvancedAudioMix) -> Unit,
    // Captions & Silence
    onGenerateCaptions: (CaptionLanguage) -> Unit,
    onAutoSilenceCut: () -> Unit,
    // Live Transcription / Speech input
    onAddText: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF131924))
    ) {
        // Tab Navigation Bar
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            edgePadding = 12.dp,
            containerColor = Color(0xFF161E2E),
            contentColor = Color(0xFF38BDF8)
        ) {
            val tabs = if (isProMode) {
                EditorTab.values()
            } else {
                arrayOf(EditorTab.BASIC, EditorTab.EFFECTS, EditorTab.COLOR, EditorTab.AUDIO, EditorTab.CAPTIONS, EditorTab.TEXT)
            }

            tabs.forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                EditorTab.BASIC -> "Basic"
                                EditorTab.KEYFRAMES -> "Keyframes"
                                EditorTab.MASKS -> "Masks"
                                EditorTab.CHROMA -> "Chroma Key"
                                EditorTab.EFFECTS -> "Visual VFX"
                                EditorTab.COLOR -> "Color Grade"
                                EditorTab.AUDIO -> "100+ Audio FX"
                                EditorTab.AUDIO_MIX -> "Pro Mixer (EQ/Comp/Limiter)"
                                EditorTab.VOICE_ENHANCER -> "Voice Enhancer"
                                EditorTab.CAPTIONS -> "Auto Captions"
                                EditorTab.TEXT -> "Text & Style"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Active Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(12.dp)
        ) {
            when (activeTab) {
                EditorTab.BASIC -> BasicToolsPanel(
                    selectedClip = selectedClip,
                    onSetSpeed = onSetSpeed,
                    onRotate = onRotate,
                    onFlipH = onFlipHorizontal,
                    onFlipV = onFlipVertical,
                    onVolume = onVolumeChange,
                    onSilenceCut = onAutoSilenceCut
                )
                EditorTab.KEYFRAMES -> KeyframesPanel(
                    selectedClip = selectedClip,
                    onAddKeyframe = onAddKeyframe,
                    onUpdate = onUpdateKeyframe
                )
                EditorTab.MASKS -> MasksPanel(
                    selectedClip = selectedClip,
                    onUpdateMask = onUpdateMask
                )
                EditorTab.CHROMA -> ChromaKeyPanel(
                    selectedClip = selectedClip,
                    onUpdateChroma = onUpdateChromaKey
                )
                EditorTab.EFFECTS -> VisualEffectsPanel(
                    selectedClip = selectedClip,
                    onPreviewEffect = onPreviewEffect,
                    onToggleEffect = onToggleEffect
                )
                EditorTab.COLOR -> ColorGradingPanel(
                    selectedClip = selectedClip,
                    onUpdateColor = onUpdateColor
                )
                EditorTab.AUDIO -> AudioEffectsPanel(
                    selectedClip = selectedClip,
                    onToggleEffect = onToggleAudioEffect
                )
                EditorTab.AUDIO_MIX -> AdvancedAudioMixerPanel(
                    selectedClip = selectedClip,
                    onUpdateMix = onUpdateAudioMix
                )
                EditorTab.VOICE_ENHANCER -> VoiceEnhancerPanel(
                    selectedClip = selectedClip,
                    onSetVoiceEnhancer = onSetVoiceEnhancer
                )
                EditorTab.CAPTIONS -> CaptionsPanel(
                    onGenerateCaptions = onGenerateCaptions,
                    onAutoSilenceCut = onAutoSilenceCut
                )
                EditorTab.TEXT -> TextPanel(
                    onAddText = onAddText
                )
            }
        }
    }
}

@Composable
private fun BasicToolsPanel(
    selectedClip: Clip?,
    onSetSpeed: (Float) -> Unit,
    onRotate: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onVolume: (Float) -> Unit,
    onSilenceCut: () -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Speed Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.padding(2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Speed: ${selectedClip?.speed ?: 1.0}x", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f, 4.0f).forEach { spd ->
                        Surface(
                            color = if (selectedClip?.speed == spd) Color(0xFF38BDF8) else Color(0xFF334155),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onSetSpeed(spd) }
                        ) {
                            Text(
                                text = "${spd}x",
                                color = if (selectedClip?.speed == spd) Color.Black else Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Transforms
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.padding(2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Transform", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onRotate, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("90°", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = onFlipH, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Flip H", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = onFlipV, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Flip V", fontSize = 11.sp)
                    }
                }
            }
        }

        // Auto Silence Cut
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.3f)),
            modifier = Modifier.padding(2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("AI Smart Cut", color = Color(0xFF2DD4BF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onSilenceCut,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto Silence Cut", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun VisualEffectsPanel(
    selectedClip: Clip?,
    onPreviewEffect: (String?, Float) -> Unit,
    onToggleEffect: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Color & Grading") }
    var previewingEffect by remember { mutableStateOf<EffectDefinition?>(null) }
    var effectIntensity by remember { mutableFloatStateOf(0.7f) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VisualEffectsLibrary.categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF38BDF8),
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Effects List in category
        val effects = VisualEffectsLibrary.allEffects.filter { it.category == selectedCategory }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(effects) { fx ->
                val isApplied = selectedClip?.effects?.any { it.name == fx.name } == true
                val isPreviewing = previewingEffect?.id == fx.id

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isApplied) Color(0xFF0284C7) else if (isPreviewing) Color(0xFF7C3AED) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .width(130.dp)
                        .clickable {
                            previewingEffect = fx
                            onPreviewEffect(fx.name, effectIntensity)
                        }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(fx.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(fx.description, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    onToggleEffect(fx.name, fx.category)
                                    onPreviewEffect(null, 0f)
                                    previewingEffect = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isApplied) Color(0xFFFF5252) else Color(0xFF10B981)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(if (isApplied) "Remove" else "Apply", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Slider for real-time preview if selecting an effect
        previewingEffect?.let { fx ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("${fx.paramName}: ${(effectIntensity * 100).toInt()}%", color = Color(0xFF38BDF8), fontSize = 11.sp)
                Slider(
                    value = effectIntensity,
                    onValueChange = {
                        effectIntensity = it
                        onPreviewEffect(fx.name, it)
                    },
                    valueRange = fx.minParam..fx.maxParam,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyframesPanel(
    selectedClip: Clip?,
    onAddKeyframe: () -> Unit,
    onUpdate: (Float, Float, Float, Float, Float) -> Unit
) {
    var posX by remember { mutableFloatStateOf(selectedClip?.positionX ?: 0f) }
    var posY by remember { mutableFloatStateOf(selectedClip?.positionY ?: 0f) }
    var scale by remember { mutableFloatStateOf(selectedClip?.scale ?: 1.0f) }
    var rotation by remember { mutableFloatStateOf(selectedClip?.rotation ?: 0f) }
    var opacity by remember { mutableFloatStateOf(selectedClip?.opacity ?: 1.0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Keyframe Interpolation Engine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddKeyframe,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Keyframe", fontSize = 11.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Scale: ${(scale * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Slider(
                    value = scale,
                    onValueChange = { scale = it; onUpdate(posX, posY, scale, rotation, opacity) },
                    valueRange = 0.5f..2.5f
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Opacity: ${(opacity * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it; onUpdate(posX, posY, scale, rotation, opacity) },
                    valueRange = 0f..1.0f
                )
            }
        }
    }
}

@Composable
private fun MasksPanel(
    selectedClip: Clip?,
    onUpdateMask: (MaskConfig) -> Unit
) {
    var maskType by remember { mutableStateOf(selectedClip?.mask?.type ?: MaskType.NONE) }
    var feather by remember { mutableFloatStateOf(selectedClip?.mask?.feather ?: 10f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Precision Masks", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(MaskType.NONE, MaskType.CIRCLE, MaskType.RECTANGLE, MaskType.LINEAR_GRADIENT).forEach { m ->
                FilterChip(
                    selected = maskType == m,
                    onClick = {
                        maskType = m
                        onUpdateMask(MaskConfig(type = m, feather = feather))
                    },
                    label = { Text(m.name, fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mask Feather / Soft Edge", color = Color(0xFF94A3B8), fontSize = 10.sp)
        Slider(
            value = feather,
            onValueChange = {
                feather = it
                onUpdateMask(MaskConfig(type = maskType, feather = it))
            },
            valueRange = 0f..50f
        )
    }
}

@Composable
private fun ChromaKeyPanel(
    selectedClip: Clip?,
    onUpdateChroma: (ChromaKeyConfig) -> Unit
) {
    var isEnabled by remember { mutableStateOf(selectedClip?.chromaKey?.enabled ?: false) }
    var tolerance by remember { mutableFloatStateOf(selectedClip?.chromaKey?.tolerance ?: 0.25f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chroma Key (Green Screen Removal)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Switch(
                checked = isEnabled,
                onCheckedChange = {
                    isEnabled = it
                    onUpdateChroma(ChromaKeyConfig(enabled = it, tolerance = tolerance))
                }
            )
        }

        if (isEnabled) {
            Text("Keying Color Tolerance: ${(tolerance * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Slider(
                value = tolerance,
                onValueChange = {
                    tolerance = it
                    onUpdateChroma(ChromaKeyConfig(enabled = isEnabled, tolerance = it))
                },
                valueRange = 0.05f..0.8f
            )
        }
    }
}

@Composable
private fun ColorGradingPanel(
    selectedClip: Clip?,
    onUpdateColor: (ColorAdjustments) -> Unit
) {
    var exposure by remember { mutableFloatStateOf(selectedClip?.colorAdjustments?.exposure ?: 0f) }
    var contrast by remember { mutableFloatStateOf(selectedClip?.colorAdjustments?.contrast ?: 1f) }
    var saturation by remember { mutableFloatStateOf(selectedClip?.colorAdjustments?.saturation ?: 1f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Hollywood Color Grading (LUT & Pro Curves)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Exposure", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Slider(
                    value = exposure,
                    onValueChange = { exposure = it; onUpdateColor(ColorAdjustments(exposure = exposure, contrast = contrast, saturation = saturation)) },
                    valueRange = -1f..1f
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Contrast", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Slider(
                    value = contrast,
                    onValueChange = { contrast = it; onUpdateColor(ColorAdjustments(exposure = exposure, contrast = contrast, saturation = saturation)) },
                    valueRange = 0.5f..2.0f
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Saturation", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it; onUpdateColor(ColorAdjustments(exposure = exposure, contrast = contrast, saturation = saturation)) },
                    valueRange = 0f..2.0f
                )
            }
        }
    }
}

@Composable
private fun AudioEffectsPanel(
    selectedClip: Clip?,
    onToggleEffect: (String) -> Unit
) {
    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        Text("100+ Modular Studio Audio Effects", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AudioEffectLibrary.allPresets.forEach { preset ->
                val isApplied = selectedClip?.audioEffects?.contains(preset.name) == true
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isApplied) Color(0xFF059669) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .width(135.dp)
                        .clickable { onToggleEffect(preset.name) }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(preset.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(preset.category, color = Color(0xFF38BDF8), fontSize = 9.sp)
                        Text(preset.description, color = Color(0xFF94A3B8), fontSize = 8.sp, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceEnhancerPanel(
    selectedClip: Clip?,
    onSetVoiceEnhancer: (String?) -> Unit
) {
    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        Text("AI Studio Voice Enhancer (Natural Speech Preservation)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AudioEffectLibrary.voiceEnhancerModes.forEach { (mode, desc) ->
                val isSelected = selectedClip?.voiceEnhancerPreset == mode
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onSetVoiceEnhancer(if (isSelected) null else mode) }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(mode, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 3)
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptionsPanel(
    onGenerateCaptions: (CaptionLanguage) -> Unit,
    onAutoSilenceCut: () -> Unit
) {
    var selectedLang by remember { mutableStateOf(CaptionLanguage.ENGLISH) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Automatic Speech Recognition & Captions", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(CaptionLanguage.ENGLISH, CaptionLanguage.ARABIC, CaptionLanguage.URDU, CaptionLanguage.HINDI, CaptionLanguage.SPANISH).forEach { lang ->
                FilterChip(
                    selected = selectedLang == lang,
                    onClick = { selectedLang = lang },
                    label = { Text(lang.displayName.take(12), fontSize = 10.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = { onGenerateCaptions(selectedLang) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate Auto Captions (${selectedLang.displayName})", fontSize = 12.sp)
        }
    }
}

@Composable
private fun TextPanel(
    onAddText: (String, Boolean) -> Unit
) {
    var textInput by remember { mutableStateOf("RU ediTOR 4K") }
    var isRtl by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Rich Subtitles & Animated Typography", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Caption / Title Text") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onAddText(textInput, isRtl) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("Add Text")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = isRtl, onCheckedChange = { isRtl = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("RTL Mode (Arabic / Urdu Typography)", color = Color.White, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AdvancedAudioMixerPanel(
    selectedClip: Clip?,
    onUpdateMix: (AdvancedAudioMix) -> Unit
) {
    var selectedToolSection by remember { mutableStateOf("Equalizer") }
    val currentMix = selectedClip?.advancedAudioMix ?: AdvancedAudioMix()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Advanced Studio Mixing Bus", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Equalizer", "Compressor", "Limiter").forEach { sec ->
                    FilterChip(
                        selected = selectedToolSection == sec,
                        onClick = { selectedToolSection = sec },
                        label = { Text(sec, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (selectedToolSection) {
            "Equalizer" -> {
                // 5-band graphic equalizer
                val bands = currentMix.equalizerBands
                val scroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scroll),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bands.forEachIndexed { index, band ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.width(105.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Text(band.frequencyLabel, fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                Text("${band.gainDb.toInt()} dB", fontSize = 9.sp, color = Color.White)
                                Slider(
                                    value = band.gainDb,
                                    onValueChange = { newGain ->
                                        val updatedBands = bands.toMutableList()
                                        updatedBands[index] = band.copy(gainDb = newGain)
                                        onUpdateMix(currentMix.copy(equalizerBands = updatedBands))
                                    },
                                    valueRange = -12f..12f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF10B981),
                                        activeTrackColor = Color(0xFF10B981)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            "Compressor" -> {
                // Compressor with threshold, ratio, attack, release
                val comp = currentMix.compressor
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dynamic Range Compressor", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Switch(
                            checked = comp.enabled,
                            onCheckedChange = {
                                onUpdateMix(currentMix.copy(compressor = comp.copy(enabled = it)))
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Threshold: ${comp.thresholdDb.toInt()} dB", fontSize = 10.sp, color = Color.White)
                            Slider(
                                value = comp.thresholdDb,
                                onValueChange = { onUpdateMix(currentMix.copy(compressor = comp.copy(thresholdDb = it))) },
                                valueRange = -60f..0f
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ratio: ${comp.ratio.toInt()}:1", fontSize = 10.sp, color = Color.White)
                            Slider(
                                value = comp.ratio,
                                onValueChange = { onUpdateMix(currentMix.copy(compressor = comp.copy(ratio = it))) },
                                valueRange = 1f..20f
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Attack: ${comp.attackMs.toInt()} ms", fontSize = 10.sp, color = Color.White)
                            Slider(
                                value = comp.attackMs,
                                onValueChange = { onUpdateMix(currentMix.copy(compressor = comp.copy(attackMs = it))) },
                                valueRange = 1f..150f
                            )
                        }
                    }
                }
            }
            "Limiter" -> {
                // Peak Brickwall Limiter
                val lim = currentMix.limiter
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("True Peak Brickwall Limiter", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Switch(
                            checked = lim.enabled,
                            onCheckedChange = {
                                onUpdateMix(currentMix.copy(limiter = lim.copy(enabled = it)))
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ceiling: ${String.format("%.1f", lim.ceilingDb)} dBFS", fontSize = 10.sp, color = Color.White)
                            Slider(
                                value = lim.ceilingDb,
                                onValueChange = { onUpdateMix(currentMix.copy(limiter = lim.copy(ceilingDb = it))) },
                                valueRange = -12f..0f
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Release: ${lim.releaseMs.toInt()} ms", fontSize = 10.sp, color = Color.White)
                            Slider(
                                value = lim.releaseMs,
                                onValueChange = { onUpdateMix(currentMix.copy(limiter = lim.copy(releaseMs = it))) },
                                valueRange = 10f..300f
                            )
                        }
                    }
                }
            }
        }
    }
}

