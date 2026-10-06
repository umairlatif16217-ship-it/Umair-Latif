package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

data class DownloadableQuality(
    val label: String,
    val resolution: String,
    val isAudioOnly: Boolean = false,
    val estimatedSizeBytes: Long = 0L,
    val codec: String = "H.264"
)

data class MediaInspectionResult(
    val title: String,
    val sourceUrl: String,
    val durationSeconds: Long,
    val qualities: List<DownloadableQuality>,
    val author: String = "Web Stream",
    val isPermitted: Boolean = true
)

object VideoDownloaderEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun inspectUrl(url: String): MediaInspectionResult = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()

        val fileName = cleanUrl.substringAfterLast("/").substringBefore("?").ifEmpty { "Online Media Stream" }

        val qualities = listOf(
            DownloadableQuality("4K Ultra HD", "3840x2160", estimatedSizeBytes = 320_000_000L, codec = "HEVC / AV1"),
            DownloadableQuality("2K Quad HD", "2560x1440", estimatedSizeBytes = 180_000_000L, codec = "H.264"),
            DownloadableQuality("1080p Full HD", "1920x1080", estimatedSizeBytes = 85_000_000L, codec = "H.264"),
            DownloadableQuality("720p HD", "1280x720", estimatedSizeBytes = 42_000_000L, codec = "H.264"),
            DownloadableQuality("480p SD", "854x480", estimatedSizeBytes = 20_000_000L, codec = "H.264"),
            DownloadableQuality("Audio Only (M4A / AAC)", "Audio 320kbps", isAudioOnly = true, estimatedSizeBytes = 9_000_000L, codec = "AAC")
        )

        MediaInspectionResult(
            title = fileName,
            sourceUrl = cleanUrl,
            durationSeconds = 185L, // 3 mins 5 secs sample
            qualities = qualities,
            author = "Public Media Host"
        )
    }

    suspend fun downloadSegment(
        destinationDir: File,
        inspection: MediaInspectionResult,
        selectedQuality: DownloadableQuality,
        startTimeSeconds: Long,
        endTimeSeconds: Long,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val duration = (endTimeSeconds - startTimeSeconds).coerceAtLeast(1L)
        val ext = if (selectedQuality.isAudioOnly) "m4a" else "mp4"
        val outFile = File(destinationDir, "download_${System.currentTimeMillis()}_trim_${startTimeSeconds}s_${endTimeSeconds}s.$ext")

        onProgress(0.1f, "Connecting to stream server...")
        delay(120)

        onProgress(0.3f, "Seeking stream segment from $startTimeSeconds s to $endTimeSeconds s...")
        delay(150)

        // Progress simulation for downloading bytes
        for (step in 4..10) {
            delay(80)
            val pct = step / 10f
            onProgress(pct, "Downloading byte stream ($duration s segment)... ${(pct * 100).toInt()}%")
        }

        outFile.writeText("RU_EDITOR_DOWNLOADED_MEDIA_SEGMENT_${inspection.title}_START_${startTimeSeconds}_END_${endTimeSeconds}\n")
        onProgress(1.0f, "Download complete!")
        outFile
    }
}
