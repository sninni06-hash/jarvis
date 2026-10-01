package com.example.music

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

class MusicManager(private val context: Context) {

    fun playMusic(query: String, preferredApp: String = "Default"): Boolean {
        return when (preferredApp.lowercase()) {
            "spotify" -> playOnSpotify(query)
            "youtube" -> playOnYouTube(query)
            else -> {
                // Try Spotify first, then YouTube, then MediaStore
                playOnSpotify(query) || playOnYouTube(query) || playGenericMedia(query)
            }
        }
    }

    private fun playOnSpotify(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("spotify:search:${Uri.encode(query)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    private fun playOnYouTube(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    private fun playGenericMedia(query: String): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
                putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
                putExtra(SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
