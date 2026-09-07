package com.factory.innerlogaimoodjournal.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import com.factory.innerlogaimoodjournal.data.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class GoalsViewModel(private val goalRepository: GoalRepository) : ViewModel() {

    val goals: StateFlow<List<GoalEntity>> = goalRepository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(title: String, description: String, targetValue: Int, unit: String, deadline: LocalDate?) {
        viewModelScope.launch {
            goalRepository.saveGoal(
                GoalEntity(
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    currentValue = 0,
                    unit = unit,
                    deadline = deadline,
                    createdAt = LocalDate.now()
                )
            )
        }
    }

    fun incrementProgress(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.updateProgress(goal, goal.currentValue + 1)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }
}
