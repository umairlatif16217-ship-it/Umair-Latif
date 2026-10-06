package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DeviceCodecSupport
import com.example.engine.ExportProgress
import com.example.model.ExportCodec
import com.example.model.ExportFps
import com.example.model.ExportQualityPreset
import com.example.model.ExportResolution
import com.example.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealExportDialog(
    project: Project?,
    deviceCapabilities: DeviceCodecSupport,
    exportProgress: ExportProgress?,
    isExporting: Boolean,
    onStartExport: (ExportResolution, ExportFps, ExportCodec, ExportQualityPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedResolution by remember { mutableStateOf(ExportResolution.RES_4K) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_60) }
    var selectedCodec by remember { mutableStateOf(ExportCodec.H265) }
    var selectedQuality by remember { mutableStateOf(ExportQualityPreset.HIGH) }

    // Calculate realistic output specs
    val targetW = if (selectedResolution == ExportResolution.RES_4K) 3840 else 1920
    val targetH = if (selectedResolution == ExportResolution.RES_4K) 2160 else 1080
    val durationSec = (project?.durationMs ?: 6000L) / 1000f
    val estBitrateMbps = when (selectedResolution) {
        ExportResolution.RES_4K -> 50f * selectedQuality.bitrateMultiplier
        ExportResolution.RES_2K -> 28f * selectedQuality.bitrateMultiplier
        ExportResolution.RES_1080P -> 14f * selectedQuality.bitrateMultiplier
        else -> 6f * selectedQuality.bitrateMultiplier
    }
    val estFileSizeBytes = ((estBitrateMbps * 1_000_000f / 8f) * durationSec).toLong()
    val estFileSizeMb = estFileSizeBytes / (1024 * 1024)

    ModalBottomSheet(
        onDismissRequest = { if (!isExporting) onDismiss() },
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
                        text = "Real 4K Master Export",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Hardware Accelerated • No Watermarks • True 3840×2160",
                        fontSize = 11.sp,
                        color = Color(0xFF00E5FF)
                    )
                }

                if (!isExporting) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8B949E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // If Exporting: Show Real Progress Pipeline
            if (isExporting && exportProgress != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (exportProgress.isComplete) "🎉 Export Complete!" else "Encoding Project Video",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { exportProgress.percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${(exportProgress.percentage * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Frame ${exportProgress.currentFrame} / ${exportProgress.totalFrames}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF8B949E)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = exportProgress.statusText,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )

                        if (exportProgress.isComplete) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done & View Saved Video")
                            }
                        }
                    }
                }
            } else {
                // Resolution Selector
                Text("RESOLUTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ExportResolution.RES_4K, ExportResolution.RES_2K, ExportResolution.RES_1080P, ExportResolution.RES_720P).forEach { res ->
                        FilterChip(
                            selected = selectedResolution == res,
                            onClick = { selectedResolution = res },
                            label = {
                                Text(
                                    text = if (res == ExportResolution.RES_4K) "4K UHD" else if (res == ExportResolution.RES_2K) "2K QHD" else if (res == ExportResolution.RES_1080P) "1080p FHD" else "720p HD",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Frame Rate (FPS)
                Text("FRAME RATE (FPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ExportFps.FPS_60, ExportFps.FPS_30, ExportFps.FPS_24).forEach { fps ->
                        FilterChip(
                            selected = selectedFps == fps,
                            onClick = { selectedFps = fps },
                            label = { Text("${fps.fps} fps", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF38BDF8),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hardware Codec
                Text("HARDWARE CODEC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ExportCodec.H265, ExportCodec.H264, ExportCodec.AV1).forEach { c ->
                        FilterChip(
                            selected = selectedCodec == c,
                            onClick = { selectedCodec = c },
                            label = { Text(c.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF818CF8),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quality Profile
                Text("BITRATE & QUALITY PRESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportQualityPreset.values().forEach { q ->
                        FilterChip(
                            selected = selectedQuality == q,
                            onClick = { selectedQuality = q },
                            label = { Text(q.label.take(8), fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estimated Metrics Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Estimated File Size:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("~${estFileSizeMb.coerceAtLeast(12)} MB", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Target Output Resolution:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${targetW}×${targetH}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Target Bitrate:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${estBitrateMbps.toInt()} Mbps", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Export Button
                Button(
                    onClick = {
                        onStartExport(selectedResolution, selectedFps, selectedCodec, selectedQuality)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_start_real_export")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export at True ${if (selectedResolution == ExportResolution.RES_4K) "4K (3840×2160)" else "${targetW}×${targetH}"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
