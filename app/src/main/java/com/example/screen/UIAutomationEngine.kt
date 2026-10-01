package com.example.screen

import com.example.services.JarvisAccessibilityService

class UIAutomationEngine {

    fun clickTarget(targetText: String): Boolean {
        val service = JarvisAccessibilityService.instance ?: return false
        return service.clickElementWithText(targetText)
    }

    fun scroll(directionDown: Boolean): Boolean {
        val service = JarvisAccessibilityService.instance ?: return false
        return service.scroll(directionDown)
    }

    fun goBack(): Boolean {
        val service = JarvisAccessibilityService.instance ?: return false
        return service.performBack()
    }

    fun goHome(): Boolean {
        val service = JarvisAccessibilityService.instance ?: return false
        return service.performHome()
    }
}
