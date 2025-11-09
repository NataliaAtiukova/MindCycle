package com.app.mindcycle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.ui.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSelectMode: (CycleMode) -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToDataPrivacy: () -> Unit,
    onNavigateToSymptomJournal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
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
            ModePickerCard(selectedMode = uiState.mode, onSelectMode = onSelectMode)
        }
        item {
            SettingsActionCard(
                title = stringResource(R.string.reminders_preview_title),
                description = stringResource(R.string.reminders_screen_subtitle),
                onClick = onNavigateToReminders
            )
        }
        item {
            SettingsActionCard(
                title = stringResource(R.string.symptom_journal_title),
                description = stringResource(R.string.symptom_journal_subtitle),
                onClick = onNavigateToSymptomJournal
            )
        }
        item {
            SettingsActionCard(
                title = stringResource(R.string.settings_export_header),
                description = stringResource(R.string.data_privacy_preview),
                onClick = onNavigateToDataPrivacy
            )
        }
    }
}

@Composable
private fun ModePickerCard(selectedMode: CycleMode, onSelectMode: (CycleMode) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_modes_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CycleMode.values().forEach { mode ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = selectedMode == mode, onClick = { onSelectMode(mode) })
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = stringResource(id = mode.titleRes), fontWeight = if (mode == selectedMode) FontWeight.SemiBold else FontWeight.Normal)
                        Text(text = stringResource(id = mode.descriptionRes), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
