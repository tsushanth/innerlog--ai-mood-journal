package com.factory.innerlogaimoodjournal.ui.screens.journal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factory.innerlogaimoodjournal.billing.PremiumManager
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import com.factory.innerlogaimoodjournal.data.mood.MoodAnalyzer
import com.factory.innerlogaimoodjournal.data.model.MoodLevel
import com.factory.innerlogaimoodjournal.data.repository.JournalRepository
import com.factory.innerlogaimoodjournal.data.repository.MoodRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class NewEntryViewModel(
    private val journalRepository: JournalRepository,
    private val moodRepository: MoodRepository,
    private val premiumManager: PremiumManager,
    entryId: Long?
) : ViewModel() {

    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var selectedMood by mutableStateOf<MoodLevel?>(null)
    var insight by mutableStateOf<String?>(null)
    var showInsightUpsell by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set
    var validationError by mutableStateOf<String?>(null)
        private set

    private var createdAt: LocalDateTime = LocalDateTime.now()
    private var existingId: Long = 0L

    init {
        if (entryId != null) {
            viewModelScope.launch {
                journalRepository.getEntryById(entryId)?.let { entry ->
                    existingId = entry.id
                    title = entry.title
                    content = entry.content
                    selectedMood = runCatching { MoodLevel.valueOf(entry.mood) }.getOrNull()
                    createdAt = entry.createdAt
                }
            }
        }
    }

    fun analyzeMood() {
        if (content.isBlank()) return
        val suggested = MoodAnalyzer.suggestMood(content)
        if (selectedMood == null) selectedMood = suggested

        if (premiumManager.isPremium.value) {
            insight = MoodAnalyzer.generateInsight(content, selectedMood ?: suggested)
            showInsightUpsell = false
        } else {
            insight = null
            showInsightUpsell = true
        }
    }

    fun save() {
        if (content.isBlank()) {
            validationError = "Write something before saving"
            return
        }
        validationError = null
        val mood = selectedMood ?: MoodAnalyzer.suggestMood(content)
        viewModelScope.launch {
            val entry = JournalEntryEntity(
                id = existingId,
                title = title,
                content = content,
                mood = mood.name,
                createdAt = createdAt,
                updatedAt = LocalDateTime.now()
            )
            val savedId = journalRepository.saveEntry(entry)
            val today = LocalDate.now()
            val existingMoodEntry = moodRepository.getMoodEntryForDate(today)
            moodRepository.saveMoodEntry(
                MoodEntryEntity(
                    id = existingMoodEntry?.id ?: 0,
                    date = today,
                    moodScore = mood.score,
                    note = title.ifBlank { content.take(40) },
                    journalEntryId = savedId,
                    createdAt = LocalDateTime.now()
                )
            )
            isSaved = true
        }
    }
}
