package com.example.verification

import com.example.planner.models.IntentType
import com.example.planner.models.TaskIntent
import com.example.planner.models.VerificationStatus

data class VerificationReport(
    val status: VerificationStatus,
    val expected: String,
    val observed: String,
    val userFriendlyMessage: String
)

class VerificationEngine {

    fun verify(intent: TaskIntent, actionSucceeded: Boolean, extraDetails: String? = null): VerificationReport {
        return when (intent.type) {
            IntentType.CALL_CONTACT -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "Phone dialer/call active with ${intent.target}",
                        observed = "Call initiated",
                        userFriendlyMessage = "Calling ${intent.target}, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.FAILED,
                        expected = "Call initiated",
                        observed = "Call failed or permission missing",
                        userFriendlyMessage = "Could not place call to ${intent.target}. Please check Phone permission."
                    )
                }
            }
            IntentType.SEND_WHATSAPP -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "WhatsApp chat launched for ${intent.target} with message",
                        observed = "WhatsApp chat open with pre-filled text",
                        userFriendlyMessage = "WhatsApp chat opened for ${intent.target} with your message, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.FAILED,
                        expected = "WhatsApp launched",
                        observed = "WhatsApp not installed or intent failed",
                        userFriendlyMessage = "WhatsApp is not installed or could not be opened."
                    )
                }
            }
            IntentType.SEND_SMS -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "SMS composer active with message to ${intent.target}",
                        observed = "SMS prepared",
                        userFriendlyMessage = "SMS prepared for ${intent.target}, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.FAILED,
                        expected = "SMS composer",
                        observed = "Failed to launch SMS",
                        userFriendlyMessage = "Unable to open SMS composer."
                    )
                }
            }
            IntentType.SET_TIMER -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "Timer for ${intent.payload} minutes created",
                        observed = "AlarmClock timer dispatched",
                        userFriendlyMessage = "${intent.payload} minute timer is set, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.FAILED,
                        expected = "Timer created",
                        observed = "Timer intent failed",
                        userFriendlyMessage = "Could not set timer."
                    )
                }
            }
            IntentType.SET_ALARM -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "Alarm set for ${intent.payload}",
                        observed = "AlarmClock alarm dispatched",
                        userFriendlyMessage = "Alarm set for ${intent.payload}, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.FAILED,
                        expected = "Alarm set",
                        observed = "Alarm intent failed",
                        userFriendlyMessage = "Could not set alarm."
                    )
                }
            }
            IntentType.PLAY_MUSIC -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "Music player playback started for ${intent.target}",
                        observed = "Music intent dispatched",
                        userFriendlyMessage = "Playing ${intent.target}, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.FAILED,
                        expected = "Music playback",
                        observed = "Media player not found",
                        userFriendlyMessage = "Unable to start music playback."
                    )
                }
            }
            IntentType.DEVICE_SETTING -> {
                if (actionSucceeded) {
                    VerificationReport(
                        status = VerificationStatus.SUCCESS,
                        expected = "${intent.target} setting updated or opened",
                        observed = "Setting executed",
                        userFriendlyMessage = "${intent.target} has been updated, Sir."
                    )
                } else {
                    VerificationReport(
                        status = VerificationStatus.REQUIRES_USER,
                        expected = "Direct setting toggle",
                        observed = "Protected setting requires user confirmation",
                        userFriendlyMessage = "Opened ${intent.target} settings. Please toggle it directly."
                    )
                }
            }
            IntentType.CREATOR_INFO -> {
                VerificationReport(
                    status = VerificationStatus.SUCCESS,
                    expected = "Creator identity statement",
                    observed = "Identity verified",
                    userFriendlyMessage = "Mujhe Nitin ne create kiya hai."
                )
            }
            IntentType.MUTE_CONTROL -> {
                val isUnmute = intent.target == "UNMUTE"
                VerificationReport(
                    status = VerificationStatus.SUCCESS,
                    expected = "Voice state changed",
                    observed = if (isUnmute) "Unmuted" else "Muted",
                    userFriendlyMessage = if (isUnmute) "Voice restored, Sir." else "Voice muted, Sir."
                )
            }
            else -> {
                VerificationReport(
                    status = if (actionSucceeded) VerificationStatus.SUCCESS else VerificationStatus.FAILED,
                    expected = "Action completed",
                    observed = extraDetails ?: (if (actionSucceeded) "Success" else "Error"),
                    userFriendlyMessage = extraDetails ?: (if (actionSucceeded) "Done, Sir." else "Task could not be completed.")
                )
            }
        }
    }
}
