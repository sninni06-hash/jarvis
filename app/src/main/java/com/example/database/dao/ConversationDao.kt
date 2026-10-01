package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entities.ConversationMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ConversationMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessage): Long

    @Query("DELETE FROM conversations")
    suspend fun clearHistory()
}
