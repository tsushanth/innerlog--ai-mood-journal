package com.factory.innerlogaimoodjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val moodScore: Int,
    val note: String,
    val journalEntryId: Long? = null,
    val createdAt: LocalDateTime
)
