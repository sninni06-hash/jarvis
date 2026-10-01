package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

data class VoiceLatencyMetrics(
    val sttEndTimestamp: Long = 0L,
    val aiFirstTokenTimestamp: Long = 0L,
    val ttsRequestTimestamp: Long = 0L,
    val ttsFirstAudioTimestamp: Long = 0L,
    val totalResponseStartLatencyMs: Long = 0L
)

class TTSManager(
    private val context: Context,
    private val elevenLabsProvider: ElevenLabsProvider? = null
) : TextToSpeech.OnInitListener {

    private var nativeTts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isNativeTtsInitialized = false
    var isMuted = false
    var speechRate: Float = 1.0f

    var onSpeechCompleted: (() -> Unit)? = null
    var onSpeechStarted: (() -> Unit)? = null

    private var activeIdentity: String = "JARVIS"

    // Independent Voice Profiles for JARVIS and MYRAA
    var jarvisConfig = VoiceProfileConfig(
        voiceId = "21m00Tcm4TlvDq8ikWAM",
        modelId = "eleven_flash_v2_5",
        stability = 0.45f,
        similarityBoost = 0.75f,
        style = 0.05f,
        useSpeakerBoost = true
    )

    var myraaConfig = VoiceProfileConfig(
        voiceId = "EXAVITQu4vr4xnSDxMaL",
        modelId = "eleven_flash_v2_5",
        stability = 0.38f,
        similarityBoost = 0.80f,
        style = 0.15f,
        useSpeakerBoost = true
    )

    private val speechScope = CoroutineScope(Dispatchers.IO)
    private var currentStreamJob: Job? = null
    private val audioPlayQueue = ConcurrentLinkedQueue<File>()
    private var isPlayingQueue = false

    private val _latencyMetrics = MutableStateFlow(VoiceLatencyMetrics())
    val latencyMetrics: StateFlow<VoiceLatencyMetrics> = _latencyMetrics.asStateFlow()

    init {
        nativeTts = TextToSpeech(context.applicationContext, this)
    }

    fun configureVoiceProfiles(
        identity: String,
        rate: Float,
        jConfig: VoiceProfileConfig,
        mConfig: VoiceProfileConfig
    ) {
        activeIdentity = identity
        speechRate = rate
        jarvisConfig = jConfig
        myraaConfig = mConfig

        // Fallback acoustic setup
        if (identity.equals("MYRAA", ignoreCase = true)) {
            nativeTts?.setPitch(1.18f)
            nativeTts?.setSpeechRate(speechRate * 1.02f)
        } else {
            nativeTts?.setPitch(0.88f)
            nativeTts?.setSpeechRate(speechRate)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isNativeTtsInitialized = true
            val hindi = Locale.forLanguageTag("hi-IN")
            val result = nativeTts?.setLanguage(hindi)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                nativeTts?.setLanguage(Locale.US)
            }

            nativeTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStarted?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    onSpeechCompleted?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeechCompleted?.invoke()
                }
            })
        }
    }

    /**
     * Speaks formatted text with sentence-level streaming TTS and immediate audio playback.
     */
    fun speak(text: String, sttEndTime: Long = 0L, onDone: (() -> Unit)? = null) {
        if (isMuted || text.isBlank()) {
            onDone?.invoke()
            return
        }

        stop() // Immediate barge-in cutoff of any playing audio

        val cleaned = SpokenResponseFormatter.formatForSpeech(text)
        if (cleaned.isBlank()) {
            onDone?.invoke()
            return
        }

        val requestStart = System.currentTimeMillis()

        // If ElevenLabs is configured, use low-latency chunked streaming
        if (elevenLabsProvider != null && elevenLabsProvider.isConfigured()) {
            val chunks = SpokenResponseFormatter.splitIntoStreamingChunks(cleaned)
            val config = if (activeIdentity.equals("MYRAA", true)) myraaConfig else jarvisConfig

            currentStreamJob = speechScope.launch {
                try {
                    for (i in chunks.indices) {
                        val chunk = chunks[i]
                        val cacheFile = File(context.cacheDir, "stream_${System.currentTimeMillis()}_$i.mp3")
                        val success = elevenLabsProvider.synthesizeStreamChunk(chunk, config, cacheFile)

                        if (success && cacheFile.exists() && cacheFile.length() > 0) {
                            if (i == 0) {
                                val firstAudioTime = System.currentTimeMillis()
                                val totalLatency = if (sttEndTime > 0) firstAudioTime - sttEndTime else firstAudioTime - requestStart
                                _latencyMetrics.value = VoiceLatencyMetrics(
                                    sttEndTimestamp = sttEndTime,
                                    ttsRequestTimestamp = requestStart,
                                    ttsFirstAudioTimestamp = firstAudioTime,
                                    totalResponseStartLatencyMs = totalLatency
                                )
                            }
                            audioPlayQueue.add(cacheFile)
                            if (!isPlayingQueue) {
                                playNextInQueue(onDone)
                            }
                        } else {
                            // If chunk failed, fallback to native speech for this chunk
                            withContext(Dispatchers.Main) {
                                speakNative(chunk, onDone)
                            }
                            break
                        }
                    }
                } catch (e: CancellationException) {
                    clearPlayQueue()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        speakNative(cleaned, onDone)
                    }
                }
            }
        } else {
            speakNative(cleaned, onDone)
        }
    }

    private fun playNextInQueue(onDone: (() -> Unit)?) {
        val file = audioPlayQueue.poll()
        if (file == null) {
            isPlayingQueue = false
            onDone?.invoke()
            onSpeechCompleted?.invoke()
            return
        }

        isPlayingQueue = true
        try {
            onSpeechStarted?.invoke()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .build()
                )
                setDataSource(context, Uri.fromFile(file))
                prepare()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    file.delete()
                    playNextInQueue(onDone)
                }
                setOnErrorListener { _, _, _ ->
                    file.delete()
                    mediaPlayer = null
                    playNextInQueue(onDone)
                    true
                }
                start()
            }
        } catch (e: Exception) {
            file.delete()
            playNextInQueue(onDone)
        }
    }

    private fun clearPlayQueue() {
        while (audioPlayQueue.isNotEmpty()) {
            val f = audioPlayQueue.poll()
            f?.delete()
        }
        isPlayingQueue = false
    }

    private fun speakNative(text: String, onDone: (() -> Unit)?) {
        if (!isNativeTtsInitialized) {
            onDone?.invoke()
            return
        }
        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        this.onSpeechCompleted = onDone
        nativeTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        currentStreamJob?.cancel()
        currentStreamJob = null
        clearPlayQueue()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {}

        if (isNativeTtsInitialized) {
            nativeTts?.stop()
        }
    }

    fun shutdown() {
        stop()
        nativeTts?.shutdown()
        nativeTts = null
        isNativeTtsInitialized = false
    }
}
