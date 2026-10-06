package com.example.engine

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build

data class DeviceCodecSupport(
    val supportsH264: Boolean = true,
    val supportsH265: Boolean = false,
    val supportsAV1: Boolean = false,
    val supports4KEncode: Boolean = false,
    val supports4KDecode: Boolean = false,
    val maxH264Width: Int = 1920,
    val maxH264Height: Int = 1080,
    val maxH265Width: Int = 0,
    val maxH265Height: Int = 0,
    val estimatedRamGb: Int = 4,
    val recommendedPerformanceProfile: String = "Balanced"
)

object DeviceCapabilityManager {

    fun detectCapabilities(context: Context): DeviceCodecSupport {
        var hasH264 = false
        var hasH265 = false
        var hasAV1 = false
        var max264W = 1920
        var max264H = 1080
        var max265W = 0
        var max265H = 0
        var canEncode4K = false
        var canDecode4K = false

        try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            val codecInfos = codecList.codecInfos

            for (info in codecInfos) {
                val types = info.supportedTypes
                for (type in types) {
                    if (type.equals("video/avc", ignoreCase = true)) {
                        hasH264 = true
                        val caps = info.getCapabilitiesForType(type)?.videoCapabilities
                        caps?.supportedWidths?.upper?.let { w ->
                            if (w > max264W) max264W = w
                        }
                        caps?.supportedHeights?.upper?.let { h ->
                            if (h > max264H) max264H = h
                        }
                        if (info.isEncoder && caps?.areSizeAndRateSupported(3840, 2160, 30.0) == true) {
                            canEncode4K = true
                        }
                        if (!info.isEncoder && caps?.areSizeAndRateSupported(3840, 2160, 30.0) == true) {
                            canDecode4K = true
                        }
                    }
                    if (type.equals("video/hevc", ignoreCase = true)) {
                        hasH265 = true
                        val caps = info.getCapabilitiesForType(type)?.videoCapabilities
                        caps?.supportedWidths?.upper?.let { w ->
                            if (w > max265W) max265W = w
                        }
                        caps?.supportedHeights?.upper?.let { h ->
                            if (h > max265H) max265H = h
                        }
                        if (info.isEncoder && caps?.areSizeAndRateSupported(3840, 2160, 30.0) == true) {
                            canEncode4K = true
                        }
                        if (!info.isEncoder && caps?.areSizeAndRateSupported(3840, 2160, 30.0) == true) {
                            canDecode4K = true
                        }
                    }
                    if (type.equals("video/av01", ignoreCase = true)) {
                        hasAV1 = true
                    }
                }
            }
        } catch (_: Exception) {
            hasH264 = true
        }

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memoryInfo)
        val ramGb = ((memoryInfo.totalMem / (1024 * 1024 * 1024))).toInt().coerceAtLeast(2)

        val profile = when {
            ramGb >= 8 && canEncode4K -> "High Performance (True 4K Capable)"
            ramGb >= 4 -> "Balanced (1080p & 2K Optimal)"
            else -> "Battery Saver / Efficient"
        }

        return DeviceCodecSupport(
            supportsH264 = hasH264,
            supportsH265 = hasH265,
            supportsAV1 = hasAV1,
            supports4KEncode = canEncode4K || max264W >= 3840 || max265W >= 3840,
            supports4KDecode = canDecode4K,
            maxH264Width = max264W,
            maxH264Height = max264H,
            maxH265Width = max265W,
            maxH265Height = max265H,
            estimatedRamGb = ramGb,
            recommendedPerformanceProfile = profile
        )
    }
}
