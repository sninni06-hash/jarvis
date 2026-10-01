package com.example.voice

import java.util.Locale

enum class WakeMode {
    JARVIS_ONLY,
    MYRAA_ONLY,
    BOTH,
    CUSTOM
}

class WakeWordManager {
    var wakeMode: WakeMode = WakeMode.BOTH
    var customWakePhrase: String = "Friday"

    private val wakeVariants = listOf(
        "Pranam Sir, kaise yaad kiya?",
        "Pranam Sir. Main ready hoon.",
        "Ji Sir, boliye.",
        "Yes Sir, main sun rahi hoon.",
        "Welcome back, Sir."
    )
    private var lastVariantIndex = 0

    fun getWakeAcknowledgment(isFixed: Boolean = false): String {
        if (isFixed) {
            return "Pranam Sir, kaise yaad kiya?"
        }
        val text = wakeVariants[lastVariantIndex % wakeVariants.size]
        lastVariantIndex++
        return text
    }

    /**
     * Checks if input contains an explicit wake-up phrase to awaken from DEACTIVATED state.
     * E.g. "Wake up Jarvis", "Wake up Myraa", "Wake up", "Jarvis utho", "Utho Myraa"
     */
    fun isWakeUpCommand(input: String): Pair<Boolean, String> {
        val lower = input.lowercase(Locale.ROOT).trim()

        val isWakeUp = lower.contains("wake up") ||
                lower.startsWith("utho") ||
                lower.contains("wake up jarvis") ||
                lower.contains("wake up myraa") ||
                lower == "jarvis" ||
                lower == "myraa"

        if (!isWakeUp) return Pair(false, input)

        val stripped = lower
            .replace("wake up", "")
            .replace("utho", "")
            .replace("jarvis", "")
            .replace("myraa", "")
            .replace("myra", "")
            .trim().removePrefix(",").removePrefix(".").trim()

        return Pair(true, stripped)
    }

    fun matchesWakeWord(input: String): Pair<Boolean, String> {
        val lower = input.lowercase(Locale.ROOT).trim()

        val matched = when (wakeMode) {
            WakeMode.JARVIS_ONLY -> lower.contains("jarvis")
            WakeMode.MYRAA_ONLY -> lower.contains("myraa") || lower.contains("myra")
            WakeMode.BOTH -> lower.contains("jarvis") || lower.contains("myraa") || lower.contains("myra")
            WakeMode.CUSTOM -> {
                val phrase = customWakePhrase.lowercase(Locale.ROOT).trim()
                phrase.isNotBlank() && lower.contains(phrase)
            }
        }

        if (!matched) return Pair(false, input)

        var stripped = lower
            .replace("wake up", "")
            .replace("jarvis", "")
            .replace("myraa", "")
            .replace("myra", "")
        if (wakeMode == WakeMode.CUSTOM) {
            stripped = stripped.replace(customWakePhrase.lowercase(Locale.ROOT), "")
        }
        stripped = stripped.trim().removePrefix(",").removePrefix(".").trim()

        return Pair(true, if (stripped.isNotBlank()) stripped else input)
    }
}
