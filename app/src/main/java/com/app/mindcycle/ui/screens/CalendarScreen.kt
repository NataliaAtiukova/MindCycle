package com.app.mindcycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CyclePhase
import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.MoodLevel
import com.app.mindcycle.ui.components.AdBanner
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.WeekFields
import java.util.*
import androidx.compose.ui.graphics.vector.ImageVector

// Функция для получения локализованных строк
@Composable
fun getLocalizedMoodLabel(moodLevel: MoodLevel): String {
    return stringResource(moodLabels[moodLevel] ?: R.string.mood_neutral)
}

@Composable
fun getLocalizedPhaseLabel(cyclePhase: CyclePhase): String {
    return stringResource(phaseLabels[cyclePhase] ?: R.string.phase_none)
}

@Composable
fun getSymptomIcon(symptom: String): ImageVector {
    return when (symptom) {
        stringResource(R.string.symptom_fatigue) -> Icons.Filled.BatteryAlert
        stringResource(R.string.symptom_pain) -> Icons.Filled.Healing
        stringResource(R.string.symptom_irritation) -> Icons.Filled.MoodBad
        stringResource(R.string.symptom_anxiety) -> Icons.Filled.SentimentDissatisfied
        stringResource(R.string.symptom_headache) -> Icons.Filled.Psychology
        stringResource(R.string.symptom_cramps) -> Icons.Filled.FlashOn
        stringResource(R.string.symptom_bloating) -> Icons.Filled.Air
        stringResource(R.string.symptom_insomnia) -> Icons.Filled.NightsStay
        else -> Icons.Filled.Psychology
    }
}

// Карты для иконок и русских подписей вынесены на уровень файла
private val moodIcons = mapOf(
    MoodLevel.VERY_BAD to Icons.Filled.MoodBad,
    MoodLevel.BAD to Icons.Filled.SentimentDissatisfied,
    MoodLevel.NEUTRAL to Icons.Filled.Psychology,
    MoodLevel.GOOD to Icons.Filled.SentimentSatisfied,
    MoodLevel.VERY_GOOD to Icons.Filled.SentimentVerySatisfied,
    MoodLevel.EXCELLENT to Icons.Filled.Star
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
    CyclePhase.MENSTRUATION to Icons.Filled.Bloodtype,
    CyclePhase.FOLLICULAR to Icons.Filled.Spa,
    CyclePhase.OVULATION to Icons.Filled.WbSunny,
    CyclePhase.LUTEAL to Icons.Filled.Nightlight,
    CyclePhase.PMS to Icons.Filled.MoodBad,
    CyclePhase.NONE to Icons.Filled.RemoveCircle
)

private val phaseLabels = mapOf(
    CyclePhase.MENSTRUATION to R.string.phase_menstruation,
    CyclePhase.FOLLICULAR to R.string.phase_follicular,
    CyclePhase.OVULATION to R.string.phase_ovulation,
    CyclePhase.LUTEAL to R.string.phase_luteal,
    CyclePhase.PMS to R.string.phase_pms,
    CyclePhase.NONE to R.string.phase_none
)



