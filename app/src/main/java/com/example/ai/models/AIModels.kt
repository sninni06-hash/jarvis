package com.example.ai.models

data class AIRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val conversationHistory: List<Pair<String, String>> = emptyList(), // Pair(role, text)
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024
)

data class AIResponse(
    val text: String,
    val modelUsed: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val latencyMs: Long = 0L
)
