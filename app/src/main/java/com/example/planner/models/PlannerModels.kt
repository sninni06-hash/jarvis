package com.example.planner.models

enum class ActionRisk {
    LOW,
    MEDIUM,
    HIGH
}

enum class VerificationStatus {
    PENDING,
    EXECUTING,
    SUCCESS,
    FAILED,
    PARTIAL,
    REQUIRES_USER
}

enum class IntentType {
    CALL_CONTACT,
    SEND_SMS,
    SEND_WHATSAPP,
    SEND_INSTAGRAM_MESSAGE,
    VIDEO_CALL,
    PLAY_MUSIC,
    OPEN_APP,
    SEARCH_PLAY_STORE,
    SET_TIMER,
    SET_ALARM,
    DEVICE_SETTING,
    READ_SCREEN,
    TAP_SCREEN,
    SCROLL_SCREEN,
    LOCATION,
    WEATHER,
    BATTERY,
    CREATOR_INFO,
    MUTE_CONTROL,
    GENERAL_AI
}

data class TaskIntent(
    val type: IntentType,
    val target: String? = null, // e.g. "Vivek", "Spotify", "Flashlight", "WiFi"
    val payload: String? = null, // e.g. message text, minutes, time
    val confidence: Float = 0.95f,
    val risk: ActionRisk = ActionRisk.LOW,
    val rawQuery: String = ""
)

data class TaskStep(
    val stepNumber: Int,
    val totalSteps: Int,
    val title: String,
    val description: String,
    val intent: TaskIntent,
    var status: VerificationStatus = VerificationStatus.PENDING,
    var resultMessage: String? = null
)

data class TaskPlan(
    val planId: String,
    val userGoal: String,
    val steps: List<TaskStep>,
    var currentStepIndex: Int = 0,
    var isCancelled: Boolean = false
) {
    val isComplete: Boolean get() = currentStepIndex >= steps.size || steps.all { it.status == VerificationStatus.SUCCESS }
    val currentStep: TaskStep? get() = steps.getOrNull(currentStepIndex)
}
