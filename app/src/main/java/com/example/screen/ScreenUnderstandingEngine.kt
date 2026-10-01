package com.example.screen

import com.example.services.JarvisAccessibilityService

data class ScreenInspectionResult(
    val visibleText: String,
    val isAccessibilityActive: Boolean,
    val summary: String
)

class ScreenUnderstandingEngine {

    fun inspectCurrentScreen(): ScreenInspectionResult {
        val service = JarvisAccessibilityService.instance
        if (service == null) {
            return ScreenInspectionResult(
                visibleText = "",
                isAccessibilityActive = false,
                summary = "Accessibility Service is not enabled. Please enable JARVIS in Android Accessibility settings to inspect and understand screen elements."
            )
        }

        val text = service.getVisibleScreenText()
        val summary = if (text.isNotBlank()) {
            "Captured Screen Context: ${text.take(300)}..."
        } else {
            "Current screen has no readable text or is in secure mode."
        }

        return ScreenInspectionResult(
            visibleText = text,
            isAccessibilityActive = true,
            summary = summary
        )
    }
}
