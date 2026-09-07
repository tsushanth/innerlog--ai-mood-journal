package com.factory.innerlogaimoodjournal.ui.screens.goals

import app.cash.turbine.test
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import com.factory.innerlogaimoodjournal.data.repository.GoalRepository
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var goalRepository: GoalRepository
    private val goalsFlow = MutableStateFlow<List<GoalEntity>>(emptyList())

    private fun goal(id: Long = 1L, current: Int = 0, target: Int = 5) = GoalEntity(
        id = id,
        title = "Read more",
        description = "desc",
        targetValue = target,
        currentValue = current,
        unit = "books",
        deadline = null,
        createdAt = LocalDate.now()
    )

    @Before
    fun setUp() {
        goalRepository = mockk()
        every { goalRepository.getAllGoals() } returns goalsFlow
        coEvery { goalRepository.saveGoal(any()) } returns 1L
        coEvery { goalRepository.deleteGoal(any()) } returns Unit
        coEvery { goalRepository.updateProgress(any(), any()) } returns Unit
    }

    private fun viewModel() = GoalsViewModel(goalRepository)

    @Test
    fun `initial goals reflect repository state`() = runTest {
        goalsFlow.value = listOf(goal())
        val vm = viewModel()

        vm.goals.test {
            assertEquals(listOf(goal()), awaitItem())
        }
    }

    @Test
    fun `addGoal saves a new goal with zero progress`() = runTest {
        val vm = viewModel()

        vm.addGoal("Read more", "desc", 5, "books", null)

        coVerify {
            goalRepository.saveGoal(
                match { it.title == "Read more" && it.currentValue == 0 && it.targetValue == 5 }
            )
        }
    }

    @Test
    fun `incrementProgress bumps current value by one`() = runTest {
        val vm = viewModel()
        val existing = goal(current = 2)

        vm.incrementProgress(existing)

        coVerify { goalRepository.updateProgress(existing, 3) }
    }

    @Test
    fun `deleteGoal delegates to repository`() = runTest {
        val vm = viewModel()
        val target = goal()

        vm.deleteGoal(target)

        coVerify { goalRepository.deleteGoal(target) }
    }
}
