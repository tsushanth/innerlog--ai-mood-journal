package com.factory.innerlogaimoodjournal.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun KeypadButton(label: String, contentDescription: String? = null, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val descriptionModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Surface(
        modifier = Modifier.size(72.dp).clip(CircleShape).then(descriptionModifier),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun KeypadIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Surface(
        modifier = Modifier.size(72.dp).clip(CircleShape),
        color = Color.Transparent,
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(28.dp))
        }
    }
}