@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    entries: List<MoodEntry>,
    cycleForecast: CycleForecast?,
    onNavigateToAddEntry: (String) -> Unit,
    onNavigateToEditEntry: (Long) -> Unit,
    onNavigateToEntriesList: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDayDetails by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<MoodEntry?>(null) }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val symptomIcons = mapOf(
        stringResource(R.string.symptom_fatigue) to Icons.Filled.BatteryAlert,
        stringResource(R.string.symptom_pain) to Icons.Filled.Healing,
        stringResource(R.string.symptom_irritation) to Icons.Filled.MoodBad,
        stringResource(R.string.symptom_anxiety) to Icons.Filled.SentimentDissatisfied,
        stringResource(R.string.symptom_headache) to Icons.Filled.Psychology,
        stringResource(R.string.symptom_cramps) to Icons.Filled.FlashOn,
        stringResource(R.string.symptom_bloating) to Icons.Filled.Air,
        stringResource(R.string.symptom_insomnia) to Icons.Filled.NightsStay
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(R.string.calendar),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Предсказание цикла
        cycleForecast?.let { forecast ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cycle_prediction),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    forecast.predictedStartDate?.let { date ->
                        Text("${stringResource(R.string.next_period)}: $date")
                    }
                    val windowStart = forecast.windowStart
                    val windowEnd = forecast.windowEnd
                    if (windowStart != null && windowEnd != null) {
                        Text("${stringResource(R.string.example_period)} ${windowStart} - ${windowEnd}")
                    }
                    Text("${stringResource(R.string.average_cycle_length)}: ${forecast.medianCycleLengthDays.toInt()} ${stringResource(R.string.days)}")
                    Text(text = stringResource(forecast.confidenceLevel.labelRes))
                }
            }
        }

        // Кнопки навигации
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { onNavigateToAddEntry(selectedDate.toString()) }) {
                Text(stringResource(R.string.add_entry))
            }
            Button(onClick = onNavigateToEntriesList) {
                Text(stringResource(R.string.entries_list))
            }
        }

        // Статистика
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.statistics),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.total_entries)}: ${entries.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                val averageMood = entries.map { it.moodLevel.ordinal }.average()
                Text(
                    text = "${stringResource(R.string.average_mood)}: ${String.format("%.1f", averageMood)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Календарь
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Здесь будет реализация календаря
                val firstDayOfMonth = currentMonth.atDay(1)
                val lastDayOfMonth = currentMonth.atEndOfMonth()
                val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                val daysInMonth = lastDayOfMonth.dayOfMonth
                val firstDayOfMonthWeekday = firstDayOfMonth.dayOfWeek.value
                val offsetDays = (firstDayOfMonthWeekday - firstDayOfWeek.value + 7) % 7

                Column {
                    // Заголовок месяца
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentMonth = currentMonth.minusMonths(1)
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = {
                            currentMonth = currentMonth.plusMonths(1)
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }

                    // Дни недели
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val weekDays = listOf(
            stringResource(R.string.mon),
            stringResource(R.string.tue),
            stringResource(R.string.wed),
            stringResource(R.string.thu),
            stringResource(R.string.fri),
            stringResource(R.string.sat),
            stringResource(R.string.sun)
        )
                        weekDays.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Сетка календаря
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Пустые ячейки в начале
                        items(offsetDays) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        }

                        // Дни месяца
                        items(daysInMonth) { day ->
                            val date = currentMonth.atDay(day + 1)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val entry = entries.find { it.date.toLocalDate() == date }
                            val isPredictedPeriod = cycleForecast?.let { forecast ->
                                val start = forecast.windowStart
                                val end = forecast.windowEnd
                                if (start != null && end != null) {
                                    (date.isAfter(start.minusDays(1)) && date.isBefore(end.plusDays(1))) || date == start || date == end
                                } else {
                                    false
                                }
                            } ?: false
                            val isPredictedOvulation = cycleForecast?.predictedStartDate?.let { predicted ->
                                date == predicted.minusDays(14)
                            } ?: false

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            isToday -> MaterialTheme.colorScheme.secondaryContainer
                                            isPredictedPeriod -> Color.Red.copy(alpha = 0.3f)
                                            isPredictedOvulation -> Color.Green.copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        selectedDate = date
                                        if (entry != null) {
                                            selectedEntry = entry
                                            showDayDetails = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = (day + 1).toString(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    entry?.let {
                                        Icon(
                                            imageVector = moodIcons[it.moodLevel] ?: Icons.Default.HelpOutline,
                                            contentDescription = stringResource(R.string.mood),
                                            modifier = Modifier.size(20.dp),
                                            tint = if (it.isPeriod) Color.Red else LocalContentColor.current
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Banner Ad
        Spacer(modifier = Modifier.height(8.dp))
        AdBanner(modifier = Modifier.fillMaxWidth())
    }

    if (showDayDetails && selectedEntry != null) {
        ModalBottomSheet(
            onDismissRequest = { showDayDetails = false },
            sheetState = sheetState
        ) {
            DayDetailsContent(
                entry = selectedEntry!!,
                onEdit = {
                    onNavigateToEditEntry(selectedEntry!!.id)
                    showDayDetails = false
                },
                onClose = { showDayDetails = false }
            )
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
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)) {
        Text(
            text = stringResource(R.string.entry_for_date, entry.date.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = moodIcons[entry.moodLevel] ?: Icons.Filled.Psychology,
                contentDescription = getLocalizedMoodLabel(entry.moodLevel),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = getLocalizedMoodLabel(entry.moodLevel), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = phaseIcons[entry.cyclePhase] ?: Icons.Filled.RemoveCircle,
                contentDescription = getLocalizedPhaseLabel(entry.cyclePhase),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = getLocalizedPhaseLabel(entry.cyclePhase), style = MaterialTheme.typography.bodyMedium)
        }
        if (entry.symptoms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.symptoms.forEach { symptom ->
                    AssistChip(onClick = { }, label = { Text(symptom) })
                }
            }
        }
        if (!entry.note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = entry.note ?: "", style = MaterialTheme.typography.bodyMedium)
        }
        if (entry.isPeriodStart) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.period_start), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.edit))
            }
            OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.close))
            }
        }
    }
}
