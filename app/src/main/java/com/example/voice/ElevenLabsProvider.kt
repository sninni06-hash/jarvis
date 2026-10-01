package com.example.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class VoiceProfileConfig(
    val voiceId: String,
    val modelId: String = "eleven_flash_v2_5",
    val stability: Float = 0.45f,
    val similarityBoost: Float = 0.75f,
    val style: Float = 0.05f,
    val useSpeakerBoost: Boolean = true
)

class ElevenLabsProvider(
    private val keyProvider: () -> String = { "" }
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean = keyProvider().trim().isNotBlank()

    suspend fun testConnection(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = apiKey.trim().ifBlank { keyProvider().trim() }
        if (key.isBlank()) return@withContext Pair(false, "ElevenLabs API Key is empty.")

        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("https://api.elevenlabs.io/v1/user")
                .addHeader("xi-api-key", key)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - start
            if (response.isSuccessful) {
                Pair(true, "ElevenLabs Connected (${latency}ms)")
            } else {
                Pair(false, "HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Pair(false, "Connection error: ${e.message}")
        }
    }

    /**
     * Synthesizes audio using ElevenLabs low-latency conversational streaming endpoint.
     * Uses optimize_streaming_latency=3 for near-instant first audio chunk.
     */
    suspend fun synthesizeStreamChunk(
        text: String,
        config: VoiceProfileConfig,
        destinationFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        val key = keyProvider().trim()
        if (key.isBlank() || text.isBlank() || config.voiceId.isBlank()) return@withContext false

        try {
            // Low-latency streaming endpoint with latency optimization flag
            val url = "https://api.elevenlabs.io/v1/text-to-speech/${config.voiceId}/stream?optimize_streaming_latency=3"

            val json = JSONObject().apply {
                put("text", text)
                put("model_id", config.modelId.ifBlank { "eleven_flash_v2_5" })
                put("voice_settings", JSONObject().apply {
                    put("stability", config.stability.toDouble())
                    put("similarity_boost", config.similarityBoost.toDouble())
                    put("style", config.style.toDouble())
                    put("use_speaker_boost", config.useSpeakerBoost)
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", key)
                .addHeader("Accept", "audio/mpeg")
                .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destinationFile.exists() && destinationFile.length() > 0
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
