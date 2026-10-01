package com.example.ai

import com.example.ai.models.AIRequest
import com.example.ai.models.AIResponse

interface AIProvider {
    val providerId: String
    val displayName: String
    val supportedModels: List<String>
    suspend fun generateContent(request: AIRequest, model: String? = null): AIResponse
    fun isConfigured(): Boolean
}
