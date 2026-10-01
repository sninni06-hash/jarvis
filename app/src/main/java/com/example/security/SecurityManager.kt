package com.example.security

import com.example.planner.models.ActionRisk
import com.example.planner.models.IntentType
import com.example.planner.models.TaskIntent

class SecurityManager {

    var requireConfirmationForMediumRisk: Boolean = true

    fun assessRisk(intent: TaskIntent): ActionRisk {
        return when (intent.type) {
            IntentType.CALL_CONTACT,
            IntentType.SEND_SMS,
            IntentType.SEND_WHATSAPP,
            IntentType.SEND_INSTAGRAM_MESSAGE,
            IntentType.VIDEO_CALL -> ActionRisk.MEDIUM

            IntentType.PLAY_MUSIC,
            IntentType.OPEN_APP,
            IntentType.SEARCH_PLAY_STORE,
            IntentType.SET_TIMER,
            IntentType.SET_ALARM,
            IntentType.DEVICE_SETTING,
            IntentType.READ_SCREEN,
            IntentType.TAP_SCREEN,
            IntentType.SCROLL_SCREEN,
            IntentType.LOCATION,
            IntentType.WEATHER,
            IntentType.BATTERY,
            IntentType.CREATOR_INFO,
            IntentType.MUTE_CONTROL,
            IntentType.GENERAL_AI -> ActionRisk.LOW
        }
    }

    fun needsUserConfirmation(intent: TaskIntent): Boolean {
        val risk = assessRisk(intent)
        return when (risk) {
            ActionRisk.HIGH -> true
            ActionRisk.MEDIUM -> requireConfirmationForMediumRisk
            ActionRisk.LOW -> false
        }
    }
}
