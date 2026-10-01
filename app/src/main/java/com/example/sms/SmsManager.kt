package com.example.sms

import android.content.Context
import android.content.Intent
import android.net.Uri

class SmsManager(private val context: Context) {

    fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
