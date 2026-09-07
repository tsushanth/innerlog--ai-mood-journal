package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.JournalDao
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val dao: JournalDao) {
    fun getAllEntries(): Flow<List<JournalEntryEntity>> = dao.getAllEntries()
    fun getFavoriteEntries(): Flow<List<JournalEntryEntity>> = dao.getFavoriteEntries()
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>> = dao.searchEntries(query)
    fun getEntryCount(): Flow<Int> = dao.getEntryCount()
    suspend fun getEntryById(id: Long): JournalEntryEntity? = dao.getEntryById(id)

    suspend fun saveEntry(entry: JournalEntryEntity): Long =
        if (entry.id == 0L) dao.insert(entry) else {
            dao.update(entry)
            entry.id
        }

    suspend fun deleteEntry(entry: JournalEntryEntity) = dao.delete(entry)
}
