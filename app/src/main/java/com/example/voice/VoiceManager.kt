package com.example.voice

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceManager(
    private val context: Context,
    val elevenLabsProvider: ElevenLabsProvider? = null
) {
    val ttsManager = TTSManager(context, elevenLabsProvider)
    val sttManager = STTManager(context)
    val wakeWordManager = WakeWordManager()
    val clapDetector = ClapDetector()

    val isListening: StateFlow<Boolean> get() = sttManager.isListening
    val rmsDb: StateFlow<Float> get() = sttManager.rmsDb

    var isDeactivated: Boolean = false

    var onUserCommand: ((String, Long) -> Unit)? = null
    var onWakeUpTriggered: ((String) -> Unit)? = null
    var onClapTriggered: (() -> Unit)? = null
    var onInterruption: (() -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        // Forward RMS changes to ClapDetector for lightweight transient clap detection
        scope.launch {
            sttManager.rmsDb.collect { rms ->
                if (!ttsManager.isMuted) {
                    clapDetector.processRms(rms)
                }
            }
        }

        clapDetector.onClapDetected = {
            onClapTriggered?.invoke()
        }

        // True barge-in detection: as soon as speech begins or volume rises, interrupt ongoing TTS
        sttManager.onResult = { recognizedText ->
            val sttEndTime = System.currentTimeMillis()
            val lower = recognizedText.lowercase(Locale.ROOT).trim()

            // If assistant is currently speaking and user speaks, barge-in cutoff
            ttsManager.stop()

            // Natural interruption keywords
            val isInterruption = lower == "ruko" || lower == "stop" || lower == "wait" ||
                    lower == "cancel" || lower == "pause" || lower == "chup" ||
                    lower == "arey ruko" || lower == "hold on" || lower == "mat karo" ||
                    lower.startsWith("ruko ") || lower.startsWith("stop ")

            if (isInterruption) {
                ttsManager.stop()
                onInterruption?.invoke()
            } else if (isDeactivated) {
                // In DEACTIVATED state: Only wake-up commands awaken the assistant!
                val (isWakeUp, remaining) = wakeWordManager.isWakeUpCommand(recognizedText)
                if (isWakeUp) {
                    onWakeUpTriggered?.invoke(remaining)
                }
            } else {
                val (isWake, remainingText) = wakeWordManager.matchesWakeWord(recognizedText)
                if (isWake) {
                    onUserCommand?.invoke(remainingText, sttEndTime)
                } else {
                    onUserCommand?.invoke(recognizedText, sttEndTime)
                }
            }
        }
    }

    fun startListening() {
        ttsManager.stop() // Always stop TTS before listening
        sttManager.startListening()
    }

    fun stopListening() {
        sttManager.stopListening()
    }

    fun speak(text: String, sttEndTime: Long = 0L, onDone: (() -> Unit)? = null) {
        ttsManager.speak(text, sttEndTime) {
            onDone?.invoke()
            // Auto continue listening if continuous mode is enabled and not deactivated
            if (sttManager.isContinuousListening && !ttsManager.isMuted && !isDeactivated) {
                startListening()
            }
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun setMute(muted: Boolean) {
        ttsManager.isMuted = muted
        if (muted) {
            ttsManager.stop()
        }
    }

    fun shutdown() {
        sttManager.stopListening()
        ttsManager.shutdown()
    }
}
