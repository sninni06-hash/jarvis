package com.example.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.services.JarvisAccessibilityService

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val androidPermission: String? = null,
    val isSystemSettings: Boolean = false
)

class PermissionManager(private val context: Context) {

    fun getPermissionsState(): List<PermissionItem> {
        val list = mutableListOf<PermissionItem>()

        // Microphone
        list.add(
            PermissionItem(
                id = "MIC",
                title = "Microphone",
                description = "Required for voice commands, wake word detection, and live speech",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
                androidPermission = Manifest.permission.RECORD_AUDIO
            )
        )

        // Contacts
        list.add(
            PermissionItem(
                id = "CONTACTS",
                title = "Contacts",
                description = "Required to find 'Vivek' and others for calls and WhatsApp messages",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED,
                androidPermission = Manifest.permission.READ_CONTACTS
            )
        )

        // Phone Calls
        list.add(
            PermissionItem(
                id = "CALLS",
                title = "Phone Calls",
                description = "Required to place hands-free phone calls when instructed",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED,
                androidPermission = Manifest.permission.CALL_PHONE
            )
        )

        // SMS
        list.add(
            PermissionItem(
                id = "SMS",
                title = "Send SMS",
                description = "Required to compose and send text messages via voice",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED,
                androidPermission = Manifest.permission.SEND_SMS
            )
        )

        // Location
        list.add(
            PermissionItem(
                id = "LOCATION",
                title = "Location",
                description = "Required to provide live local weather and coordinates",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
                androidPermission = Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        // Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(
                PermissionItem(
                    id = "NOTIFICATIONS",
                    title = "Notifications",
                    description = "Required to run foreground assistant and task execution alerts",
                    isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
                    androidPermission = Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }

        // Overlay / Floating Assistant
        list.add(
            PermissionItem(
                id = "OVERLAY",
                title = "Floating Assistant Overlay",
                description = "Required to display the floating holographic assistant bubble over other apps",
                isGranted = Settings.canDrawOverlays(context),
                isSystemSettings = true
            )
        )

        // Accessibility Service
        list.add(
            PermissionItem(
                id = "ACCESSIBILITY",
                title = "Accessibility Automation",
                description = "Required for reading on-screen elements and auto-navigating WhatsApp / Instagram",
                isGranted = JarvisAccessibilityService.isServiceRunning,
                isSystemSettings = true
            )
        )

        return list
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
