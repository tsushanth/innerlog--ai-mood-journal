package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.HabitDao
import com.factory.innerlogaimoodjournal.data.local.entity.HabitCompletionEntity
import com.factory.innerlogaimoodjournal.data.local.entity.HabitEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HabitRepositoryTest {

    private lateinit var dao: HabitDao
    private lateinit var repository: HabitRepository

    @Before
    fun setUp() {
        dao = mockk()
        repository = HabitRepository(dao)
    }

    @Test
    fun `toggleCompletion inserts a completion when none exists for the day`() = runTest {
        coEvery { dao.getCompletion(1L, any()) } returns null
        coEvery { dao.insertCompletion(any()) } returns Unit

        repository.toggleCompletion(1L, LocalDate.now())

        coVerify { dao.insertCompletion(match { it.habitId == 1L && it.completed }) }
        coVerify(exactly = 0) { dao.deleteCompletion(any()) }
    }

    @Test
    fun `toggleCompletion removes the completion when one already exists`() = runTest {
        val today = LocalDate.now()
        val existing = HabitCompletionEntity(id = 9L, habitId = 1L, date = today)
        coEvery { dao.getCompletion(1L, today) } returns existing
        coEvery { dao.deleteCompletion(any()) } returns Unit

        repository.toggleCompletion(1L, today)

        coVerify { dao.deleteCompletion(existing) }
        coVerify(exactly = 0) { dao.insertCompletion(any()) }
    }

    @Test
    fun `addHabit delegates to dao insert`() = runTest {
        coEvery { dao.insertHabit(any()) } returns 42L

        val id = repository.addHabit(
            HabitEntity(
                name = "Read",
                icon = "icon",
                targetDaysPerWeek = 3,
                createdAt = LocalDate.now()
            )
        )

        assertEquals(42L, id)
    }
}
