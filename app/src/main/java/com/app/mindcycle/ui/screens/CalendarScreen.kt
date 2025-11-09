package com.app.mindcycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.CyclePhase
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.MoodLevel
import com.app.mindcycle.ui.components.AdBanner
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import org.threeten.bp.format.DateTimeFormatter
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    entries: List<MoodEntry>,
    cycleForecast: CycleForecast?,
    onNavigateToAddEntry: (String) -> Unit,
    onNavigateToEditEntry: (Long) -> Unit,
    onNavigateToEntriesList: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedEntry: MutableState<MoodEntry?> = remember { mutableStateOf(null) }
    val showSheet = remember { mutableStateOf(false) }
    val startMonth = YearMonth.now().minusMonths(12)
    val endMonth = YearMonth.now().plusMonths(12)
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.now(),
        firstDayOfWeek = firstDayOfWeekFromLocale()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ForecastSummaryCard(cycleForecast = cycleForecast)
        HorizontalCalendar(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = calendarState,
            dayContent = { day ->
                val entry = entries.firstOrNull { it.date.toLocalDate().toString() == day.date.toString() }
                CalendarDayCell(
                    day = day,
                    entry = entry,
                    isPrediction = cycleForecast?.let {
                        val windowStart = it.windowStart?.toJavaDate()
                        val windowEnd = it.windowEnd?.toJavaDate()
                        if (windowStart != null && windowEnd != null) {
                            (day.date >= windowStart && day.date <= windowEnd)
                        } else false
                    } ?: false,
                    onEntrySelected = {
                        selectedEntry.value = entry
                        showSheet.value = true
                    },
                    onAddEntry = { onNavigateToAddEntry(day.date.toString()) }
                )
            }
        )
        ActionRow(onNavigateToEntriesList = onNavigateToEntriesList)
        Spacer(modifier = Modifier.height(8.dp))
        AdBanner(modifier = Modifier.fillMaxWidth())
    }

    if (showSheet.value && selectedEntry.value != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState
        ) {
            selectedEntry.value?.let { entry ->
                DayDetailsContent(
                    entry = entry,
                    onEdit = {
                        onNavigateToEditEntry(entry.id)
                        showSheet.value = false
                    },
                    onClose = { showSheet.value = false }
                )
            }
        }
    }
}

@Composable
private fun ForecastSummaryCard(cycleForecast: CycleForecast?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(id = R.string.cycle_prediction), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (cycleForecast == null || cycleForecast.insufficientData) {
                Text(text = stringResource(id = R.string.insufficient_data_hint), style = MaterialTheme.typography.bodyMedium)
            } else {
                val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.next_period), style = MaterialTheme.typography.labelMedium)
                        Text(text = cycleForecast.predictedStartDate?.format(formatter) ?: "—", style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.forecast_confidence_label), style = MaterialTheme.typography.labelMedium)
                        Text(text = stringResource(id = cycleForecast.confidenceLevel.labelRes), style = MaterialTheme.typography.headlineSmall)
                    }
                }
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    progress = cycleForecast.confidenceScore.toFloat().coerceIn(0f, 1f)
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    entry: MoodEntry?,
    isPrediction: Boolean,
    onEntrySelected: () -> Unit,
    onAddEntry: () -> Unit
) {
    val background = when {
        day.position != DayPosition.MonthDate -> MaterialTheme.colorScheme.surfaceVariant
        isPrediction -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        day.date == LocalDate.now() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    Column(
        modifier = Modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .clickable(enabled = day.position == DayPosition.MonthDate) {
                if (entry != null) onEntrySelected() else onAddEntry()
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = day.date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium)
        entry?.let {
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(24.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(colorForMood(it.moodLevel))
            )
        }
    }
}

@Composable
private fun ActionRow(onNavigateToEntriesList: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.entries_list), color = MaterialTheme.colorScheme.onPrimaryContainer)
            TextButton(onClick = onNavigateToEntriesList) {
                Text(text = stringResource(id = R.string.view_entries))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayDetailsContent(
    entry: MoodEntry,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = entry.date.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = localizedMoodLabel(entry.moodLevel), style = MaterialTheme.typography.bodyLarge)
        Text(text = localizedPhaseLabel(entry.cyclePhase), style = MaterialTheme.typography.bodyMedium)
        if (entry.symptoms.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.symptoms.forEach { symptom ->
                    AssistChip(onClick = {}, label = { Text(symptom) })
                }
            }
        }
        entry.note?.takeIf { it.isNotBlank() }?.let { note ->
            Text(text = note, style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.close))
            }
            TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.edit))
            }
        }
    }
}

private fun colorForMood(level: MoodLevel): Color = when (level) {
    MoodLevel.VERY_BAD -> Color(0xFFD32F2F)
    MoodLevel.BAD -> Color(0xFFF57C00)
    MoodLevel.NEUTRAL -> Color(0xFF757575)
    MoodLevel.GOOD -> Color(0xFF388E3C)
    MoodLevel.VERY_GOOD -> Color(0xFF1976D2)
    MoodLevel.EXCELLENT -> Color(0xFFD81B60)
}

private fun org.threeten.bp.LocalDate.toJavaDate(): LocalDate = LocalDate.parse(this.toString())

@Composable
private fun localizedMoodLabel(level: MoodLevel): String = stringResource(
    when (level) {
        MoodLevel.VERY_BAD -> R.string.mood_very_bad
        MoodLevel.BAD -> R.string.mood_bad
        MoodLevel.NEUTRAL -> R.string.mood_neutral
        MoodLevel.GOOD -> R.string.mood_good
        MoodLevel.VERY_GOOD -> R.string.mood_very_good
        MoodLevel.EXCELLENT -> R.string.mood_excellent
    }
)

@Composable
private fun localizedPhaseLabel(phase: CyclePhase): String = stringResource(
    when (phase) {
        CyclePhase.MENSTRUATION -> R.string.phase_menstruation
        CyclePhase.FOLLICULAR -> R.string.phase_follicular
        CyclePhase.OVULATION -> R.string.phase_ovulation
        CyclePhase.LUTEAL -> R.string.phase_luteal
        CyclePhase.PMS -> R.string.phase_pms
        CyclePhase.NONE -> R.string.phase_none
    }
)
