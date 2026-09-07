package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.GoalDao
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GoalRepositoryTest {

    private lateinit var dao: GoalDao
    private lateinit var repository: GoalRepository

    private fun goal(id: Long = 0L, current: Int = 0, target: Int = 10) = GoalEntity(
        id = id,
        title = "Goal",
        description = "desc",
        targetValue = target,
        currentValue = current,
        unit = "times",
        deadline = null,
        createdAt = LocalDate.now()
    )

    @Before
    fun setUp() {
        dao = mockk()
        repository = GoalRepository(dao)
    }

    @Test
    fun `saveGoal inserts when id is zero`() = runTest {
        coEvery { dao.insert(any()) } returns 7L

        val id = repository.saveGoal(goal(id = 0L))

        assertEquals(7L, id)
        coVerify { dao.insert(any()) }
    }

    @Test
    fun `saveGoal updates when id is non-zero`() = runTest {
        coEvery { dao.update(any()) } returns Unit

        val id = repository.saveGoal(goal(id = 4L))

        assertEquals(4L, id)
        coVerify { dao.update(match { it.id == 4L }) }
    }

    @Test
    fun `updateProgress clamps value to target and marks completed`() = runTest {
        coEvery { dao.update(any()) } returns Unit

        repository.updateProgress(goal(id = 1L, current = 8, target = 10), newValue = 50)

        coVerify { dao.update(match { it.currentValue == 10 && it.isCompleted }) }
    }

    @Test
    fun `updateProgress clamps negative values to zero`() = runTest {
        coEvery { dao.update(any()) } returns Unit

        repository.updateProgress(goal(id = 1L, current = 2, target = 10), newValue = -5)

        coVerify { dao.update(match { it.currentValue == 0 && !it.isCompleted }) }
    }

    @Test
    fun `deleteGoal delegates to dao`() = runTest {
        coEvery { dao.delete(any()) } returns Unit
        val target = goal(id = 2L)

        repository.deleteGoal(target)

        coVerify { dao.delete(target) }
    }
}
