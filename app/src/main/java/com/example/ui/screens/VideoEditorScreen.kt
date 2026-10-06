package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccessShortcut
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CaptionLanguage
import com.example.engine.LiveAudioTranscriber
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.ui.components.ContextualEditingPanel
import com.example.ui.components.LiveTranscriptionDialog
import com.example.ui.components.MultiTrackTimeline
import com.example.ui.components.RealExportDialog
import com.example.ui.components.VideoDownloaderSheet
import com.example.ui.components.VideoPreviewPlayer
import com.example.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val playheadMs by viewModel.playheadMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isProMode by viewModel.isProMode.collectAsState()
    val timelineZoom by viewModel.timelineZoom.collectAsState()
    val deviceCaps by viewModel.deviceCapabilities.collectAsState()

    // Export dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    val exportProgress by viewModel.exportProgress.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()

    // Downloader sheet state
    var showDownloaderSheet by remember { mutableStateOf(false) }
    val downloaderUrl by viewModel.downloaderUrl.collectAsState()
    val downloaderInspection by viewModel.downloaderInspection.collectAsState()
    val downloaderProgress by viewModel.downloaderProgress.collectAsState()
    val downloadStartSec by viewModel.downloadStartTime.collectAsState()
    val downloadEndSec by viewModel.downloadEndTime.collectAsState()

    // Live Transcriber State
    var showLiveTranscription by remember { mutableStateOf(false) }
    val liveTranscriber = remember { LiveAudioTranscriber(context) }
    val liveTransState by liveTranscriber.state.collectAsState()

    // Real-Time Visual Effects Preview
    var livePreviewEffectName by remember { mutableStateOf<String?>(null) }
    var livePreviewEffectIntensity by remember { mutableFloatStateOf(0.7f) }

    // Media Picker launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addMediaClip(uri)
        }
    }

    // Permission launcher for Live Mic Transcription
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showLiveTranscription = true
        }
    }

    val selectedClip = remember(activeProject, selectedClipId) {
        activeProject?.tracks?.flatMap { it.clips }?.find { it.id == selectedClipId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeProject?.name ?: "RU ediTOR",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isProMode) "PRO MODE" else "SIMPLE MODE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isProMode) Color(0xFF00E5FF) else Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${deviceCaps.recommendedPerformanceProfile}",
                                fontSize = 10.sp,
                                color = Color(0xFF8B949E)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.canUndo,
                        modifier = Modifier.testTag("btn_undo")
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (viewModel.canUndo) Color.White else Color(0xFF484F58)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.canRedo,
                        modifier = Modifier.testTag("btn_redo")
                    ) {
                        Icon(
                            Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (viewModel.canRedo) Color.White else Color(0xFF484F58)
                        )
                    }

                    // Live Speech Transcribe Shortcut
                    IconButton(
                        onClick = {
                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.testTag("btn_live_transcribe")
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Live Audio Transcription",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    // Downloader
                    IconButton(onClick = { showDownloaderSheet = true }) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = "Public Stream Downloader",
                            tint = Color(0xFF818CF8)
                        )
                    }

                    // Mode Switch
                    IconButton(onClick = { viewModel.toggleProMode() }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Toggle Pro / Simple Mode",
                            tint = if (isProMode) Color(0xFF00E5FF) else Color(0xFF8B949E)
                        )
                    }

                    // Export Button
                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_top_export")
                    ) {
                        Text("4K EXPORT", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
            )
        },
        bottomBar = {
            // Floating Track Add / Media Import strip
            Surface(
                color = Color(0xFF0B0E14),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Button(
                        onClick = { mediaPickerLauncher.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_import_media")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Media", fontSize = 12.sp, color = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Transcribe", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }

                        OutlinedButton(
                            onClick = { showDownloaderSheet = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF818CF8))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Downloader", fontSize = 11.sp, color = Color(0xFF818CF8))
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0B0E14),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Video Canvas Preview Player with live effects feedback
            VideoPreviewPlayer(
                project = activeProject,
                playheadMs = playheadMs,
                isPlaying = isPlaying,
                previewEffectName = livePreviewEffectName,
                previewEffectIntensity = livePreviewEffectIntensity,
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onStepFrame = { forward -> viewModel.stepFrame(forward) },
                onSeek = { ms -> viewModel.seekTo(ms) }
            )

            // 2. Multi-track Precision Timeline
            MultiTrackTimeline(
                project = activeProject,
                playheadMs = playheadMs,
                zoom = timelineZoom,
                selectedClipId = selectedClipId,
                onSelectClip = { id -> viewModel.selectClip(id) },
                onSeek = { ms -> viewModel.seekTo(ms) },
                onSplitClip = { viewModel.splitClipAtPlayhead() },
                onDeleteClip = { viewModel.deleteSelectedClip() },
                onDuplicateClip = { viewModel.duplicateSelectedClip() },
                onZoomChange = { z -> viewModel.setZoom(z) }
            )

            // 3. Contextual Editing Panel (Tabs for VFX, Audio, Keyframes, Captions)
            ContextualEditingPanel(
                selectedClip = selectedClip,
                activeTab = activeTab,
                isProMode = isProMode,
                onTabSelected = { tab -> viewModel.setActiveTab(tab) },
                onSetSpeed = { spd -> viewModel.setClipSpeed(spd) },
                onRotate = { viewModel.rotateSelectedClip() },
                onFlipHorizontal = { viewModel.flipSelectedClip(true) },
                onFlipVertical = { viewModel.flipSelectedClip(false) },
                onVolumeChange = { v -> viewModel.setClipVolume(v) },
                onAddKeyframe = { viewModel.addKeyframeAtPlayhead() },
                onUpdateKeyframe = { x, y, s, r, o -> viewModel.updateKeyframeValues(x, y, s, r, o) },
                onUpdateMask = { m -> viewModel.updateMask(m) },
                onUpdateChromaKey = { c -> viewModel.updateChromaKey(c) },
                onPreviewEffect = { name, intensity ->
                    livePreviewEffectName = name
                    livePreviewEffectIntensity = intensity
                },
                onToggleEffect = { name, cat -> viewModel.toggleEffect(name, cat) },
                onUpdateColor = { c -> viewModel.updateColorAdjustments(c) },
                onToggleAudioEffect = { fx -> viewModel.toggleAudioEffect(fx) },
                onSetVoiceEnhancer = { v -> viewModel.setVoiceEnhancerPreset(v) },
                onUpdateAudioMix = { mix -> viewModel.updateAdvancedAudioMix(mix) },
                onGenerateCaptions = { lang -> viewModel.generateAutoCaptions(lang) },
                onAutoSilenceCut = { viewModel.autoSilenceCut() },
                onAddText = { txt, rtl -> viewModel.addTextOverlay(txt, rtl) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Export Dialog Bottom Sheet
    if (showExportDialog) {
        RealExportDialog(
            project = activeProject,
            deviceCapabilities = deviceCaps,
            exportProgress = exportProgress,
            isExporting = isExporting,
            onStartExport = { res, fps, codec, quality ->
                viewModel.startExport(res, fps, codec, quality)
            },
            onDismiss = {
                showExportDialog = false
                viewModel.dismissExport()
            }
        )
    }

    // Video Downloader Bottom Sheet with duration trimmer
    if (showDownloaderSheet) {
        VideoDownloaderSheet(
            url = downloaderUrl,
            inspection = downloaderInspection,
            progress = downloaderProgress,
            startTimeSec = downloadStartSec,
            endTimeSec = downloadEndSec,
            onUrlChange = { viewModel.setDownloaderUrl(it) },
            onInspectUrl = { viewModel.inspectDownloaderUrl() },
            onSetRange = { s, e -> viewModel.setDownloadRange(s, e) },
            onStartDownload = { q -> viewModel.startSegmentDownload(q) },
            onDismiss = { showDownloaderSheet = false }
        )
    }

    // Live Audio Speech-To-Text Transcriber
    if (showLiveTranscription) {
        LiveTranscriptionDialog(
            state = liveTransState,
            onStartListening = { lang -> liveTranscriber.startListening(lang) },
            onStopListening = { liveTranscriber.stopListening() },
            onClearText = { liveTranscriber.clearText() },
            onAddToTimelineAsCaptions = { text, langCode ->
                val isRtl = langCode.startsWith("ar") || langCode.startsWith("ur")
                viewModel.addTextOverlay(text, isRtl)
            },
            onDismiss = {
                liveTranscriber.stopListening()
                showLiveTranscription = false
            }
        )
    }
}
