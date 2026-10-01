package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.core.performance.PerformanceMode

class JarvisPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString("user_name", "Nitin") ?: "Nitin"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var isFirstLaunchDone: Boolean
        get() = prefs.getBoolean("first_launch_done", false)
        set(value) = prefs.edit().putBoolean("first_launch_done", value).apply()

    var creatorName: String
        get() = prefs.getString("creator_name", "Nitin") ?: "Nitin"
        set(value) = prefs.edit().putString("creator_name", value).apply()

    var assistantIdentity: String
        get() = prefs.getString("assistant_identity", "BOTH") ?: "BOTH"
        set(value) = prefs.edit().putString("assistant_identity", value).apply()

    var activeVoiceIdentity: String // "JARVIS" or "MYRAA"
        get() = prefs.getString("active_voice_identity", "JARVIS") ?: "JARVIS"
        set(value) = prefs.edit().putString("active_voice_identity", value).apply()

    var customWakeName: String
        get() = prefs.getString("custom_wake_name", "Friday") ?: "Friday"
        set(value) = prefs.edit().putString("custom_wake_name", value).apply()

    var isContinuousListening: Boolean
        get() = prefs.getBoolean("continuous_listening", true)
        set(value) = prefs.edit().putBoolean("continuous_listening", value).apply()

    var isStartupAnimationEnabled: Boolean
        get() = prefs.getBoolean("startup_anim", true)
        set(value) = prefs.edit().putBoolean("startup_anim", value).apply()

    var isFloatingIconEnabled: Boolean
        get() = prefs.getBoolean("floating_icon", true)
        set(value) = prefs.edit().putBoolean("floating_icon", value).apply()

    // Gemini Configuration
    var geminiApiKey: String
        get() {
            val saved = prefs.getString("gemini_api_key", "").orEmpty()
            if (saved.isNotBlank()) return saved
            return try {
                val bcKey = BuildConfig.GEMINI_API_KEY
                if (bcKey.isNotBlank() && !bcKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) bcKey else ""
            } catch (e: Throwable) {
                ""
            }
        }
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()

    var geminiModel: String
        get() = prefs.getString("gemini_model", "gemini-2.5-flash") ?: "gemini-2.5-flash"
        set(value) = prefs.edit().putString("gemini_model", value).apply()

    var geminiStatus: String
        get() = prefs.getString("gemini_status", "NOT_TESTED") ?: "NOT_TESTED"
        set(value) = prefs.edit().putString("gemini_status", value).apply()

    // OpenAI Configuration
    var openAiApiKey: String
        get() = prefs.getString("openai_api_key", "") ?: ""
        set(value) = prefs.edit().putString("openai_api_key", value).apply()

    var openAiModel: String
        get() = prefs.getString("openai_model", "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = prefs.edit().putString("openai_model", value).apply()

    var openAiStatus: String
        get() = prefs.getString("openai_status", "NOT_TESTED") ?: "NOT_TESTED"
        set(value) = prefs.edit().putString("openai_status", value).apply()

    // ElevenLabs Configuration
    var elevenLabsApiKey: String
        get() = prefs.getString("elevenlabs_api_key", "") ?: ""
        set(value) = prefs.edit().putString("elevenlabs_api_key", value).apply()

    var elevenLabsJarvisVoiceId: String
        get() = prefs.getString("eleven_jarvis_voice", "21m00Tcm4TlvDq8ikWAM") ?: "21m00Tcm4TlvDq8ikWAM" // Deep male
        set(value) = prefs.edit().putString("eleven_jarvis_voice", value).apply()

    var elevenLabsMyraaVoiceId: String
        get() = prefs.getString("eleven_myraa_voice", "EXAVITQu4vr4xnSDxMaL") ?: "EXAVITQu4vr4xnSDxMaL" // Warm female
        set(value) = prefs.edit().putString("eleven_myraa_voice", value).apply()

    var elevenLabsStatus: String
        get() = prefs.getString("elevenlabs_status", "NOT_TESTED") ?: "NOT_TESTED"
        set(value) = prefs.edit().putString("elevenlabs_status", value).apply()

    var elevenLabsTtsModel: String
        get() = prefs.getString("eleven_model", "eleven_flash_v2_5") ?: "eleven_flash_v2_5"
        set(value) = prefs.edit().putString("eleven_model", value).apply()

    var jarvisStability: Float
        get() = prefs.getFloat("jarvis_stability", 0.45f)
        set(value) = prefs.edit().putFloat("jarvis_stability", value).apply()

    var jarvisSimilarity: Float
        get() = prefs.getFloat("jarvis_similarity", 0.75f)
        set(value) = prefs.edit().putFloat("jarvis_similarity", value).apply()

    var jarvisStyle: Float
        get() = prefs.getFloat("jarvis_style", 0.05f)
        set(value) = prefs.edit().putFloat("jarvis_style", value).apply()

    var myraaStability: Float
        get() = prefs.getFloat("myraa_stability", 0.38f)
        set(value) = prefs.edit().putFloat("myraa_stability", value).apply()

    var myraaSimilarity: Float
        get() = prefs.getFloat("myraa_similarity", 0.80f)
        set(value) = prefs.edit().putFloat("myraa_similarity", value).apply()

    var myraaStyle: Float
        get() = prefs.getFloat("myraa_style", 0.15f)
        set(value) = prefs.edit().putFloat("myraa_style", value).apply()

    var speakerBoost: Boolean
        get() = prefs.getBoolean("speaker_boost", true)
        set(value) = prefs.edit().putBoolean("speaker_boost", value).apply()

    var isClapDeactivationEnabled: Boolean
        get() = prefs.getBoolean("clap_deactivation", true)
        set(value) = prefs.edit().putBoolean("clap_deactivation", value).apply()

    var clapSensitivity: String
        get() = prefs.getString("clap_sensitivity", "MEDIUM") ?: "MEDIUM"
        set(value) = prefs.edit().putString("clap_sensitivity", value).apply()

    var wakeAcknowledgmentMode: String
        get() = prefs.getString("wake_ack_mode", "NATURAL_VARIATION") ?: "NATURAL_VARIATION"
        set(value) = prefs.edit().putString("wake_ack_mode", value).apply()

    var primaryAIProvider: String
        get() = prefs.getString("primary_ai", "gemini") ?: "gemini"
        set(value) = prefs.edit().putString("primary_ai", value).apply()

    var backupAIProvider: String
        get() = prefs.getString("backup_ai", "openai") ?: "openai"
        set(value) = prefs.edit().putString("backup_ai", value).apply()

    var personality: Personality
        get() {
            val name = prefs.getString("personality", Personality.PROFESSIONAL.name) ?: Personality.PROFESSIONAL.name
            return try { Personality.valueOf(name) } catch (e: Exception) { Personality.PROFESSIONAL }
        }
        set(value) = prefs.edit().putString("personality", value.name).apply()

    // Performance Mode for Low-End vs Flagship Devices
    var performanceMode: PerformanceMode
        get() {
            val name = prefs.getString("performance_mode", PerformanceMode.PERFORMANCE.name) ?: PerformanceMode.PERFORMANCE.name
            return try { PerformanceMode.valueOf(name) } catch (e: Exception) { PerformanceMode.PERFORMANCE }
        }
        set(value) = prefs.edit().putString("performance_mode", value.name).apply()

    var isMemoryEnabled: Boolean
        get() = prefs.getBoolean("memory_enabled", true)
        set(value) = prefs.edit().putBoolean("memory_enabled", value).apply()

    var isDeveloperMode: Boolean
        get() = prefs.getBoolean("developer_mode", false)
        set(value) = prefs.edit().putBoolean("developer_mode", value).apply()

    var isMuted: Boolean
        get() = prefs.getBoolean("is_muted", false)
        set(value) = prefs.edit().putBoolean("is_muted", value).apply()

    var requireMediumRiskConfirmation: Boolean
        get() = prefs.getBoolean("confirm_medium_risk", false) // Direct execution for snappy response
        set(value) = prefs.edit().putBoolean("confirm_medium_risk", value).apply()

    var speechSpeed: Float
        get() = prefs.getFloat("speech_speed", 1.05f)
        set(value) = prefs.edit().putFloat("speech_speed", value).apply()

    var speechPitch: Float
        get() = prefs.getFloat("speech_pitch", 1.0f)
        set(value) = prefs.edit().putFloat("speech_pitch", value).apply()

    var isScreenShareActive: Boolean
        get() = prefs.getBoolean("screen_share_active", false)
        set(value) = prefs.edit().putBoolean("screen_share_active", value).apply()
}
