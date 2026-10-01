package com.example.ai

import com.example.ai.models.AIRequest
import com.example.ai.models.AIResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GrokProvider(private val apiKeyProvider: () -> String = { "" }) : AIProvider {
    override val providerId: String = "grok"
    override val displayName: String = "xAI Grok"
    override val supportedModels: List<String> = listOf("grok-2", "grok-2-mini", "grok-beta")

    override fun isConfigured(): Boolean = apiKeyProvider().isNotBlank()

    override suspend fun generateContent(request: AIRequest, model: String?): AIResponse = withContext(Dispatchers.IO) {
        val targetModel = model ?: "grok-2-mini"
        if (!isConfigured()) {
            return@withContext AIResponse(
                text = "Grok API key not configured.",
                modelUsed = targetModel,
                isSuccess = false,
                errorMessage = "API key missing"
            )
        }
        AIResponse(
            text = "Grok provider ready for task execution.",
            modelUsed = targetModel,
            isSuccess = true
        )
    }
}
