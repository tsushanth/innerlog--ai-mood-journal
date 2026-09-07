package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.GoalDao
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val dao: GoalDao) {
    fun getAllGoals(): Flow<List<GoalEntity>> = dao.getAllGoals()

    suspend fun saveGoal(goal: GoalEntity): Long =
        if (goal.id == 0L) dao.insert(goal) else {
            dao.update(goal)
            goal.id
        }

    suspend fun deleteGoal(goal: GoalEntity) = dao.delete(goal)

    suspend fun updateProgress(goal: GoalEntity, newValue: Int) {
        val clamped = newValue.coerceIn(0, goal.targetValue)
        dao.update(goal.copy(currentValue = clamped, isCompleted = clamped >= goal.targetValue))
    }
}
