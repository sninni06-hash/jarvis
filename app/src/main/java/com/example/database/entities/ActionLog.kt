package com.example.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_logs")
data class ActionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskName: String,
    val actionType: String,
    val details: String,
    val status: String, // SUCCESS, FAILED, PARTIAL, REQUIRES_USER
    val timestamp: Long = System.currentTimeMillis()
)
