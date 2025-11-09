package com.app.mindcycle.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.ui.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSelectMode: (CycleMode) -> Unit,
    onToggleReminder: (ReminderType, Boolean) -> Unit,
    onReminderTimeChange: (ReminderType, Int, Int) -> Unit,
    onMuteReminderToday: (ReminderType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            ModeCard(selectedMode = uiState.mode, onSelectMode = onSelectMode)
        }
        item {
            RemindersCard(
                reminders = uiState.reminderConfigs,
                onToggle = onToggleReminder,
                onTimeChange = onReminderTimeChange,
                onMuteToday = onMuteReminderToday
            )
        }
        item {
            ExportCard()
        }
    }
}

@Composable
private fun ModeCard(selectedMode: CycleMode, onSelectMode: (CycleMode) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_modes_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CycleMode.values().forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = mode.titleRes), fontWeight = if (mode == selectedMode) FontWeight.Bold else FontWeight.Normal)
                        Text(text = stringResource(id = mode.descriptionRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = mode == selectedMode, onCheckedChange = { checked ->
                        if (checked) onSelectMode(mode)
                    })
                }
                Divider()
            }
        }
    }
}

@Composable
private fun RemindersCard(
    reminders: Map<ReminderType, ReminderConfig>,
    onToggle: (ReminderType, Boolean) -> Unit,
    onTimeChange: (ReminderType, Int, Int) -> Unit,
    onMuteToday: (ReminderType) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_reminders_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ReminderType.values().forEach { type ->
                val config = reminders[type] ?: ReminderConfig()
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = type.titleRes), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (config.enabled) stringResource(R.string.reminder_time_label, config.hour, config.minute) else stringResource(R.string.reminder_disabled),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = config.enabled, onCheckedChange = { onToggle(type, it) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        val dialog = TimePickerDialog(
                            context,
                            { _, hour, minute -> onTimeChange(type, hour, minute) },
                            config.hour,
                            config.minute,
                            true
                        )
                        dialog.show()
                    }) {
                        Text(text = stringResource(R.string.reminder_edit_time))
                    }
                    TextButton(onClick = { onMuteToday(type) }) {
                        Text(text = stringResource(R.string.reminder_mute_today))
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
private fun ExportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_export_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.settings_export_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
