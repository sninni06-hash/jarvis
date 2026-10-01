package com.example.phone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings

class PhoneControlManager(private val context: Context) {

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            cameraManager?.setTorchMode(cameraId, enabled)
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun openSettings(target: String): Boolean {
        val action = when (target.uppercase()) {
            "WIFI" -> Settings.ACTION_WIFI_SETTINGS
            "BLUETOOTH" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "AIRPLANE_MODE" -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            "ACCESSIBILITY" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "APPLICATION_DETAILS" -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun adjustVolume(increase: Boolean): Boolean {
        return try {
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchAppByName(appName: String): Boolean {
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val targetLower = appName.lowercase()

        val matchingApp = packages.firstOrNull {
            val label = pm.getApplicationLabel(it).toString().lowercase()
            label.contains(targetLower) || it.packageName.lowercase().contains(targetLower)
        }

        if (matchingApp != null) {
            val launchIntent = pm.getLaunchIntentForPackage(matchingApp.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }
        return false
    }
}
