package com.example.playstore

import android.content.Context
import android.content.Intent
import android.net.Uri

class PlayStoreManager(private val context: Context) {

    fun searchPlayStore(appName: String): Boolean {
        return try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(appName)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(marketIntent)
            true
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(appName)}&c=apps")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }
}
