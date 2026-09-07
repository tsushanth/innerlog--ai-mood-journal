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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.factory.innerlogaimoodjournal.security.BiometricAuthHelper
import com.factory.innerlogaimoodjournal.security.LockManager
import com.factory.innerlogaimoodjournal.ui.components.KeypadButton
import com.factory.innerlogaimoodjournal.ui.components.KeypadIconButton

@Composable
fun LockScreen(
    activity: FragmentActivity,
    lockManager: LockManager,
    onUnlocked: () -> Unit
) {
    val viewModel: LockViewModel = viewModel(factory = viewModelFactory {
        initializer { LockViewModel(lockManager) }
    })
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        if (viewModel.isBiometricEnabled() && BiometricAuthHelper.canAuthenticate(activity)) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                onSuccess = onUnlocked,
                onError = { message -> viewModel.onBiometricError(message) }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                val filled = index < viewModel.pinInput.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(viewModel.errorMessage ?: " ", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(24.dp))

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("bio", "0", "back")
        )

        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    when (key) {
                        "bio" -> {
                            if (viewModel.isBiometricEnabled()) {
                                KeypadIconButton(
                                    icon = Icons.Filled.Fingerprint,
                                    contentDescription = "Unlock with fingerprint"
                                ) {
                                    BiometricAuthHelper.authenticate(
                                        activity,
                                        onSuccess = onUnlocked,
                                        onError = { message -> viewModel.onBiometricError(message) }
                                    )
                                }
                            } else {
                                Spacer(Modifier.size(72.dp))
                            }
                        }
                        "back" -> {
                            KeypadButton("⌫", contentDescription = "Delete") { viewModel.onBackspace() }
                        }
                        else -> {
                            KeypadButton(key) {
                                viewModel.onDigitEntered(key)
                                if (viewModel.pinInput.length == 4) {
                                    viewModel.verify {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onUnlocked()
                                    }
                                    if (viewModel.errorMessage != null) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
