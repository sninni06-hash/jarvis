package com.example.settings

enum class Personality(
    val title: String,
    val description: String,
    val completionAffirmation: String
) {
    PROFESSIONAL("JARVIS Professional", "Polite, crisp British intelligence style", "Done, Sir."),
    FRIENDLY("Friendly", "Warm, casual, conversational and helpful", "All done! Glad I could help! 😊"),
    CARING("Caring", "Gentle, thoughtful, respectful and supportive", "Taken care of! Take care of yourself today. ❤️"),
    LOVE("Love / Affectionate", "Sweet, devoted, respectful and warm", "Done for you ❤️ Anything else you need?"),
    ROASTING("Roasting", "Playful, light teasing banter, never abusive", "Done! Try not to break anything else today. 😏"),
    STUDY("Study", "Focused, structured, educational and organized", "Task completed. Continuing research protocol."),
    CALM("Calm", "Relaxed, minimal, peaceful and quiet", "Task completed peacefully."),
    MOTIVATIONAL("Motivational", "High-energy, inspiring, confident and driven", "Crushed it! Next goal awaits, let's keep moving! ⚡"),
    CUSTOM("Custom", "Custom user-defined prompt persona", "Completed as configured.")
}
