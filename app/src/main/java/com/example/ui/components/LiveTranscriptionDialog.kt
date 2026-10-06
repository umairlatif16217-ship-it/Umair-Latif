package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LiveTranscriptionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTranscriptionDialog(
    state: LiveTranscriptionState,
    onStartListening: (String) -> Unit,
    onStopListening: () -> Unit,
    onClearText: () -> Unit,
    onAddToTimelineAsCaptions: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedLanguageCode by remember { mutableStateOf("en-US") }

    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(state.isListening) {
        if (state.isListening) {
            pulseScale.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (state.isListening) onStopListening()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFF0F141C),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Live Real-Time Audio Transcription",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time speech to synchronized subtitle stream",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                IconButton(onClick = {
                    if (state.isListening) onStopListening()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8B949E))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language Selector
            Text("INPUT SPEECH LANGUAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B949E))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "en-US" to "English (US)",
                    "ar-SA" to "العربية (Arabic)",
                    "ur-PK" to "اردو (Urdu)",
                    "es-ES" to "Español",
                    "hi-IN" to "हिन्दी"
                ).forEach { (code, name) ->
                    FilterChip(
                        selected = selectedLanguageCode == code,
                        onClick = {
                            selectedLanguageCode = code
                            if (state.isListening) {
                                onStartListening(code)
                            }
                        },
                        label = { Text(name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Listening Pulse / Mic Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(if (state.isListening) pulseScale.value else 1f)
                            .background(
                                if (state.isListening) Color(0xFFFF3366) else Color(0xFF334155),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (state.isListening) onStopListening()
                                else onStartListening(selectedLanguageCode)
                            },
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (state.isListening) "Listening Live... Speak now" else "Tap Microphone to Start Live Transcribing",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isListening) Color(0xFFFF5252) else Color(0xFF94A3B8)
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.error,
                            fontSize = 11.sp,
                            color = Color(0xFFFF5252)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Output Text Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LIVE TRANSCRIPT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        if (state.recognizedText.isNotBlank()) {
                            IconButton(onClick = onClearText, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val fullDisplay = buildString {
                        append(state.recognizedText)
                        if (state.partialText.isNotBlank()) {
                            if (isNotEmpty()) append(" ")
                            append("[${state.partialText}]")
                        }
                    }

                    Text(
                        text = if (fullDisplay.isBlank()) "Transcription stream will appear here in real time..." else fullDisplay,
                        color = if (fullDisplay.isBlank()) Color(0xFF64748B) else Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Add directly onto timeline
            Button(
                onClick = {
                    val textToPush = state.recognizedText.ifBlank { state.partialText }
                    if (textToPush.isNotBlank()) {
                        onAddToTimelineAsCaptions(textToPush, selectedLanguageCode)
                        onDismiss()
                    }
                },
                enabled = state.recognizedText.isNotBlank() || state.partialText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Transcribed Text as Timeline Subtitles", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
