package com.example.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri

class CallManager(private val context: Context) {

    fun initiateCall(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: SecurityException) {
            // Fall back to DIAL intent which does not require CALL_PHONE permission
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                true
            } catch (ex: Exception) {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
