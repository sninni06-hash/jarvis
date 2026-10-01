package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entities.MemoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE `key` LIKE '%' || :query || '%' OR `value` LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<MemoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(item: MemoryItem): Long

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAll()
}
