package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.MoodDao
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MoodRepositoryTest {

    private lateinit var dao: MoodDao
    private lateinit var repository: MoodRepository

    private fun moodEntry(id: Long = 0L, score: Int = 3) = MoodEntryEntity(
        id = id,
        date = LocalDate.now(),
        moodScore = score,
        note = "",
        createdAt = LocalDateTime.now()
    )

    @Before
    fun setUp() {
        dao = mockk()
        repository = MoodRepository(dao)
    }

    @Test
    fun `saveMoodEntry delegates to dao insert and returns the id`() = runTest {
        coEvery { dao.insert(any()) } returns 11L

        val id = repository.saveMoodEntry(moodEntry())

        assertEquals(11L, id)
    }

    @Test
    fun `getMoodEntryForDate returns null when nothing found`() = runTest {
        coEvery { dao.getMoodEntryForDate(any()) } returns null

        assertNull(repository.getMoodEntryForDate(LocalDate.now()))
    }

    @Test
    fun `deleteMoodEntry delegates to dao`() = runTest {
        coEvery { dao.delete(any()) } returns Unit
        val target = moodEntry(id = 3L)

        repository.deleteMoodEntry(target)

        coVerify { dao.delete(target) }
    }
}
