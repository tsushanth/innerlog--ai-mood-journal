package com.factory.innerlogaimoodjournal.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import com.factory.innerlogaimoodjournal.data.mood.MoodAnalyzer
import com.factory.innerlogaimoodjournal.data.repository.MoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class MoodStatsViewModel(moodRepository: MoodRepository) : ViewModel() {

    val weeklyEntries: StateFlow<List<MoodEntryEntity>> =
        moodRepository.getMoodEntriesSince(LocalDate.now().minusDays(6))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<String> = weeklyEntries
        .map { entries -> MoodAnalyzer.weeklySummary(entries.map { it.moodScore }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Log a few more days to see your weekly mood trend.")
}
