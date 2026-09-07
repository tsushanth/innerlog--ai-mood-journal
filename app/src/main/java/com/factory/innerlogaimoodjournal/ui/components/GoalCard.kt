package com.factory.innerlogaimoodjournal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GoalCard(goal: GoalEntity, onIncrement: () -> Unit, onDelete: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    if (goal.description.isNotBlank()) {
                        Text(goal.description, style = MaterialTheme.typography.bodyLarge)
                    }
                    goal.deadline?.let { deadline ->
                        val isOverdue = !goal.isCompleted && deadline.isBefore(LocalDate.now())
                        Text(
                            "Due ${deadline.format(DateTimeFormatter.ISO_LOCAL_DATE)}${if (isOverdue) " (overdue)" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete goal ${goal.title}")
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (goal.currentValue.toFloat() / goal.targetValue.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}", style = MaterialTheme.typography.labelLarge)
                if (!goal.isCompleted) {
                    FilledTonalButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIncrement()
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Progress")
                    }
                } else {
                    Text("Completed 🎉", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
