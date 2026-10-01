package com.example.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String, // PREFERENCE, FACT, ROUTINE, CONTACT, CUSTOM_COMMAND
    val timestamp: Long = System.currentTimeMillis()
)
