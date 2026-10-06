package com.example.engine

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

data class MediaMetadataInfo(
    val title: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotation: Int,
    val fps: Int,
    val bitrate: Long,
    val mimeType: String,
    val audioChannels: Int,
    val audioSampleRate: Int,
    val hasVideo: Boolean,
    val hasAudio: Boolean,
    val sizeBytes: Long
)

object MediaInspector {

    fun inspectUri(context: Context, uri: Uri): MediaMetadataInfo {
        val retriever = MediaMetadataRetriever()
        var durationMs = 0L
        var width = 1920
        var height = 1080
        var rotation = 0
        var fps = 30
        var bitrate = 10_000_000L
        var mimeType = "video/mp4"
        var channels = 2
        var sampleRate = 44100
        var hasVideo = true
        var hasAudio = true
        var sizeBytes = 0L

        try {
            retriever.setDataSource(context, uri)

            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durStr?.toLongOrNull() ?: 5000L

            val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            if (wStr != null && hStr != null) {
                width = wStr.toIntOrNull() ?: 1920
                height = hStr.toIntOrNull() ?: 1080
            } else {
                hasVideo = false
            }

            val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            rotation = rotStr?.toIntOrNull() ?: 0

            val brStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            bitrate = brStr?.toLongOrNull() ?: 12_000_000L

            val mimeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            if (mimeStr != null) mimeType = mimeStr

            val frameRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            fps = frameRateStr?.toFloatOrNull()?.toInt() ?: 30

            val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
            hasAudio = hasAudioStr != null && hasAudioStr.equals("yes", ignoreCase = true)

            // Try getting file size if local
            if (uri.scheme == "file") {
                val f = File(uri.path ?: "")
                if (f.exists()) sizeBytes = f.length()
            } else {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1 && cursor.moveToFirst()) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
            // fallback
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        return MediaMetadataInfo(
            title = uri.lastPathSegment ?: "Imported Media",
            durationMs = if (durationMs > 0) durationMs else 5000L,
            width = width,
            height = height,
            rotation = rotation,
            fps = fps,
            bitrate = bitrate,
            mimeType = mimeType,
            audioChannels = channels,
            audioSampleRate = sampleRate,
            hasVideo = hasVideo,
            hasAudio = hasAudio,
            sizeBytes = sizeBytes
        )
    }
}
