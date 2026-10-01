package com.example.ai

import com.example.BuildConfig
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

class GeminiProvider(
    private val keyProvider: () -> String = { "" }
) : AIProvider {
    override val providerId: String = "gemini"
    override val displayName: String = "Google Gemini"
    override val supportedModels: List<String> = listOf(
        "gemini-2.5-flash",
        "gemini-3.5-flash",
        "gemini-3.1-pro-preview"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(): String {
        val userKey = keyProvider().trim()
        if (userKey.isNotBlank()) return userKey
        return try {
            val k = BuildConfig.GEMINI_API_KEY
            if (k.isNotBlank() && !k.equals("MY_GEMINI_API_KEY", ignoreCase = true)) k else ""
        } catch (e: Throwable) {
            ""
        }
    }

    override fun isConfigured(): Boolean {
        val k = getEffectiveApiKey()
        return k.isNotBlank() && !k.equals("MY_GEMINI_API_KEY", ignoreCase = true)
    }

    suspend fun testConnection(apiKey: String, model: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = apiKey.ifBlank { getEffectiveApiKey() }
        if (key.isBlank() || key.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            return@withContext Pair(false, "API Key is empty or placeholder.")
        }

        val start = System.currentTimeMillis()
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"
            val json = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", "ping")))
                }))
            }
            val request = Request.Builder()
                .url(url)
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
            Pair(false, "Connection error: ${e.message}")
        }
    }

    suspend fun analyzeScreenFrame(base64Jpeg: String, query: String, model: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) return@withContext "Screen vision requires configured Gemini API key in Settings."

        val targetModel = model ?: "gemini-2.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

        try {
            val json = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", "You are JARVIS / MYRAA vision engine. Identify UI elements, buttons, text, icons, Wi-Fi or settings from this screen frame. User asks: $query. Keep answer concise and actionable."))
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Jpeg)
                        })
                    })
                }
                put("contents", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", parts)
                }))
            }

            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                val root = JSONObject(body)
                val cand = root.optJSONArray("candidates")?.optJSONObject(0)
                cand?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                    ?: "No visual elements recognized."
            } else {
                "Screen analysis failed (${response.code})"
            }
        } catch (e: Exception) {
            "Screen vision error: ${e.message}"
        }
    }

    override suspend fun generateContent(request: AIRequest, model: String?): AIResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val targetModel = model ?: "gemini-2.5-flash"
        val apiKey = getEffectiveApiKey()

        if (apiKey.isBlank() || apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            return@withContext AIResponse(
                text = "JARVIS local engine: Gemini key not configured. Using deterministic offline brain.",
                modelUsed = targetModel,
                isSuccess = false,
                errorMessage = "API key not configured",
                latencyMs = System.currentTimeMillis() - startTime
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()

                request.conversationHistory.takeLast(10).forEach { (role, msg) ->
                    val partRole = if (role.equals("USER", true)) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", partRole)
                        put("parts", JSONArray().put(JSONObject().put("text", msg)))
                    })
                }

                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", request.prompt)))
                })

                put("contents", contentsArray)

                if (!request.systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", request.systemInstruction)))
                    })
                }

                put("generationConfig", JSONObject().apply {
                    put("temperature", request.temperature)
                    put("maxOutputTokens", request.maxTokens)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string().orEmpty()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

                    AIResponse(
                        text = text.ifBlank { "Done, Sir." },
                        modelUsed = targetModel,
                        isSuccess = true,
                        latencyMs = latency
                    )
                } else {
                    AIResponse(
                        text = "Response was filtered or empty.",
                        modelUsed = targetModel,
                        isSuccess = false,
                        errorMessage = "Empty candidates",
                        latencyMs = latency
                    )
                }
            } else {
                AIResponse(
                    text = "Gemini service returned code ${response.code}.",
                    modelUsed = targetModel,
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: $responseBody",
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            AIResponse(
                text = "Network offline or unreachable. Executing locally.",
                modelUsed = targetModel,
                isSuccess = false,
                errorMessage = e.message,
                latencyMs = System.currentTimeMillis() - startTime
            )
        }
    }
}
