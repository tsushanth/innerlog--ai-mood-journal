package com.factory.innerlogaimoodjournal.ui.screens.journal

import com.factory.innerlogaimoodjournal.billing.PremiumManager
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import com.factory.innerlogaimoodjournal.data.model.MoodLevel
import com.factory.innerlogaimoodjournal.data.repository.JournalRepository
import com.factory.innerlogaimoodjournal.data.repository.MoodRepository
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class NewEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var journalRepository: JournalRepository
    private lateinit var moodRepository: MoodRepository
    private lateinit var premiumManager: PremiumManager
    private val isPremiumFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        journalRepository = mockk()
        moodRepository = mockk()
        premiumManager = mockk()
        every { premiumManager.isPremium } returns isPremiumFlow
        coEvery { journalRepository.saveEntry(any()) } returns 42L
        coEvery { moodRepository.getMoodEntryForDate(any()) } returns null
        coEvery { moodRepository.saveMoodEntry(any()) } returns 1L
    }

    private fun viewModel(entryId: Long? = null) =
        NewEntryViewModel(journalRepository, moodRepository, premiumManager, entryId)

    @Test
    fun `initial state is empty when creating a new entry`() {
        val vm = viewModel(entryId = null)

        assertEquals("", vm.title)
        assertEquals("", vm.content)
        assertNull(vm.selectedMood)
        assertFalse(vm.isSaved)
        assertNull(vm.validationError)
    }

    @Test
    fun `existing entry is loaded into state when editing`() = runTest {
        val entry = JournalEntryEntity(
            id = 7L,
            title = "Reflections",
            content = "Today was good",
            mood = MoodLevel.GOOD.name,
            createdAt = LocalDateTime.of(2026, 1, 1, 8, 0),
            updatedAt = LocalDateTime.of(2026, 1, 1, 8, 0)
        )
        coEvery { journalRepository.getEntryById(7L) } returns entry

        val vm = viewModel(entryId = 7L)

        assertEquals("Reflections", vm.title)
        assertEquals("Today was good", vm.content)
        assertEquals(MoodLevel.GOOD, vm.selectedMood)
    }

    @Test
    fun `analyzeMood does nothing for blank content`() {
        val vm = viewModel()
        vm.content = "   "

        vm.analyzeMood()

        assertNull(vm.insight)
        assertFalse(vm.showInsightUpsell)
        assertNull(vm.selectedMood)
    }

    @Test
    fun `analyzeMood shows upsell instead of insight for non-premium users`() {
        isPremiumFlow.value = false
        val vm = viewModel()
        vm.content = "I feel happy and grateful today"

        vm.analyzeMood()

        assertEquals(MoodLevel.GREAT, vm.selectedMood)
        assertNull(vm.insight)
        assertTrue(vm.showInsightUpsell)
    }

    @Test
    fun `analyzeMood generates insight for premium users`() {
        isPremiumFlow.value = true
        val vm = viewModel()
        vm.content = "I feel happy and grateful today"

        vm.analyzeMood()

        assertNotNull(vm.insight)
        assertFalse(vm.showInsightUpsell)
    }

    @Test
    fun `analyzeMood keeps a manually selected mood`() {
        val vm = viewModel()
        vm.content = "I feel terrible and awful"
        vm.selectedMood = MoodLevel.GREAT

        vm.analyzeMood()

        assertEquals(MoodLevel.GREAT, vm.selectedMood)
    }

    @Test
    fun `save with blank content sets validation error and does not persist`() = runTest {
        val vm = viewModel()
        vm.content = ""

        vm.save()

        assertEquals("Write something before saving", vm.validationError)
        assertFalse(vm.isSaved)
        coVerify(exactly = 0) { journalRepository.saveEntry(any()) }
    }

    @Test
    fun `save persists journal entry and mood entry then marks saved`() = runTest {
        val vm = viewModel()
        vm.title = "My day"
        vm.content = "Feeling okay"
        vm.selectedMood = MoodLevel.OKAY

        vm.save()

        assertTrue(vm.isSaved)
        assertNull(vm.validationError)
        coVerify { journalRepository.saveEntry(match { it.title == "My day" && it.mood == MoodLevel.OKAY.name }) }
        coVerify { moodRepository.saveMoodEntry(match { it.moodScore == MoodLevel.OKAY.score && it.journalEntryId == 42L }) }
    }

    @Test
    fun `save reuses today's existing mood entry id instead of creating a duplicate`() = runTest {
        val existingMoodEntry = MoodEntryEntity(
            id = 99L,
            date = LocalDate.now(),
            moodScore = MoodLevel.OKAY.score,
            note = "earlier",
            createdAt = LocalDateTime.now()
        )
        coEvery { moodRepository.getMoodEntryForDate(any()) } returns existingMoodEntry
        val capturedMoodEntry = slot<MoodEntryEntity>()
        coEvery { moodRepository.saveMoodEntry(capture(capturedMoodEntry)) } returns 99L

        val vm = viewModel()
        vm.content = "Another entry today"
        vm.selectedMood = MoodLevel.GOOD

        vm.save()

        assertEquals(99L, capturedMoodEntry.captured.id)
    }
}
