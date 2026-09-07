package com.factory.innerlogaimoodjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val unit: String,
    val deadline: LocalDate?,
    val isCompleted: Boolean = false,
    val createdAt: LocalDate
)
