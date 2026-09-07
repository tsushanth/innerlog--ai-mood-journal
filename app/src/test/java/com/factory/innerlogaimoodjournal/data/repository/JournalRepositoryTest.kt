package com.factory.innerlogaimoodjournal.data.repository

import com.factory.innerlogaimoodjournal.data.local.dao.JournalDao
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class JournalRepositoryTest {

    private lateinit var dao: JournalDao
    private lateinit var repository: JournalRepository

    private fun entry(id: Long = 0L) = JournalEntryEntity(
        id = id,
        title = "Title",
        content = "Content",
        mood = "OKAY",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Before
    fun setUp() {
        dao = mockk()
        repository = JournalRepository(dao)
    }

    @Test
    fun `saveEntry inserts a new entry when id is zero`() = runTest {
        coEvery { dao.insert(any()) } returns 10L

        val id = repository.saveEntry(entry(id = 0L))

        assertEquals(10L, id)
        coVerify { dao.insert(any()) }
        coVerify(exactly = 0) { dao.update(any()) }
    }

    @Test
    fun `saveEntry updates an existing entry and returns its id`() = runTest {
        coEvery { dao.update(any()) } returns Unit

        val id = repository.saveEntry(entry(id = 5L))

        assertEquals(5L, id)
        coVerify { dao.update(match { it.id == 5L }) }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `deleteEntry delegates to dao`() = runTest {
        coEvery { dao.delete(any()) } returns Unit
        val target = entry(id = 3L)

        repository.deleteEntry(target)

        coVerify { dao.delete(target) }
    }

    @Test
    fun `getEntryById returns null when dao has no match`() = runTest {
        coEvery { dao.getEntryById(404L) } returns null

        assertEquals(null, repository.getEntryById(404L))
    }
}
