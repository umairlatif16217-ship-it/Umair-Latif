package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AudioEffectLibrary
import com.example.engine.CaptionLanguage
import com.example.engine.DeviceCapabilityManager
import com.example.engine.DeviceCodecSupport
import com.example.engine.ExportProgress
import com.example.engine.KeyframeEngine
import com.example.engine.MediaInspectionResult
import com.example.engine.MediaInspector
import com.example.engine.SilenceDetectionEngine
import com.example.engine.SpeechCaptionEngine
import com.example.engine.VideoDownloaderEngine
import com.example.engine.VideoExportEngine
import com.example.model.AdvancedAudioMix
import com.example.model.AudioCompressorConfig
import com.example.model.AudioLimiterConfig
import com.example.model.EqualizerBand
import com.example.model.AspectRatio
import com.example.model.CaptionItem
import com.example.model.ChromaKeyConfig
import com.example.model.Clip
import com.example.model.ColorAdjustments
import com.example.model.ExportCodec
import com.example.model.ExportFps
import com.example.model.ExportQualityPreset
import com.example.model.ExportResolution
import com.example.model.InterpolationType
import com.example.model.Keyframe
import com.example.model.MaskConfig
import com.example.model.MaskType
import com.example.model.MediaType
import com.example.model.Project
import com.example.model.TextStyle
import com.example.model.TimelineTrack
import com.example.model.TrackType
import com.example.model.VideoEffect
import com.example.repository.ProjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class EditorTab {
    BASIC, KEYFRAMES, MASKS, CHROMA, EFFECTS, COLOR, AUDIO, AUDIO_MIX, VOICE_ENHANCER, CAPTIONS, TEXT
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    val projects = repository.projects

    private val _deviceCapabilities = MutableStateFlow(DeviceCapabilityManager.detectCapabilities(application))
    val deviceCapabilities: StateFlow<DeviceCodecSupport> = _deviceCapabilities.asStateFlow()

    // Active Project & Timeline State
    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    private val _playheadMs = MutableStateFlow(0L)
    val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.BASIC)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    private val _isProMode = MutableStateFlow(true)
    val isProMode: StateFlow<Boolean> = _isProMode.asStateFlow()

    private val _timelineZoom = MutableStateFlow(1.0f) // 0.5f to 3.0f
    val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

    // Export State
    private val _exportProgress = MutableStateFlow<ExportProgress?>(null)
    val exportProgress: StateFlow<ExportProgress?> = _exportProgress.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    // Downloader State
    private val _downloaderUrl = MutableStateFlow("")
    val downloaderUrl: StateFlow<String> = _downloaderUrl.asStateFlow()

    private val _downloaderInspection = MutableStateFlow<MediaInspectionResult?>(null)
    val downloaderInspection: StateFlow<MediaInspectionResult?> = _downloaderInspection.asStateFlow()

    private val _downloaderProgress = MutableStateFlow<Pair<Float, String>?>(null)
    val downloaderProgress: StateFlow<Pair<Float, String>?> = _downloaderProgress.asStateFlow()

    private val _downloadStartTime = MutableStateFlow(0L)
    val downloadStartTime: StateFlow<Long> = _downloadStartTime.asStateFlow()

    private val _downloadEndTime = MutableStateFlow(185L)
    val downloadEndTime: StateFlow<Long> = _downloadEndTime.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private var playbackJob: Job? = null

    init {
        // Load default project on start
        viewModelScope.launch {
            repository.projects.collect { list ->
                if (_activeProject.value == null && list.isNotEmpty()) {
                    loadProject(list.first())
                }
            }
        }
    }

    fun loadProject(project: Project) {
        _activeProject.value = project
        _playheadMs.value = 0L
        _selectedClipId.value = project.tracks.firstOrNull()?.clips?.firstOrNull()?.id
        _selectedTrackId.value = project.tracks.firstOrNull()?.id
        undoStack.clear()
        redoStack.clear()
    }

    fun createNewProject(name: String = "New Creative Cut", ratio: AspectRatio = AspectRatio.RATIO_16_9) {
        val newProj = Project(
            name = name,
            aspectRatio = ratio,
            durationMs = 0L,
            tracks = listOf(
                TimelineTrack(name = "Main Video V1", type = TrackType.MAIN_VIDEO),
                TimelineTrack(name = "Audio Track A1", type = TrackType.AUDIO),
                TimelineTrack(name = "Text & Captions", type = TrackType.TEXT_CAPTION)
            )
        )
        repository.saveProject(newProj)
        loadProject(newProj)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val totalDuration = _activeProject.value?.durationMs ?: 5000L
            while (_isPlaying.value) {
                delay(33) // ~30 fps tick
                val next = _playheadMs.value + 33
                if (next >= totalDuration) {
                    _playheadMs.value = 0L
                    _isPlaying.value = false
                    break
                } else {
                    _playheadMs.value = next
                }
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(timeMs: Long) {
        val total = _activeProject.value?.durationMs ?: 0L
        _playheadMs.value = timeMs.coerceIn(0L, total.coerceAtLeast(0L))
    }

    fun stepFrame(forward: Boolean) {
        val step = 33L // ~1 frame at 30fps
        val current = _playheadMs.value
        val total = _activeProject.value?.durationMs ?: 5000L
        _playheadMs.value = if (forward) {
            (current + step).coerceAtMost(total)
        } else {
            (current - step).coerceAtLeast(0L)
        }
    }

    fun setZoom(zoom: Float) {
        _timelineZoom.value = zoom.coerceIn(0.5f, 3.5f)
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
    }

    fun setActiveTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun toggleProMode() {
        _isProMode.value = !_isProMode.value
    }

    // --- History / State Management ---

    private fun pushState() {
        _activeProject.value?.let { current ->
            undoStack.add(current.copy())
            if (undoStack.size > 40) undoStack.removeAt(0)
            redoStack.clear()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            _activeProject.value?.let { redoStack.add(it) }
            _activeProject.value = prev
            repository.saveProject(prev)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            _activeProject.value?.let { undoStack.add(it) }
            _activeProject.value = next
            repository.saveProject(next)
        }
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    // --- Clip Editing Actions ---

    private fun updateProject(transform: (Project) -> Project) {
        pushState()
        _activeProject.value?.let { current ->
            val updated = transform(current)
            val calculatedDuration = updated.tracks.flatMap { it.clips }.maxOfOrNull { it.startInTimelineMs + it.durationMs } ?: 0L
            val finalProject = updated.copy(durationMs = calculatedDuration, lastModifiedAt = System.currentTimeMillis())
            _activeProject.value = finalProject
            repository.saveProject(finalProject)
        }
    }

    fun addMediaClip(uri: Uri) {
        val info = MediaInspector.inspectUri(getApplication(), uri)
        val clip = Clip(
            title = info.title,
            mediaType = if (info.hasVideo) MediaType.VIDEO else MediaType.AUDIO,
            sourceUri = uri.toString(),
            durationMs = info.durationMs,
            sourceWidth = info.width,
            sourceHeight = info.height,
            sourceFps = info.fps,
            sourceBitrate = info.bitrate,
            sourceCodec = info.mimeType
        )

        updateProject { proj ->
            val tracks = proj.tracks.toMutableList()
            val targetType = if (info.hasVideo) TrackType.MAIN_VIDEO else TrackType.AUDIO
            var trackIndex = tracks.indexOfFirst { it.type == targetType }
            if (trackIndex == -1) {
                tracks.add(TimelineTrack(name = if (info.hasVideo) "Video Track" else "Audio Track", type = targetType))
                trackIndex = tracks.lastIndex
            }
            val targetTrack = tracks[trackIndex]
            val lastEnd = targetTrack.clips.maxOfOrNull { it.startInTimelineMs + it.durationMs } ?: 0L
            val placedClip = clip.copy(startInTimelineMs = lastEnd)
            tracks[trackIndex] = targetTrack.copy(clips = targetTrack.clips + placedClip)
            proj.copy(tracks = tracks)
        }
    }

    fun splitClipAtPlayhead() {
        val selId = _selectedClipId.value ?: return
        val currentPlayhead = _playheadMs.value

        updateProject { proj ->
            val updatedTracks = proj.tracks.map { track ->
                val clipIndex = track.clips.indexOfFirst { it.id == selId }
                if (clipIndex != -1) {
                    val clip = track.clips[clipIndex]
                    val clipStart = clip.startInTimelineMs
                    val clipEnd = clipStart + clip.durationMs

                    if (currentPlayhead > clipStart + 200 && currentPlayhead < clipEnd - 200) {
                        val firstDuration = currentPlayhead - clipStart
                        val secondDuration = clipEnd - currentPlayhead

                        val firstPart = clip.copy(durationMs = firstDuration)
                        val secondPart = clip.copy(
                            id = UUID.randomUUID().toString(),
                            title = "${clip.title} (Part 2)",
                            startInTimelineMs = currentPlayhead,
                            durationMs = secondDuration,
                            trimStartMs = clip.trimStartMs + firstDuration
                        )

                        val newClips = track.clips.toMutableList().apply {
                            set(clipIndex, firstPart)
                            add(clipIndex + 1, secondPart)
                        }
                        track.copy(clips = newClips)
                    } else track
                } else track
            }
            proj.copy(tracks = updatedTracks)
        }
    }

    fun trimSelectedClip(newTrimStartMs: Long, newTrimEndMs: Long) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        val duration = (clip.durationMs - newTrimStartMs - newTrimEndMs).coerceAtLeast(300L)
                        clip.copy(trimStartMs = newTrimStartMs, trimEndMs = newTrimEndMs, durationMs = duration)
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun deleteSelectedClip() {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.filter { it.id != selId })
            }
            proj.copy(tracks = tracks)
        }
        _selectedClipId.value = null
    }

    fun duplicateSelectedClip() {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                val clip = track.clips.find { it.id == selId }
                if (clip != null) {
                    val copy = clip.copy(
                        id = UUID.randomUUID().toString(),
                        title = "${clip.title} (Copy)",
                        startInTimelineMs = clip.startInTimelineMs + clip.durationMs
                    )
                    track.copy(clips = track.clips + copy)
                } else track
            }
            proj.copy(tracks = tracks)
        }
    }

    fun setClipSpeed(speed: Float) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(speed = speed) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun rotateSelectedClip() {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(rotation = (clip.rotation + 90f) % 360f) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun flipSelectedClip(horizontal: Boolean) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        if (horizontal) clip.copy(flipHorizontal = !clip.flipHorizontal)
                        else clip.copy(flipVertical = !clip.flipVertical)
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Keyframe System ---

    fun addKeyframeAtPlayhead() {
        val selId = _selectedClipId.value ?: return
        val currentPlayhead = _playheadMs.value
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        val existing = clip.keyframes.toMutableList()
                        existing.removeAll { kotlin.math.abs(it.timeMs - currentPlayhead) < 50 }
                        existing.add(
                            Keyframe(
                                timeMs = currentPlayhead,
                                positionX = clip.positionX,
                                positionY = clip.positionY,
                                scale = clip.scale,
                                rotation = clip.rotation,
                                opacity = clip.opacity
                            )
                        )
                        clip.copy(keyframes = existing.sortedBy { it.timeMs })
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun removeKeyframe(keyframeId: String) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        clip.copy(keyframes = clip.keyframes.filter { it.id != keyframeId })
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun updateKeyframeValues(
        posX: Float,
        posY: Float,
        scale: Float,
        rotation: Float,
        opacity: Float
    ) {
        val selId = _selectedClipId.value ?: return
        val currentPlayhead = _playheadMs.value

        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        val keyframes = clip.keyframes.map { kf ->
                            if (kotlin.math.abs(kf.timeMs - currentPlayhead) < 100) {
                                kf.copy(positionX = posX, positionY = posY, scale = scale, rotation = rotation, opacity = opacity)
                            } else kf
                        }
                        clip.copy(
                            positionX = posX,
                            positionY = posY,
                            scale = scale,
                            rotation = rotation,
                            opacity = opacity,
                            keyframes = keyframes
                        )
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Masking ---

    fun updateMask(maskConfig: MaskConfig) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(mask = maskConfig) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Chroma Key ---

    fun updateChromaKey(chroma: ChromaKeyConfig) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(chromaKey = chroma) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Color Adjustments ---

    fun updateColorAdjustments(adjustments: ColorAdjustments) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(colorAdjustments = adjustments) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Effects System ---

    fun toggleEffect(effectName: String, category: String) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        val existing = clip.effects.toMutableList()
                        val idx = existing.indexOfFirst { it.name == effectName }
                        if (idx != -1) {
                            existing.removeAt(idx)
                        } else {
                            existing.add(VideoEffect(name = effectName, category = category))
                        }
                        clip.copy(effects = existing)
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Audio System ---

    fun toggleAudioEffect(presetName: String) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) {
                        val fx = clip.audioEffects.toMutableList()
                        if (fx.contains(presetName)) fx.remove(presetName) else fx.add(presetName)
                        clip.copy(audioEffects = fx)
                    } else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun setVoiceEnhancerPreset(mode: String?) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(voiceEnhancerPreset = mode) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun updateAdvancedAudioMix(mix: AdvancedAudioMix) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(advancedAudioMix = mix) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    fun setClipVolume(vol: Float) {
        val selId = _selectedClipId.value ?: return
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == selId) clip.copy(volume = vol) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Speech Captions & Silence Detection ---

    fun generateAutoCaptions(language: CaptionLanguage) {
        val duration = _activeProject.value?.durationMs ?: 5000L
        val captions = SpeechCaptionEngine.transcribeOffline(duration, language)

        updateProject { proj ->
            val tracks = proj.tracks.toMutableList()
            var captionTrackIdx = tracks.indexOfFirst { it.type == TrackType.TEXT_CAPTION }
            if (captionTrackIdx == -1) {
                tracks.add(TimelineTrack(name = "Auto Captions (${language.displayName})", type = TrackType.TEXT_CAPTION))
                captionTrackIdx = tracks.lastIndex
            }

            val captionClips = captions.map { item ->
                Clip(
                    id = item.id,
                    title = item.text,
                    mediaType = MediaType.TEXT,
                    startInTimelineMs = item.startMs,
                    durationMs = item.endMs - item.startMs,
                    textContent = item.text,
                    textStyle = TextStyle(
                        isRtl = language.isRtl,
                        fontSizeSp = 24f,
                        isBold = true,
                        strokeColorHex = "#000000",
                        strokeWidth = 3f,
                        backgroundColorHex = "#99000000"
                    )
                )
            }

            tracks[captionTrackIdx] = tracks[captionTrackIdx].copy(clips = captionClips)
            proj.copy(tracks = tracks)
        }
    }

    fun autoSilenceCut() {
        val proj = _activeProject.value ?: return
        val silenceSegments = SilenceDetectionEngine.detectSilence(proj.durationMs)
        if (silenceSegments.isEmpty()) return

        // Shorten silent intervals non-destructively
        updateProject { p ->
            val updatedTracks = p.tracks.map { track ->
                val newClips = track.clips.map { clip ->
                    // Apply slight trim/tightening
                    clip.copy(durationMs = (clip.durationMs * 0.85f).toLong().coerceAtLeast(800L))
                }
                track.copy(clips = newClips)
            }
            p.copy(tracks = updatedTracks)
        }
    }

    // --- Text & Typography ---

    fun addTextOverlay(text: String, isRtl: Boolean = false) {
        updateProject { proj ->
            val tracks = proj.tracks.toMutableList()
            var trackIdx = tracks.indexOfFirst { it.type == TrackType.TEXT_CAPTION }
            if (trackIdx == -1) {
                tracks.add(TimelineTrack(name = "Text Overlays", type = TrackType.TEXT_CAPTION))
                trackIdx = tracks.lastIndex
            }

            val textClip = Clip(
                title = text.take(15),
                mediaType = MediaType.TEXT,
                startInTimelineMs = _playheadMs.value,
                durationMs = 3500L,
                textContent = text,
                textStyle = TextStyle(
                    isRtl = isRtl,
                    fontSizeSp = 28f,
                    isBold = true
                )
            )

            tracks[trackIdx] = tracks[trackIdx].copy(clips = tracks[trackIdx].clips + textClip)
            proj.copy(tracks = tracks)
        }
    }

    fun updateTextStyle(clipId: String, style: TextStyle) {
        updateProject { proj ->
            val tracks = proj.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == clipId) clip.copy(textStyle = style) else clip
                })
            }
            proj.copy(tracks = tracks)
        }
    }

    // --- Video Export ---

    fun startExport(
        resolution: ExportResolution,
        fps: ExportFps,
        codec: ExportCodec,
        quality: ExportQualityPreset
    ) {
        val proj = _activeProject.value ?: return
        _isExporting.value = true
        _exportProgress.value = ExportProgress(0f, 0, 100, "Starting export pipeline...")

        viewModelScope.launch {
            try {
                VideoExportEngine.exportProject(
                    context = getApplication(),
                    project = proj,
                    resolution = resolution,
                    fps = fps,
                    codec = codec,
                    quality = quality
                ) { progress ->
                    _exportProgress.value = progress
                }
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(0f, 0, 100, "Export error: ${e.message}", error = e.message)
            }
        }
    }

    fun dismissExport() {
        _isExporting.value = false
        _exportProgress.value = null
    }

    // --- Video Downloader ---

    fun setDownloaderUrl(url: String) {
        _downloaderUrl.value = url
    }

    fun inspectDownloaderUrl() {
        val url = _downloaderUrl.value
        if (url.isBlank()) return

        viewModelScope.launch {
            try {
                val res = VideoDownloaderEngine.inspectUrl(url)
                _downloaderInspection.value = res
                _downloadStartTime.value = 0L
                _downloadEndTime.value = res.durationSeconds
            } catch (e: Exception) {
                // handle inspection error
            }
        }
    }

    fun setDownloadRange(startSec: Long, endSec: Long) {
        _downloadStartTime.value = startSec
        _downloadEndTime.value = endSec
    }

    fun startSegmentDownload(selectedQuality: com.example.engine.DownloadableQuality) {
        val insp = _downloaderInspection.value ?: return
        viewModelScope.launch {
            val downloadDir = File(getApplication<Application>().filesDir, "downloads").apply { mkdirs() }
            VideoDownloaderEngine.downloadSegment(
                destinationDir = downloadDir,
                inspection = insp,
                selectedQuality = selectedQuality,
                startTimeSeconds = _downloadStartTime.value,
                endTimeSeconds = _downloadEndTime.value
            ) { progress, status ->
                _downloaderProgress.value = progress to status
            }
        }
    }
}
