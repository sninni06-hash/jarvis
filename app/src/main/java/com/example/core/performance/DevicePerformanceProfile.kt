package com.example.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

enum class PerformanceMode {
    PERFORMANCE, // For Low-End devices (Galaxy J7 Prime, Exynos 7870, 2-3GB RAM)
    BALANCED,
    QUALITY      // For modern high-end devices
}

data class DeviceSpecs(
    val modelName: String,
    val androidVersion: String,
    val sdkInt: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val cpuCores: Int,
    val isLowRamDevice: Boolean,
    val availableInternalStorageMb: Long,
    val suggestedMode: PerformanceMode
)

object DevicePerformanceProfile {

    fun detectSpecs(context: Context): DeviceSpecs {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val rawTotal = if (memInfo.totalMem > 0) memInfo.totalMem else Runtime.getRuntime().maxMemory()
        val totalRamMb = (rawTotal / (1024 * 1024)).coerceAtLeast(512L)
        val rawAvail = if (memInfo.availMem > 0) memInfo.availMem else Runtime.getRuntime().freeMemory()
        val availRamMb = (rawAvail / (1024 * 1024)).coerceAtLeast(128L)
        val isLowRam = activityManager?.isLowRamDevice ?: (totalRamMb <= 3072)
        val cores = Runtime.getRuntime().availableProcessors()
        val sdk = Build.VERSION.SDK_INT
        val model = "${Build.MANUFACTURER} ${Build.MODEL}"

        val storageMb = try {
            val stat = StatFs(Environment.getDataDirectory().path)
            (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        } catch (e: Exception) {
            1024L
        }

        // Low-End detection: e.g. <=3GB RAM, <=4 cores, isLowRamDevice, or older chipset
        val suggested = when {
            isLowRam || totalRamMb <= 3072 || cores <= 4 || sdk < 28 -> PerformanceMode.PERFORMANCE
            totalRamMb in 3073..5120 -> PerformanceMode.BALANCED
            else -> PerformanceMode.QUALITY
        }

        return DeviceSpecs(
            modelName = model,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API $sdk)",
            sdkInt = sdk,
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            cpuCores = cores,
            isLowRamDevice = isLowRam,
            availableInternalStorageMb = storageMb,
            suggestedMode = suggested
        )
    }

    // Adaptive performance parameters
    fun getParticleCount(mode: PerformanceMode): Int = when (mode) {
        PerformanceMode.PERFORMANCE -> 14
        PerformanceMode.BALANCED -> 26
        PerformanceMode.QUALITY -> 42
    }

    fun enableComplexBlur(mode: PerformanceMode): Boolean = mode == PerformanceMode.QUALITY
    fun enableMultiLayerGlow(mode: PerformanceMode): Boolean = mode != PerformanceMode.PERFORMANCE
    fun getVisionSampleIntervalMs(mode: PerformanceMode): Long = when (mode) {
        PerformanceMode.PERFORMANCE -> 2500L
        PerformanceMode.BALANCED -> 1500L
        PerformanceMode.QUALITY -> 800L
    }
}
