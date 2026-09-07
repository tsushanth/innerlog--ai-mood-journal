package com.factory.innerlogaimoodjournal.ui.screens.habits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("✅") }
    var target by remember { mutableStateOf("7") }
    val isValid = name.isNotBlank()

    val haptics = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val iconFocusRequester = remember { FocusRequester() }
    val targetFocusRequester = remember { FocusRequester() }

    fun confirmIfValid() {
        if (isValid) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onConfirm(name.trim(), icon.ifBlank { "✅" }, target.toIntOrNull()?.coerceIn(1, 7) ?: 7)
        } else {
            focusManager.clearFocus()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name") },
                    isError = name.isBlank(),
                    supportingText = { if (name.isBlank()) Text("Name is required") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { iconFocusRequester.requestFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Emoji") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { targetFocusRequester.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(iconFocusRequester)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { c -> c.isDigit() } },
                    label = { Text("Target days / week") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirmIfValid() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(targetFocusRequester)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { confirmIfValid() }, enabled = isValid) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
