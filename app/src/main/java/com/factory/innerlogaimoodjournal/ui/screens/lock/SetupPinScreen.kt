package com.factory.innerlogaimoodjournal.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.factory.innerlogaimoodjournal.security.LockManager
import com.factory.innerlogaimoodjournal.ui.components.KeypadButton

@Composable
fun SetupPinScreen(lockManager: LockManager, onDone: () -> Unit) {
    var stage by remember { mutableStateOf(0) }
    var firstPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (stage == 0) "Create a PIN" else "Confirm your PIN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                val filled = index < currentInput.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(error ?: " ", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(24.dp))

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Spacer(Modifier.size(72.dp))
                        key == "⌫" -> KeypadButton(key, contentDescription = "Delete") {
                            if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1)
                        }
                        else -> KeypadButton(key) {
                            if (currentInput.length < 4) {
                                currentInput += key
                                if (currentInput.length == 4) {
                                    if (stage == 0) {
                                        firstPin = currentInput
                                        currentInput = ""
                                        stage = 1
                                    } else {
                                        if (currentInput == firstPin) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            lockManager.setPin(firstPin)
                                            onDone()
                                        } else {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            error = "PINs don't match, try again"
                                            currentInput = ""
                                            firstPin = ""
                                            stage = 0
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
