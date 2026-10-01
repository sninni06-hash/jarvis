package com.example.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "JARVIS" or "MYRAA"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val intent: String? = null,
    val taskId: String? = null
)
