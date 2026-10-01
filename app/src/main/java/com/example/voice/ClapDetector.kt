package com.example.voice

class ClapDetector {

    var isEnabled: Boolean = true
    var sensitivity: String = "MEDIUM"
    var onClapDetected: (() -> Unit)? = null

    private var lastPeakTime = 0L
    private var lastRms = 0f

    /**
     * Inspects incoming RMS audio decibel levels to detect sharp transient hand claps locally.
     * Prevents false triggers from continuous speech or background noise.
     */
    fun processRms(rmsDb: Float) {
        if (!isEnabled) return

        val threshold = when (sensitivity.uppercase()) {
            "LOW" -> 85f      // requires very sharp loud clap
            "HIGH" -> 72f     // more sensitive
            else -> 78f       // balanced false-trigger resistance
        }

        val now = System.currentTimeMillis()
        val delta = rmsDb - lastRms

        // A clap is characterized by a rapid surge in amplitude (> 30dB rise) exceeding the threshold
        if (rmsDb > threshold && delta > 25f && (now - lastPeakTime > 400)) {
            lastPeakTime = now
            onClapDetected?.invoke()
        }

        lastRms = rmsDb
    }
}
