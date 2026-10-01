package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.database.dao.ActionLogDao
import com.example.database.dao.ConversationDao
import com.example.database.dao.MemoryDao
import com.example.database.entities.ActionLog
import com.example.database.entities.ConversationMessage
import com.example.database.entities.MemoryItem

@Database(
    entities = [MemoryItem::class, ConversationMessage::class, ActionLog::class],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationDao(): ConversationDao
    abstract fun actionLogDao(): ActionLogDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_memory.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
