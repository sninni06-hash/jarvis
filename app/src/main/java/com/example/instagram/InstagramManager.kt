package com.example.instagram

import android.content.Context
import android.content.Intent
import android.net.Uri

class InstagramManager(private val context: Context) {

    fun openInstagram(): Boolean {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage("com.instagram.android")
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return true
        }
        return try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun sendDirectMessage(username: String, message: String): Boolean {
        return try {
            val uri = Uri.parse("https://instagram.com/_u/${Uri.encode(username)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.instagram.android")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openInstagram()
        }
    }

    fun startVideoCall(username: String): Boolean {
        return openInstagram()
    }
}
