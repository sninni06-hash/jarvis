package com.example.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Track screen changes
    }

    override fun onInterrupt() {
        // Accessibility interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    fun getVisibleScreenText(): String {
        val root = rootInActiveWindow ?: return "Screen content unavailable or accessibility disabled."
        val builder = StringBuilder()
        traverseNode(root, builder)
        return builder.toString().trim()
    }

    private fun traverseNode(node: AccessibilityNodeInfo, sb: StringBuilder) {
        node.text?.let { if (it.isNotBlank()) sb.append(it).append(" | ") }
        node.contentDescription?.let { if (it.isNotBlank()) sb.append(it).append(" | ") }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, sb)
        }
    }

    fun clickElementWithText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        for (node in nodes) {
            if (node.isClickable) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                parent = parent.parent
            }
        }
        return false
    }

    fun scroll(directionDown: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (directionDown) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return root.performAction(action)
    }

    fun performBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set

        val isServiceRunning: Boolean get() = instance != null
    }
}
