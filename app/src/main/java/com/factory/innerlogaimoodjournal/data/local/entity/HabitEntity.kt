package com.factory.innerlogaimoodjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val targetDaysPerWeek: Int,
    val createdAt: LocalDate,
    val isArchived: Boolean = false
)
