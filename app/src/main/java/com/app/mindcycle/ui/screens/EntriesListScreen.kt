package com.app.mindcycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CyclePhase
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.MoodLevel
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.tooling.preview.Preview
import org.threeten.bp.LocalDateTime

private val moodIcons = mapOf(
    MoodLevel.VERY_BAD to Icons.Rounded.MoodBad,
    MoodLevel.BAD to Icons.Rounded.SentimentDissatisfied,
    MoodLevel.NEUTRAL to Icons.Rounded.Psychology,
    MoodLevel.GOOD to Icons.Rounded.SentimentSatisfied,
    MoodLevel.VERY_GOOD to Icons.Rounded.SentimentVerySatisfied,
    MoodLevel.EXCELLENT to Icons.Rounded.Star
)

private val moodLabels = mapOf(
    MoodLevel.VERY_BAD to R.string.mood_very_bad,
    MoodLevel.BAD to R.string.mood_bad,
    MoodLevel.NEUTRAL to R.string.mood_neutral,
    MoodLevel.GOOD to R.string.mood_good,
    MoodLevel.VERY_GOOD to R.string.mood_very_good,
    MoodLevel.EXCELLENT to R.string.mood_excellent
)

private val phaseIcons = mapOf(
    CyclePhase.MENSTRUATION to Icons.Rounded.Bloodtype,
    CyclePhase.FOLLICULAR to Icons.Rounded.Spa,
    CyclePhase.OVULATION to Icons.Rounded.WbSunny,
    CyclePhase.LUTEAL to Icons.Rounded.Nightlight,
    CyclePhase.PMS to Icons.Rounded.MoodBad,
    CyclePhase.NONE to Icons.Rounded.RemoveCircle
)

private val phaseLabels = mapOf(
    CyclePhase.MENSTRUATION to R.string.phase_menstruation,
    CyclePhase.FOLLICULAR to R.string.phase_follicular,
    CyclePhase.OVULATION to R.string.phase_ovulation,
    CyclePhase.LUTEAL to R.string.phase_luteal,
    CyclePhase.PMS to R.string.phase_pms,
    CyclePhase.NONE to R.string.phase_none
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriesListScreen(
    entries: List<MoodEntry>,
    onDeleteEntry: (MoodEntry) -> Unit,
    onNavigateToEditEntry: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(R.string.entries_list),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Список записей
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries.sortedByDescending { it.date }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = entry.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Mood
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(moodIcons[entry.moodLevel] ?: Icons.Rounded.HelpOutline, contentDescription = stringResource(R.string.mood), modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(moodLabels[entry.moodLevel] ?: R.string.mood_unknown))
                        }
                        
                        // Cycle Phase
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(phaseIcons[entry.cyclePhase] ?: Icons.Rounded.HelpOutline, contentDescription = stringResource(R.string.phase), modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(phaseLabels[entry.cyclePhase] ?: R.string.phase_unknown))
                        }

                        if (entry.symptoms.isNotEmpty()) {
                            Text(stringResource(R.string.symptoms) + ": ${entry.symptoms.joinToString(", ")}")
                        }
                        if (!entry.note.isNullOrEmpty()) {
                            Text(stringResource(R.string.notes) + ": ${entry.note}")
                        }
                        if (entry.isPeriodStart) {
                            Text(stringResource(R.string.period_start))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onNavigateToEditEntry(entry.id) }
                            ) {
                                Text(stringResource(R.string.edit))
                            }
                            Button(
                                onClick = { onDeleteEntry(entry) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }
        }

        // Кнопка возврата
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun EntryCard(
    entry: MoodEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (entry.moodLevel) {
                            MoodLevel.VERY_BAD -> "😫"
                            MoodLevel.BAD -> "😔"
                            MoodLevel.NEUTRAL -> "😐"
                            MoodLevel.GOOD -> "🙂"
                            MoodLevel.VERY_GOOD -> "😃"
                            MoodLevel.EXCELLENT -> "😊"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            when (entry.cyclePhase) {
                                CyclePhase.MENSTRUATION -> R.string.phase_menstruation
                                CyclePhase.FOLLICULAR -> R.string.phase_follicular
                                CyclePhase.OVULATION -> R.string.phase_ovulation
                                CyclePhase.LUTEAL -> R.string.phase_luteal
                                CyclePhase.PMS -> R.string.phase_pms
                                CyclePhase.NONE -> R.string.phase_none
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.edit),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EntryDetailsDialog(
    entry: MoodEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Настроение
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.mood),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        when (entry.moodLevel) {
                            MoodLevel.VERY_BAD -> "😫"
                            MoodLevel.BAD -> "😔"
                            MoodLevel.NEUTRAL -> "😐"
                            MoodLevel.GOOD -> "🙂"
                            MoodLevel.VERY_GOOD -> "😃"
                            MoodLevel.EXCELLENT -> "😊"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Фаза цикла
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.phase),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(
                            when (entry.cyclePhase) {
                                CyclePhase.MENSTRUATION -> R.string.phase_menstruation
                                CyclePhase.FOLLICULAR -> R.string.phase_follicular
                                CyclePhase.OVULATION -> R.string.phase_ovulation
                                CyclePhase.LUTEAL -> R.string.phase_luteal
                                CyclePhase.PMS -> R.string.phase_pms
                                CyclePhase.NONE -> R.string.phase_none
                            }
                        )
                    )
                }

                if (entry.symptoms.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.symptoms_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                    entry.symptoms.forEach { symptom ->
                        Text(
                            "• $symptom",
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }

                if (!entry.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.notes_label_detail),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(entry.note)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.edit),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 