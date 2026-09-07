package com.factory.innerlogaimoodjournal.ui.screens.stats

import app.cash.turbine.test
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import com.factory.innerlogaimoodjournal.data.repository.MoodRepository
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MoodStatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun entry(score: Int, date: LocalDate = LocalDate.now()) = MoodEntryEntity(
        date = date,
        moodScore = score,
        note = "",
        createdAt = LocalDateTime.now()
    )

    @Test
    fun `weeklyEntries and summary start empty with no logged moods`() = runTest {
        val repository: MoodRepository = mockk()
        every { repository.getMoodEntriesSince(any()) } returns MutableStateFlow(emptyList())
        val vm = MoodStatsViewModel(repository)

        vm.weeklyEntries.test { assertEquals(emptyList<MoodEntryEntity>(), awaitItem()) }
        vm.summary.test { assertTrue(awaitItem().contains("Log a few more days")) }
    }

    @Test
    fun `summary reflects weekly entries once loaded`() = runTest {
        val entries = MutableStateFlow(listOf(entry(2), entry(5)))
        val repository: MoodRepository = mockk()
        every { repository.getMoodEntriesSince(any()) } returns entries
        val vm = MoodStatsViewModel(repository)

        vm.weeklyEntries.test { assertEquals(2, awaitItem().size) }
        vm.summary.test {
            val summary = awaitItem()
            assertTrue(summary.isNotBlank())
        }
    }
}
