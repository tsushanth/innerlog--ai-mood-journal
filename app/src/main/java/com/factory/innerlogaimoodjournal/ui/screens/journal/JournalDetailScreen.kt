package com.factory.innerlogaimoodjournal.ui.screens.journal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import com.factory.innerlogaimoodjournal.data.model.MoodLevel
import com.factory.innerlogaimoodjournal.ui.currentApp
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(entryId: Long, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val app = currentApp()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var entry by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        entry = app.journalRepository.getEntryById(entryId)
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(entryId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = {
                            entry?.let {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    app.journalRepository.deleteEntry(it)
                                    onBack()
                                }
                            }
                        },
                        enabled = entry != null
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        entry?.let { current ->
            val mood = runCatching { MoodLevel.valueOf(current.mood) }.getOrDefault(MoodLevel.OKAY)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(mood.emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(current.title.ifBlank { "Untitled entry" }, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    current.createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(current.content, style = MaterialTheme.typography.bodyLarge)
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (loaded) {
                Text("Entry not found", style = MaterialTheme.typography.bodyLarge)
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
