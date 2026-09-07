package com.factory.innerlogaimoodjournal.ui.screens.journal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.factory.innerlogaimoodjournal.ui.components.MoodSelector
import com.factory.innerlogaimoodjournal.ui.components.ProBadge
import com.factory.innerlogaimoodjournal.ui.currentApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(entryId: Long?, onDone: () -> Unit, onUpgrade: () -> Unit) {
    val app = currentApp()
    val viewModel: NewEntryViewModel = viewModel(
        key = "new_entry_${entryId ?: -1}",
        factory = viewModelFactory {
            initializer { NewEntryViewModel(app.journalRepository, app.moodRepository, app.premiumManager, entryId) }
        }
    )

    val haptics = LocalHapticFeedback.current
    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(viewModel.isSaved) {
        if (viewModel.isSaved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.analyzeMood()
                            viewModel.save()
                        },
                        enabled = viewModel.content.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { contentFocusRequester.requestFocus() })
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.content,
                onValueChange = {
                    viewModel.content = it
                    viewModel.analyzeMood()
                },
                modifier = Modifier.fillMaxWidth().weight(1f).focusRequester(contentFocusRequester),
                placeholder = { Text("Write what's on your mind...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )
            viewModel.validationError?.let { message ->
                Spacer(Modifier.height(4.dp))
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Text("How are you feeling?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            MoodSelector(selected = viewModel.selectedMood, onSelect = { viewModel.selectedMood = it })

            viewModel.insight?.let { insightText ->
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        insightText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (viewModel.showInsightUpsell) {
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onUpgrade)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Unlock AI-powered mood insights",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        ProBadge()
                    }
                }
            }
        }
    }
}
