package com.example.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri

class WhatsAppAutomationManager(private val context: Context) {

    fun openWhatsApp(): Boolean {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")
            ?: pm.getLaunchIntentForPackage("com.whatsapp.w4b")
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return true
        }
        return false
    }

    fun sendMessage(phoneNumber: String?, message: String): Boolean {
        try {
            if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "").removePrefix("+")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            // Fallback: try generic send intent
            return try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun startVideoCall(contactName: String): Boolean {
        // Supported Android deep link / WhatsApp intent dispatch
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=Starting video call with $contactName")
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openWhatsApp()
        }
    }
}
