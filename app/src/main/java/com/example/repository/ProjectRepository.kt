package com.example.repository

import android.content.Context
import com.example.model.AspectRatio
import com.example.model.Clip
import com.example.model.ColorAdjustments
import com.example.model.MediaType
import com.example.model.Project
import com.example.model.TimelineTrack
import com.example.model.TrackType
import com.example.model.VideoEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

class ProjectRepository(private val context: Context) {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        // Initialize with default template / starter project if empty
        val starterClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                title = "Cinematic Drone Intro (4K)",
                mediaType = MediaType.VIDEO,
                durationMs = 6000L,
                sourceWidth = 3840,
                sourceHeight = 2160,
                sourceFps = 60,
                sourceBitrate = 48_000_000L,
                sourceCodec = "HEVC / H.265",
                colorAdjustments = ColorAdjustments(contrast = 1.15f, saturation = 1.2f, vibrance = 0.2f),
                effects = listOf(VideoEffect(name = "Cinematic Glow", category = "Stylized", intensity = 0.6f))
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                title = "Urban City Timelapse",
                mediaType = MediaType.VIDEO,
                startInTimelineMs = 6000L,
                durationMs = 5000L,
                sourceWidth = 3840,
                sourceHeight = 2160,
                sourceFps = 30,
                sourceBitrate = 35_000_000L,
                sourceCodec = "H.264"
            )
        )

        val starterAudio = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                title = "Atmospheric Synth Pad (Epic)",
                mediaType = MediaType.AUDIO,
                durationMs = 11000L,
                volume = 0.85f,
                fadeInMs = 500L,
                fadeOutMs = 1000L
            )
        )

        val starterProject = Project(
            id = "starter_4k_epic",
            name = "4K Cinematic Masterpiece",
            createdAt = System.currentTimeMillis() - 3600000L,
            lastModifiedAt = System.currentTimeMillis(),
            aspectRatio = AspectRatio.RATIO_16_9,
            durationMs = 11000L,
            tracks = listOf(
                TimelineTrack(
                    id = "track_v1",
                    name = "Main Video V1",
                    type = TrackType.MAIN_VIDEO,
                    clips = starterClips
                ),
                TimelineTrack(
                    id = "track_a1",
                    name = "Music A1",
                    type = TrackType.AUDIO,
                    clips = starterAudio
                )
            ),
            isFavorite = true
        )

        val reelsProject = Project(
            id = "reels_short_promo",
            name = "Viral Reels Trend",
            createdAt = System.currentTimeMillis() - 7200000L,
            lastModifiedAt = System.currentTimeMillis() - 1800000L,
            aspectRatio = AspectRatio.RATIO_9_16,
            durationMs = 8500L,
            tracks = listOf(
                TimelineTrack(
                    id = "track_reels_v1",
                    name = "Main Vertical V1",
                    type = TrackType.MAIN_VIDEO,
                    clips = listOf(
                        Clip(
                            id = UUID.randomUUID().toString(),
                            title = "High Energy Motion",
                            mediaType = MediaType.VIDEO,
                            durationMs = 8500L,
                            sourceWidth = 1080,
                            sourceHeight = 1920,
                            sourceFps = 60
                        )
                    )
                )
            )
        )

        _projects.value = listOf(starterProject, reelsProject)
    }

    fun saveProject(project: Project) {
        val current = _projects.value.toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        if (index != -1) {
            current[index] = project.copy(lastModifiedAt = System.currentTimeMillis())
        } else {
            current.add(0, project.copy(lastModifiedAt = System.currentTimeMillis()))
        }
        _projects.value = current
    }

    fun deleteProject(projectId: String) {
        _projects.value = _projects.value.filter { it.id != projectId }
    }

    fun duplicateProject(projectId: String): Project? {
        val original = _projects.value.find { it.id == projectId } ?: return null
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            lastModifiedAt = System.currentTimeMillis()
        )
        saveProject(copy)
        return copy
    }

    fun toggleFavorite(projectId: String) {
        _projects.value = _projects.value.map {
            if (it.id == projectId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun getProject(id: String): Project? {
        return _projects.value.find { it.id == id }
    }
}
