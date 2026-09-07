package com.factory.innerlogaimoodjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getMoodEntriesSince(startDate: LocalDate): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    suspend fun getMoodEntryForDate(date: LocalDate): MoodEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntryEntity): Long

    @Update
    suspend fun update(entry: MoodEntryEntity)

    @Delete
    suspend fun delete(entry: MoodEntryEntity)
}
