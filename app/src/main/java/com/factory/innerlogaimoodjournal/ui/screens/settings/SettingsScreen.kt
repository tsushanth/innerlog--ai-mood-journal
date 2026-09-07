package com.factory.innerlogaimoodjournal.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.factory.innerlogaimoodjournal.ui.currentApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onSetupPin: () -> Unit, onUpgrade: () -> Unit) {
    val app = currentApp()
    val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory {
        initializer { SettingsViewModel(app.lockManager, app.billingManager, app.premiumManager) }
    })
    val isPremium by viewModel.isPremium.collectAsState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Membership", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isPremium) "InnerLog Premium" else "Free plan",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isPremium) "You have full access to habits, goals, insights, and AI mood analysis."
                        else "Upgrade to unlock habits, goals, insights, and AI mood analysis.",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    if (isPremium) {
                        TextButton(onClick = { openManageSubscriptions(context) }) {
                            Text("Manage subscription")
                        }
                    } else {
                        Button(onClick = onUpgrade) {
                            Text("Upgrade to Premium")
                        }
                    }
                    TextButton(onClick = { viewModel.restorePurchases() }) {
                        Text("Restore purchases")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text("Privacy & Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("App lock", style = MaterialTheme.typography.bodyLarge)
                    Text("Require a PIN to open InnerLog", style = MaterialTheme.typography.labelLarge)
                }
                Switch(
                    checked = viewModel.lockEnabled,
                    onCheckedChange = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (enabled) onSetupPin() else viewModel.disableLock()
                    },
                    modifier = Modifier.semantics { contentDescription = "App lock" }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric unlock", style = MaterialTheme.typography.bodyLarge)
                    Text("Use fingerprint or face unlock instead of PIN", style = MaterialTheme.typography.labelLarge)
                }
                Switch(
                    checked = viewModel.biometricEnabled,
                    enabled = viewModel.lockEnabled,
                    onCheckedChange = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setBiometricEnabled(it)
                    },
                    modifier = Modifier.semantics { contentDescription = "Biometric unlock" }
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "InnerLog helps you track your mood, reflect through journaling, and build healthy habits — all stored privately on your device.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun openManageSubscriptions(context: android.content.Context) {
    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}
