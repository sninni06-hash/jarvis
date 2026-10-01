package com.example.ai

import com.example.ai.models.AIRequest
import com.example.ai.models.AIResponse

class AIProviderManager(
    val geminiProvider: GeminiProvider = GeminiProvider(),
    val openAIProvider: OpenAIProvider = OpenAIProvider(),
    val grokProvider: GrokProvider = GrokProvider()
) {
    private val providers = mapOf(
        geminiProvider.providerId to geminiProvider,
        openAIProvider.providerId to openAIProvider,
        grokProvider.providerId to grokProvider
    )

    fun getProvider(id: String): AIProvider {
        return providers[id] ?: geminiProvider
    }

    suspend fun executeWithFailover(
        request: AIRequest,
        primaryProviderId: String = "gemini",
        backupProviderId: String = "openai",
        primaryModel: String? = null,
        backupModel: String? = null
    ): AIResponse {
        val primary = getProvider(primaryProviderId)
        val primaryResponse = primary.generateContent(request, primaryModel)

        if (primaryResponse.isSuccess) {
            return primaryResponse
        }

        // Try backup provider if primary failed
        val backup = getProvider(backupProviderId)
        if (backup.isConfigured()) {
            val backupResponse = backup.generateContent(request, backupModel)
            if (backupResponse.isSuccess) {
                return backupResponse
            }
        }

        // If both failed or unavailable, return the primary response with helpful error or offline intelligence
        return primaryResponse
    }
}
