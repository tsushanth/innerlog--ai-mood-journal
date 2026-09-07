package com.factory.innerlogaimoodjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isFavorite: Boolean = false
)
