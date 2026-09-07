package com.factory.innerlogaimoodjournal.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import com.factory.innerlogaimoodjournal.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val journalRepository: JournalRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<JournalEntryEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) journalRepository.getAllEntries() else journalRepository.searchEntries(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun toggleFavorite(entry: JournalEntryEntity) {
        viewModelScope.launch {
            journalRepository.saveEntry(entry.copy(isFavorite = !entry.isFavorite))
        }
    }

    fun deleteEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            journalRepository.deleteEntry(entry)
        }
    }
}
