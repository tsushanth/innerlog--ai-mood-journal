package com.factory.innerlogaimoodjournal.ui.screens.home

import app.cash.turbine.test
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import com.factory.innerlogaimoodjournal.data.repository.JournalRepository
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var journalRepository: JournalRepository
    private val allEntries = MutableStateFlow(listOf(entry(1L, "First")))

    private fun entry(id: Long, title: String, favorite: Boolean = false) = JournalEntryEntity(
        id = id,
        title = title,
        content = "content",
        mood = "OKAY",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        isFavorite = favorite
    )

    @Before
    fun setUp() {
        journalRepository = mockk()
        every { journalRepository.getAllEntries() } returns allEntries
        coEvery { journalRepository.saveEntry(any()) } returns 1L
        coEvery { journalRepository.deleteEntry(any()) } returns Unit
    }

    private fun viewModel() = HomeViewModel(journalRepository)

    @Test
    fun `initial entries come from getAllEntries when query is blank`() = runTest {
        val vm = viewModel()

        vm.entries.test {
            assertEquals(listOf(entry(1L, "First")), awaitItem())
        }
    }

    @Test
    fun `changing search query switches to search results`() = runTest {
        every { journalRepository.searchEntries("mood") } returns flowOf(listOf(entry(2L, "Mood swing")))
        val vm = viewModel()

        vm.entries.test {
            awaitItem()
            vm.onSearchQueryChanged("mood")
            assertEquals(listOf(entry(2L, "Mood swing")), awaitItem())
        }
    }

    @Test
    fun `clearing search query reverts to all entries`() = runTest {
        every { journalRepository.searchEntries("mood") } returns flowOf(listOf(entry(2L, "Mood swing")))
        val vm = viewModel()

        vm.entries.test {
            awaitItem()
            vm.onSearchQueryChanged("mood")
            awaitItem()
            vm.onSearchQueryChanged("")
            assertEquals(listOf(entry(1L, "First")), awaitItem())
        }
    }

    @Test
    fun `toggleFavorite saves entry with flipped favorite flag`() = runTest {
        coVerify(exactly = 0) { journalRepository.saveEntry(any()) }
        val vm = viewModel()

        vm.toggleFavorite(entry(1L, "First", favorite = false))

        coVerify { journalRepository.saveEntry(match { it.id == 1L && it.isFavorite }) }
    }

    @Test
    fun `deleteEntry delegates to repository`() = runTest {
        val vm = viewModel()
        val target = entry(1L, "First")

        vm.deleteEntry(target)

        coVerify { journalRepository.deleteEntry(target) }
    }
}
