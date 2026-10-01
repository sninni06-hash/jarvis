package com.example.planner

import com.example.planner.models.ActionRisk
import com.example.planner.models.IntentType
import com.example.planner.models.TaskIntent
import java.util.Locale

object CommandRouter {

    fun parseIntent(input: String): TaskIntent {
        val text = input.trim()
        val lower = text.lowercase(Locale.ROOT)

        // Creator questions (MANDATORY REQUIREMENT)
        if (lower.contains("who created you") || lower.contains("who made you") ||
            lower.contains("who is your creator") || lower.contains("kisne banaya") ||
            lower.contains("tumhe kisne banaya") || lower.contains("tera creator") ||
            lower.contains("who is your developer")
        ) {
            return TaskIntent(
                type = IntentType.CREATOR_INFO,
                target = "Nitin",
                confidence = 1.0f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Mute / Unmute
        if (lower.contains("mute") || lower.contains("chup ho jao") || lower.contains("silent") || lower.contains("unmute")) {
            val isUnmute = lower.contains("unmute") || lower.contains("bol")
            return TaskIntent(
                type = IntentType.MUTE_CONTROL,
                target = if (isUnmute) "UNMUTE" else "MUTE",
                confidence = 0.98f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Battery / Power
        if (lower.contains("battery") || lower.contains("charging") || lower.contains("charge pe") || lower.contains("charge kitna")) {
            return TaskIntent(
                type = IntentType.BATTERY,
                confidence = 0.98f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Weather / Temperature
        if (lower.contains("weather") || lower.contains("temperature") || lower.contains("mausam") || lower.contains("barish") || lower.contains("thand") || lower.contains("garmi")) {
            return TaskIntent(
                type = IntentType.WEATHER,
                confidence = 0.96f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Location
        if (lower.contains("location") || lower.contains("where am i") || lower.contains("kahan hoon") || lower.contains("meri location")) {
            return TaskIntent(
                type = IntentType.LOCATION,
                confidence = 0.96f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Screen Understanding / UI Actions
        if (lower.contains("screen pe jo hai") || lower.contains("screen dekh") || lower.contains("screen read") ||
            lower.contains("kya likha hai") || lower.contains("screenshot samjho") || lower.contains("screen dekho")
        ) {
            return TaskIntent(
                type = IntentType.READ_SCREEN,
                confidence = 0.95f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        if (lower.contains("scroll") || lower.contains("neeche scroll") || lower.contains("upar scroll")) {
            val direction = if (lower.contains("upar") || lower.contains("up")) "UP" else "DOWN"
            return TaskIntent(
                type = IntentType.SCROLL_SCREEN,
                target = direction,
                confidence = 0.95f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        if (lower.contains("button dabao") || lower.contains("press karo") || lower.contains("tap karo") || lower.contains("click")) {
            val target = text.replace(Regex("(?i).*button|press|karo|tap|click|pe"), "").trim()
            return TaskIntent(
                type = IntentType.TAP_SCREEN,
                target = target.ifBlank { "primary_action" },
                confidence = 0.90f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Timer: e.g. "30 minute ka timer laga", "1 minute ka timer", "5 min timer"
        if (lower.contains("timer")) {
            val minutesMatch = Regex("(\\d+)\\s*(?:minute|min|m|second|sec|s)?").find(lower)
            val minutes = minutesMatch?.groupValues?.get(1) ?: "5"
            return TaskIntent(
                type = IntentType.SET_TIMER,
                payload = minutes,
                confidence = 0.97f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Alarm: e.g. "kal subah 7:15 ka alarm", "kal 7 baje alarm", "2:22 ka alarm", "7:15 AM ka alarm"
        if (lower.contains("alarm")) {
            val timeMatch = Regex("(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm|baje|subah|shaam)?)").find(lower)
            val alarmTime = timeMatch?.groupValues?.get(1)?.trim() ?: "07:00"
            return TaskIntent(
                type = IntentType.SET_ALARM,
                payload = alarmTime,
                confidence = 0.96f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Video Call
        if (lower.contains("video call")) {
            val isWhatsApp = lower.contains("whatsapp")
            val isInstagram = lower.contains("instagram") || lower.contains("insta")
            val contact = extractContactName(text, listOf("video call", "ko", "pe", "kar", "karo", "laga", "karna"))
            return TaskIntent(
                type = IntentType.VIDEO_CALL,
                target = contact.ifBlank { "Vivek" },
                payload = if (isInstagram) "INSTAGRAM" else "WHATSAPP",
                confidence = 0.92f,
                risk = ActionRisk.MEDIUM,
                rawQuery = text
            )
        }

        // WhatsApp: e.g. "Vivek ko WhatsApp karo", "Vivek ko WhatsApp pe msg kar", "WhatsApp kholo"
        if (lower.contains("whatsapp")) {
            if (lower == "whatsapp kholo" || lower == "open whatsapp" || lower == "whatsapp open") {
                return TaskIntent(
                    type = IntentType.OPEN_APP,
                    target = "WhatsApp",
                    confidence = 0.99f,
                    risk = ActionRisk.LOW,
                    rawQuery = text
                )
            }
            val contact = extractContactName(text, listOf("whatsapp", "pe", "ko", "msg", "message", "bhejo", "kar", "karo", "bol", "ki"))
            val message = extractMessagePayload(text)
            return TaskIntent(
                type = IntentType.SEND_WHATSAPP,
                target = contact.ifBlank { "Vivek" },
                payload = message,
                confidence = 0.95f,
                risk = ActionRisk.MEDIUM,
                rawQuery = text
            )
        }

        // SMS: e.g. "Vivek ko SMS bhejo", "Vivek ko message karo"
        if (lower.contains("sms") || (lower.contains("message") && !lower.contains("whatsapp") && !lower.contains("instagram"))) {
            val contact = extractContactName(text, listOf("sms", "message", "msg", "ko", "bhejo", "karo", "kar", "bol"))
            val message = extractMessagePayload(text)
            return TaskIntent(
                type = IntentType.SEND_SMS,
                target = contact.ifBlank { "Vivek" },
                payload = message,
                confidence = 0.93f,
                risk = ActionRisk.MEDIUM,
                rawQuery = text
            )
        }

        // Instagram: e.g. "Instagram kholo", "Instagram pe Vivek ko message"
        if (lower.contains("instagram") || lower.contains("insta")) {
            if (lower.contains("msg") || lower.contains("message") || lower.contains("dm")) {
                val contact = extractContactName(text, listOf("instagram", "insta", "pe", "ko", "message", "msg", "dm", "karo"))
                val message = extractMessagePayload(text)
                return TaskIntent(
                    type = IntentType.SEND_INSTAGRAM_MESSAGE,
                    target = contact.ifBlank { "Vivek" },
                    payload = message,
                    confidence = 0.92f,
                    risk = ActionRisk.MEDIUM,
                    rawQuery = text
                )
            }
            return TaskIntent(
                type = IntentType.OPEN_APP,
                target = "Instagram",
                confidence = 0.98f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Phone Call: e.g. "Vivek ko call kro", "Vivek ko phone laga", "Call Vivek", "Vivek calling", "bhai Vivek ko call"
        if (lower.contains("call") || lower.contains("phone laga") || lower.contains("dial")) {
            val contact = extractContactName(text, listOf("call", "phone", "laga", "lagao", "kro", "karo", "kar", "bhai", "ko", "calling", "dial"))
            return TaskIntent(
                type = IntentType.CALL_CONTACT,
                target = contact.ifBlank { "Vivek" },
                confidence = 0.97f,
                risk = ActionRisk.MEDIUM,
                rawQuery = text
            )
        }

        // Music: e.g. "koi Arijit Singh ka song chala", "Spotify pe Believer play karo", "YouTube pe song play karo"
        if (lower.contains("song") || lower.contains("gaana") || lower.contains("music") || lower.contains("play") || lower.contains("chala")) {
            val app = when {
                lower.contains("spotify") -> "Spotify"
                lower.contains("youtube") -> "YouTube"
                else -> "Default"
            }
            val query = text.replace(Regex("(?i)(koi|ka|ke|song|gaana|music|play|chala|laga|chalao|karo|kar|pe|spotify|youtube)"), "").trim()
            return TaskIntent(
                type = IntentType.PLAY_MUSIC,
                target = query.ifBlank { "Arijit Singh" },
                payload = app,
                confidence = 0.95f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Play Store search / install: e.g. "Play Store kholo", "Instagram install karo", "WhatsApp search karo"
        if (lower.contains("play store") || lower.contains("install")) {
            val app = text.replace(Regex("(?i)(play store|kholo|open|pe|search|install|karo|kar|is app ko)"), "").trim()
            return TaskIntent(
                type = IntentType.SEARCH_PLAY_STORE,
                target = app.ifBlank { "WhatsApp" },
                confidence = 0.94f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Device settings & controls: e.g. "WiFi on kar", "Bluetooth settings kholo", "Flashlight on"
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("bluetooth") ||
            lower.contains("flashlight") || lower.contains("torch") || lower.contains("airplane mode") ||
            lower.contains("volume") || lower.contains("settings")
        ) {
            val setting = when {
                lower.contains("wifi") || lower.contains("wi-fi") -> "WIFI"
                lower.contains("bluetooth") -> "BLUETOOTH"
                lower.contains("flashlight") || lower.contains("torch") -> "FLASHLIGHT"
                lower.contains("airplane") -> "AIRPLANE_MODE"
                lower.contains("volume") -> "VOLUME"
                else -> "SETTINGS"
            }
            val action = if (lower.contains("off") || lower.contains("band")) "OFF" else "ON"
            return TaskIntent(
                type = IntentType.DEVICE_SETTING,
                target = setting,
                payload = action,
                confidence = 0.96f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Open generic app: e.g. "Camera kholo", "Calculator open karo"
        if (lower.contains("kholo") || lower.contains("open") || lower.contains("launch")) {
            val app = text.replace(Regex("(?i)(kholo|open|launch|kar|karo|app)"), "").trim()
            return TaskIntent(
                type = IntentType.OPEN_APP,
                target = app,
                confidence = 0.90f,
                risk = ActionRisk.LOW,
                rawQuery = text
            )
        }

        // Fallback to General AI
        return TaskIntent(
            type = IntentType.GENERAL_AI,
            payload = text,
            confidence = 0.85f,
            risk = ActionRisk.LOW,
            rawQuery = text
        )
    }

    private fun extractContactName(text: String, keywordsToStrip: List<String>): String {
        var clean = text
        for (kw in keywordsToStrip) {
            clean = clean.replace(Regex("(?i)\\b$kw\\b"), " ")
        }
        return clean.trim().split(Regex("\\s+")).firstOrNull()?.replaceFirstChar { it.uppercase() }.orEmpty()
    }

    private fun extractMessagePayload(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        val indicators = listOf("bol ki", "bol dena", "bhejo ki", "message:", "bol", "msg")
        for (ind in indicators) {
            val idx = lower.indexOf(ind)
            if (idx != -1) {
                val candidate = text.substring(idx + ind.length).trim()
                if (candidate.isNotBlank()) return candidate
            }
        }
        return "I am on my way."
    }
}
