package com.example.voice

import java.util.regex.Pattern

object SpokenResponseFormatter {

    private val MARKDOWN_BOLD_ITALIC = Pattern.compile("(\\*\\*|\\*|_|~~)")
    private val MARKDOWN_HEADERS = Pattern.compile("(?m)^#{1,6}\\s*")
    private val MARKDOWN_BULLETS = Pattern.compile("(?m)^(\\s*[-*+]|\\s*\\d+\\.)\\s+")
    private val CODE_BLOCKS = Pattern.compile("```[\\s\\S]*?```|`[^`]+`")
    private val URL_PATTERN = Pattern.compile("https?://\\S+")
    private val SYSTEM_LABELS = Pattern.compile("(?i)\\b(LISTENING|THINKING|EXECUTING|ONLINE|OFFLINE|COMMAND RECEIVED|TASK COMPLETE|VERIFYING|SUCCESS|FAILED|SYSTEM OVERVIEW)\\b:?")

    /**
     * Cleans raw AI or system text into natural conversational spoken speech.
     */
    fun formatForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        var text = rawText

        // 1. Strip Code blocks & URLs
        text = CODE_BLOCKS.matcher(text).replaceAll("")
        text = URL_PATTERN.matcher(text).replaceAll("")

        // 2. Strip Markdown
        text = MARKDOWN_HEADERS.matcher(text).replaceAll("")
        text = MARKDOWN_BULLETS.matcher(text).replaceAll("")
        text = MARKDOWN_BOLD_ITALIC.matcher(text).replaceAll("")

        // 3. Strip System Labels
        text = SYSTEM_LABELS.matcher(text).replaceAll("")

        // 4. Transform robotic phrases to natural conversational phrasing
        text = text
            .replace("Command received. Battery level is", "Battery")
            .replace("Battery level is", "Battery")
            .replace("The operation has been completed successfully.", "Ho gaya.")
            .replace("The operation was completed successfully.", "Ho gaya.")
            .replace("Task completed successfully.", "Ho gaya.")
            .replace("Action completed.", "Ho gaya.")
            .replace("I have initiated the call to", "Call laga diya hai")
            .replace("Initiating call to", "Call kar rahi hoon")
            .replace("Opening application", "Khol rahi hoon")
            .replace("Setting alarm for", "Alarm laga diya")
            .replace("Setting timer for", "Timer laga diya")

        // 5. Symbol normalizations for spoken audio
        text = text
            .replace("%", " percent")
            .replace("°C", " degree celsius")
            .replace("&", " aur ")
            .replace("@", " at ")

        // 6. Clean whitespace and repeated punctuation
        text = text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("([.!?])\\1+"), "$1")
            .replace("..", ".")
            .trim()

        // 7. Limit repetitive "Sir"
        val sirMatches = Regex("(?i)\\bSir\\b").findAll(text).toList()
        if (sirMatches.size > 1) {
            // Keep only the first "Sir" and replace subsequent ones
            var count = 0
            text = Regex("(?i)\\bSir\\b").replace(text) {
                count++
                if (count == 1) it.value else ""
            }
            text = text.replace(Regex("\\s+"), " ").trim()
        }

        return text
    }

    /**
     * Splits long spoken text into conversational sentence/phrase chunks for low-latency streaming TTS.
     * Starts audio playback on the very first chunk without waiting for the full response!
     */
    fun splitIntoStreamingChunks(formattedText: String): List<String> {
        if (formattedText.isBlank()) return emptyList()

        // Split on sentence boundaries: . ! ? or newlines
        val rawSentences = formattedText.split(Regex("(?<=[.!?\\n])\\s+"))
        val result = mutableListOf<String>()

        for (s in rawSentences) {
            val trimmed = s.trim()
            if (trimmed.isEmpty()) continue

            // If a sentence is very long (> 120 chars), break at commas or conjunctions for low latency
            if (trimmed.length > 120 && trimmed.contains(",")) {
                val subChunks = trimmed.split(Regex("(?<=,)\\s+"))
                for (sub in subChunks) {
                    val subTrimmed = sub.trim()
                    if (subTrimmed.isNotEmpty()) {
                        result.add(subTrimmed)
                    }
                }
            } else {
                result.add(trimmed)
            }
        }

        return if (result.isEmpty()) listOf(formattedText) else result
    }
}
