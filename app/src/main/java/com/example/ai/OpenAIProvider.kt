package com.example.ai

import com.example.ai.models.AIRequest
import com.example.ai.models.AIResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAIProvider(private val apiKeyProvider: () -> String = { "" }) : AIProvider {
    override val providerId: String = "openai"
    override val displayName: String = "OpenAI ChatGPT"
    override val supportedModels: List<String> = listOf("gpt-4o", "gpt-4o-mini", "o3-mini")

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    override fun isConfigured(): Boolean = apiKeyProvider().trim().isNotBlank()

    suspend fun testConnection(apiKey: String, model: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = apiKey.trim().ifBlank { apiKeyProvider().trim() }
        if (key.isBlank()) return@withContext Pair(false, "OpenAI API key is empty.")

        val start = System.currentTimeMillis()
        try {
            val json = JSONObject().apply {
                put("model", model.ifBlank { "gpt-4o-mini" })
                put("messages", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("content", "ping")
                }))
                put("max_tokens", 5)
            }
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $key")
                .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - start
            if (response.isSuccessful) {
                Pair(true, "Connected successfully (${latency}ms)")
            } else {
                Pair(false, "HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Pair(false, "Error: ${e.message}")
        }
    }

    override suspend fun generateContent(request: AIRequest, model: String?): AIResponse = withContext(Dispatchers.IO) {
        val targetModel = model ?: "gpt-4o-mini"
        val key = apiKeyProvider().trim()

        if (key.isBlank()) {
            return@withContext AIResponse(
                text = "OpenAI key not configured in Settings.",
                modelUsed = targetModel,
                isSuccess = false,
                errorMessage = "API key missing"
            )
        }

        val start = System.currentTimeMillis()
        try {
            val json = JSONObject().apply {
                put("model", targetModel)
                val messages = JSONArray()
                if (!request.systemInstruction.isNullOrBlank()) {
                    messages.put(JSONObject().put("role", "system").put("content", request.systemInstruction))
                }
                request.conversationHistory.takeLast(10).forEach { (role, content) ->
                    val r = if (role.equals("USER", true)) "user" else "assistant"
                    messages.put(JSONObject().put("role", r).put("content", content))
                }
                messages.put(JSONObject().put("role", "user").put("content", request.prompt))
                put("messages", messages)
                put("temperature", request.temperature)
            }

            val httpRequest = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $key")
                .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            val latency = System.currentTimeMillis() - start
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val root = JSONObject(responseBody)
                val text = root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                AIResponse(text = text.trim(), modelUsed = targetModel, isSuccess = true, latencyMs = latency)
            } else {
                AIResponse(
                    text = "OpenAI request failed (${response.code}).",
                    modelUsed = targetModel,
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: $responseBody",
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            AIResponse(
                text = "Failed to reach OpenAI servers.",
                modelUsed = targetModel,
                isSuccess = false,
                errorMessage = e.message,
                latencyMs = System.currentTimeMillis() - start
            )
        }
    }
}
