package com.factory.innerlogaimoodjournal.ui.screens.habits

import app.cash.turbine.test
import com.factory.innerlogaimoodjournal.data.local.entity.HabitCompletionEntity
import com.factory.innerlogaimoodjournal.data.local.entity.HabitEntity
import com.factory.innerlogaimoodjournal.data.repository.HabitRepository
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class HabitsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var habitRepository: HabitRepository
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())
    private val completionsFlow = MutableStateFlow<List<HabitCompletionEntity>>(emptyList())

    private fun habit(id: Long = 1L) = HabitEntity(
        id = id,
        name = "Meditate",
        icon = "icon",
        targetDaysPerWeek = 5,
        createdAt = LocalDate.now()
    )

    @Before
    fun setUp() {
        habitRepository = mockk()
        every { habitRepository.getAllHabits() } returns habitsFlow
        every { habitRepository.getCompletionsForDate(any()) } returns completionsFlow
        coEvery { habitRepository.addHabit(any()) } returns 1L
        coEvery { habitRepository.toggleCompletion(any(), any()) } returns Unit
        coEvery { habitRepository.deleteHabit(any()) } returns Unit
    }

    private fun viewModel() = HabitsViewModel(habitRepository)

    @Test
    fun `habitStates combines habits with today's completions`() = runTest {
        habitsFlow.value = listOf(habit(1L), habit(2L))
        completionsFlow.value = listOf(HabitCompletionEntity(habitId = 1L, date = LocalDate.now()))
        val vm = viewModel()

        vm.habitStates.test {
            val states = awaitItem()
            assertEquals(2, states.size)
            assertTrue(states.first { it.habit.id == 1L }.completedToday)
            assertFalse(states.first { it.habit.id == 2L }.completedToday)
        }
    }

    @Test
    fun `addHabit persists a new habit`() = runTest {
        val vm = viewModel()

        vm.addHabit("Read", "book", 3)

        coVerify {
            habitRepository.addHabit(match { it.name == "Read" && it.targetDaysPerWeek == 3 })
        }
    }

    @Test
    fun `toggleToday toggles completion for today`() = runTest {
        val vm = viewModel()

        vm.toggleToday(5L)

        coVerify { habitRepository.toggleCompletion(5L, LocalDate.now()) }
    }

    @Test
    fun `deleteHabit delegates to repository`() = runTest {
        val vm = viewModel()
        val target = habit()

        vm.deleteHabit(target)

        coVerify { habitRepository.deleteHabit(target) }
    }
}
