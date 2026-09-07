package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.HabitDao
import com.factory.innerlogaimoodjournal.data.local.entity.HabitCompletionEntity
import com.factory.innerlogaimoodjournal.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitRepository(private val dao: HabitDao) {
    fun getAllHabits(): Flow<List<HabitEntity>> = dao.getAllHabits()
    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletionEntity>> = dao.getCompletionsForDate(date)
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>> = dao.getCompletionsForHabit(habitId)

    suspend fun addHabit(habit: HabitEntity): Long = dao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)

    suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val existing = dao.getCompletion(habitId, date)
        if (existing != null) {
            dao.deleteCompletion(existing)
        } else {
            dao.insertCompletion(HabitCompletionEntity(habitId = habitId, date = date, completed = true))
        }
    }
}
