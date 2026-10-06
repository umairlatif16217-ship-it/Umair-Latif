package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DownloadableQuality
import com.example.engine.MediaInspectionResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDownloaderSheet(
    url: String,
    inspection: MediaInspectionResult?,
    progress: Pair<Float, String>?,
    startTimeSec: Long,
    endTimeSec: Long,
    onUrlChange: (String) -> Unit,
    onInspectUrl: () -> Unit,
    onSetRange: (Long, Long) -> Unit,
    onStartDownload: (DownloadableQuality) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedQuality by remember { mutableStateOf<DownloadableQuality?>(null) }
    var sliderRange by remember(inspection) {
        val total = (inspection?.durationSeconds ?: 185L).toFloat()
        mutableStateOf(0f..total)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                        text = "Public Stream Downloader",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Supports precise time-segment trimming (e.g. 00:01:20 to 00:02:45)",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8B949E))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // URL Input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("Paste Public Media URL (MP4 / Stream)") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onInspectUrl,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Inspect")
                }
            }

            // Inspection Details
            if (inspection != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(inspection.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Duration: ${formatSec(inspection.durationSeconds)} • Source: ${inspection.author}", fontSize = 11.sp, color = Color(0xFF94A3B8))

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segment Trimmer Slider
                        Text(
                            text = "TRIM DOWNLOAD RANGE (Manual Duration)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start: ${formatSec(sliderRange.start.toLong())}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                            Text("End: ${formatSec(sliderRange.endInclusive.toLong())}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                        }

                        RangeSlider(
                            value = sliderRange,
                            onValueChange = { range ->
                                sliderRange = range
                                onSetRange(range.start.toLong(), range.endInclusive.toLong())
                            },
                            valueRange = 0f..(inspection.durationSeconds.toFloat()),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Downloading only ${formatSec((sliderRange.endInclusive - sliderRange.start).toLong())} segment",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quality Selector
                Text("AVAILABLE QUALITIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    inspection.qualities.forEach { q ->
                        val isSel = selectedQuality == q
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF0369A1) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQuality = q }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(q.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("${q.resolution} • Codec: ${q.codec}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Text(
                                    "~${q.estimatedSizeBytes / (1024 * 1024)} MB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }

                // Progress Bar
                if (progress != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progress.first },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(progress.second, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val q = selectedQuality ?: inspection.qualities.first()
                        onStartDownload(q)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Trimmed Segment", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatSec(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
