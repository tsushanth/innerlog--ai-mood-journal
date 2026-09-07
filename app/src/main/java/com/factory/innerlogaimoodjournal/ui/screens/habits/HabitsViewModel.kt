package com.factory.innerlogaimoodjournal.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factory.innerlogaimoodjournal.data.local.entity.HabitEntity
import com.factory.innerlogaimoodjournal.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUiState(val habit: HabitEntity, val completedToday: Boolean)

class HabitsViewModel(private val habitRepository: HabitRepository) : ViewModel() {

    private val today = LocalDate.now()

    val habitStates: StateFlow<List<HabitUiState>> = combine(
        habitRepository.getAllHabits(),
        habitRepository.getCompletionsForDate(today)
    ) { habits, todaysCompletions ->
        habits.map { habit ->
            HabitUiState(
                habit = habit,
                completedToday = todaysCompletions.any { it.habitId == habit.id }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String, icon: String, targetDaysPerWeek: Int) {
        viewModelScope.launch {
            habitRepository.addHabit(
                HabitEntity(name = name, icon = icon, targetDaysPerWeek = targetDaysPerWeek, createdAt = today)
            )
        }
    }

    fun toggleToday(habitId: Long) {
        viewModelScope.launch {
            habitRepository.toggleCompletion(habitId, today)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
}
