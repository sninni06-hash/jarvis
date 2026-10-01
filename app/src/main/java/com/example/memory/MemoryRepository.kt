package com.example.memory

import com.example.database.JarvisDatabase
import com.example.database.entities.ActionLog
import com.example.database.entities.ConversationMessage
import com.example.database.entities.MemoryItem
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val database: JarvisDatabase) {
    private val memoryDao = database.memoryDao()
    private val conversationDao = database.conversationDao()
    private val actionLogDao = database.actionLogDao()

    val allMemories: Flow<List<MemoryItem>> = memoryDao.getAllMemories()
    val allMessages: Flow<List<ConversationMessage>> = conversationDao.getAllMessages()
    val allLogs: Flow<List<ActionLog>> = actionLogDao.getAllLogs()

    suspend fun saveMemory(key: String, value: String, category: String = "FACT") {
        memoryDao.insertMemory(MemoryItem(key = key, value = value, category = category))
    }

    suspend fun searchMemories(query: String): List<MemoryItem> {
        return memoryDao.searchMemories(query)
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }

    suspend fun clearMemories() {
        memoryDao.clearAll()
    }

    suspend fun recordMessage(sender: String, text: String, intent: String? = null, taskId: String? = null) {
        conversationDao.insertMessage(ConversationMessage(sender = sender, text = text, intent = intent, taskId = taskId))
    }

    suspend fun getRecentMessages(limit: Int = 10): List<ConversationMessage> {
        return conversationDao.getRecentMessages(limit)
    }

    suspend fun clearConversation() {
        conversationDao.clearHistory()
    }

    suspend fun logAction(taskName: String, actionType: String, details: String, status: String) {
        actionLogDao.insertLog(ActionLog(taskName = taskName, actionType = actionType, details = details, status = status))
    }

    suspend fun clearLogs() {
        actionLogDao.clearLogs()
    }
}
