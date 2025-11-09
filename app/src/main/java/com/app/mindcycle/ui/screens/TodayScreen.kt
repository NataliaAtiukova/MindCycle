package com.app.mindcycle.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import com.app.mindcycle.R
import com.app.mindcycle.data.model.ContraceptionLogEntry
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.ui.viewmodel.MainUiState
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    uiState: MainUiState,
    onNavigateToAddEntry: (String, Boolean, String?) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToEntries: () -> Unit,
    onRecordContraception: (ContraceptionMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())
    val header = stringResource(id = R.string.today_title)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = header,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = today.format(formatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            ForecastCard(
                uiState = uiState,
                onOpenCalendar = onNavigateToCalendar,
                onOpenEntries = onNavigateToEntries
            )
        }

        item {
            QuickActionsCard(
                onStart = {
                    onNavigateToAddEntry(today.toString(), true, null)
                },
                onEnd = {
                    onNavigateToAddEntry(today.toString(), false, null)
                }
            )
        }

        item {
            SymptomChipsSection(
                onNavigateToAddEntry = { symptom ->
                    onNavigateToAddEntry(today.toString(), false, symptom)
                }
            )
        }

        if (uiState.mode.enableContraceptionJournal) {
            item {
                ContraceptionJournalCard(
                    logs = uiState.contraceptionLogs,
                    onRecord = onRecordContraception
                )
            }
        }

        item {
            RemindersPreview(reminders = uiState.reminderConfigs)
        }
    }
}

@Composable
private fun ForecastCard(
    uiState: MainUiState,
    onOpenCalendar: () -> Unit,
    onOpenEntries: () -> Unit
) {
    val forecast = uiState.forecast
    val mode = uiState.mode
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.cycle_prediction),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(onClick = {}, label = {
                    Text(text = stringResource(id = mode.titleRes))
                })
            }
            Text(
                text = stringResource(id = mode.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                mode.hideForecastOnToday -> {
                    Text(
                        text = stringResource(R.string.forecast_hidden_perimenopause),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                forecast == null -> {
                    Text(
                        text = stringResource(R.string.insufficient_data_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                forecast.insufficientData -> {
                    Text(
                        text = stringResource(R.string.insufficient_data_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                    forecast.predictedStartDate?.let { date ->
                        Text(
                            text = "${stringResource(R.string.next_period)}: ${date.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    val windowStart = forecast.windowStart?.format(dateFormatter)
                    val windowEnd = forecast.windowEnd?.format(dateFormatter)
                    if (windowStart != null && windowEnd != null) {
                        Text(
                            text = "${stringResource(R.string.forecast_window_label)}: ${stringResource(R.string.forecast_window_range, windowStart, windowEnd)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    SuggestionChip(onClick = {}, label = {
                        Text(stringResource(id = forecast.confidenceLevel.labelRes))
                    })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenCalendar, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.open_calendar))
                }
                OutlinedButton(onClick = onOpenEntries, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.ListAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.view_entries))
                }
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onStart: () -> Unit,
    onEnd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.log_entry),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onStart, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cta_period_start))
                }
                Button(onClick = onEnd, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text(stringResource(R.string.cta_period_end))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomChipsSection(
    onNavigateToAddEntry: (String) -> Unit
) {
    val chips = listOf(
        R.string.symptom_fatigue,
        R.string.symptom_pain,
        R.string.symptom_irritation,
        R.string.symptom_anxiety,
        R.string.symptom_headache,
        R.string.symptom_cramps,
        R.string.symptom_bloating,
        R.string.symptom_insomnia
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.quick_symptoms_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach { res ->
                    val label = stringResource(id = res)
                    AssistChip(onClick = { onNavigateToAddEntry(label) }, label = { Text(label) })
                }
            }
        }
    }
}

@Composable
private fun ContraceptionJournalCard(
    logs: List<ContraceptionLogEntry>,
    onRecord: (ContraceptionMethod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.hc_journal_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ContraceptionMethod.values().forEach { method ->
                    OutlinedButton(onClick = { onRecord(method) }, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = method.labelRes), textAlign = TextAlign.Center)
                    }
                }
            }
            val lastLog = logs.firstOrNull()
            if (lastLog != null) {
                val formatter = DateTimeFormatter.ofPattern("d MMM • HH:mm", Locale.getDefault())
                val lastText = lastLog.takenAt?.format(formatter) ?: lastLog.scheduledDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                Text(text = stringResource(R.string.hc_last_taken, lastText))
            } else {
                Text(text = stringResource(R.string.hc_no_log))
            }
        }
    }
}

@Composable
private fun RemindersPreview(reminders: Map<ReminderType, com.app.mindcycle.data.model.ReminderConfig>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.reminders_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ReminderType.values().forEach { type ->
                val config = reminders[type]
                ReminderRow(type = type, config = config)
                Divider()
            }
        }
    }
}

@Composable
private fun ReminderRow(type: ReminderType, config: com.app.mindcycle.data.model.ReminderConfig?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(id = type.titleRes), style = MaterialTheme.typography.bodyLarge)
        val label = when {
            config == null || !config.enabled -> stringResource(R.string.reminder_disabled)
            config.mutedUntilEpochDay == LocalDate.now().toEpochDay() -> stringResource(R.string.reminder_muted_today)
            else -> stringResource(R.string.reminder_time_label, config.hour, config.minute)
        }
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
