package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.MoodDao
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MoodRepository(private val dao: MoodDao) {
    fun getAllMoodEntries(): Flow<List<MoodEntryEntity>> = dao.getAllMoodEntries()
    fun getMoodEntriesSince(startDate: LocalDate): Flow<List<MoodEntryEntity>> = dao.getMoodEntriesSince(startDate)
    suspend fun getMoodEntryForDate(date: LocalDate): MoodEntryEntity? = dao.getMoodEntryForDate(date)
    suspend fun saveMoodEntry(entry: MoodEntryEntity): Long = dao.insert(entry)
    suspend fun deleteMoodEntry(entry: MoodEntryEntity) = dao.delete(entry)
}
