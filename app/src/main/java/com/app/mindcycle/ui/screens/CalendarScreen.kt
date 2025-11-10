package com.app.mindcycle.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.CyclePhase
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.MoodLevel
import com.app.mindcycle.ui.components.AdBanner
import com.app.mindcycle.ui.theme.ForecastPink
import com.app.mindcycle.ui.theme.MenstruationPink
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    entries: List<MoodEntry>,
    cycleForecast: CycleForecast?,
    onNavigateToAddEntry: (String, Boolean, Boolean) -> Unit,
    onNavigateToEditEntry: (Long) -> Unit,
    onNavigateToEntriesList: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val startMonth = remember { YearMonth.now().minusMonths(12) }
    val months = remember {
        (0..24).map { offset -> startMonth.plusMonths(offset.toLong()) }
    }
    val currentMonthIndex = remember { months.indexOf(YearMonth.now()).takeIf { it >= 0 } ?: months.lastIndex }
    val pagerState = rememberPagerState(initialPage = currentMonthIndex, pageCount = { months.size })

    val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    val entriesByDate = remember(entries) {
        entries.groupBy { it.date.toLocalDate().toJavaLocalDate() }.mapValues { it.value.last() }
    }
    val menstruationLabels = remember(entries) { buildMenstruationLabels(entries) }
    val forecastRange = remember(cycleForecast) {
        val start = cycleForecast?.windowStart?.toJavaLocalDate()
        val end = cycleForecast?.windowEnd?.toJavaLocalDate()
        if (start != null && end != null) start..end else null
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ForecastSummaryCard(cycleForecast = cycleForecast)

            AnimatedContent(
                targetState = months[pagerState.currentPage],
                label = "month_header",
                transitionSpec = {
                    (fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)) with
                        fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)))
                }
            ) { month ->
                MonthHeader(
                    yearMonth = month,
                    onPrevious = {
                        if (pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage - 1,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                        }
                    },
                    onNext = {
                        if (pagerState.currentPage < months.lastIndex) {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                        }
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) { page ->
                val month = months[page]
                val days = remember(month) { buildMonthDays(month, firstDayOfWeek) }
                MonthGrid(
                    days = days,
                    entriesByDate = entriesByDate,
                    menstruationLabels = menstruationLabels,
                    forecastRange = forecastRange,
                    selectedDate = selectedDate,
                    onDayClick = { date ->
                        selectedDate = date
                        isSheetVisible = true
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )
            }

            ActionRow(onNavigateToEntriesList = onNavigateToEntriesList)
            AdBanner(modifier = Modifier.fillMaxWidth())
        }
    }

    val activeDate = selectedDate
    val activeEntry = activeDate?.let { entriesByDate[it] }
    if (isSheetVisible && activeDate != null) {
        ModalBottomSheet(
            onDismissRequest = { isSheetVisible = false },
            sheetState = sheetState
        ) {
            DayDetailsSheet(
                date = activeDate,
                entry = activeEntry,
                onNavigateToEditEntry = {
                    activeEntry?.let { onNavigateToEditEntry(it.id) }
                    isSheetVisible = false
                },
                onAddPeriodStart = {
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.entry_saved)) }
                    onNavigateToAddEntry(activeDate.toString(), true, true)
                    isSheetVisible = false
                },
                onAddPeriodEnd = {
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.entry_saved)) }
                    onNavigateToAddEntry(activeDate.toString(), false, true)
                    isSheetVisible = false
                },
                onClose = { isSheetVisible = false }
            )
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = stringResource(id = R.string.back))
        }
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = stringResource(id = R.string.next_month))
        }
    }
}

@Composable
private fun MonthGrid(
    days: List<DayCell>,
    entriesByDate: Map<LocalDate, MoodEntry>,
    menstruationLabels: Map<LocalDate, Int>,
    forecastRange: ClosedRange<LocalDate>?,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        items(days, key = { it.date }) { day ->
            val entry = entriesByDate[day.date]
            val menstruationLabel = menstruationLabels[day.date]
            val isForecast = forecastRange?.let { day.date in it } ?: false
            CalendarDayCell(
                day = day,
                entry = entry,
                menstruationLabel = menstruationLabel,
                isForecast = isForecast,
                isSelected = selectedDate == day.date,
                onDayClick = { onDayClick(day.date) }
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: DayCell,
    entry: MoodEntry?,
    menstruationLabel: Int?,
    isForecast: Boolean,
    isSelected: Boolean,
    onDayClick: () -> Unit
) {
    val indicatorColor = entry?.let { colorForMood(it.moodLevel) } ?: MaterialTheme.colorScheme.secondary
    val isToday = day.date == LocalDate.now()
    val backgroundColor by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "selection_alpha"
    )
    val containerColor = Color.White.copy(alpha = backgroundColor * 0.8f)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.97f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "selection_scale"
    )
    val alpha by animateFloatAsState(targetValue = if (day.isCurrentMonth) 1f else 0.4f, label = "month_alpha")
    val semanticsDescription = buildString {
        append(day.date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())))
        if (entry != null) {
            append(". ")
            append(localizedMoodLabel(entry.moodLevel))
            if (entry.symptoms.isNotEmpty()) {
                append(". ")
                append(entry.symptoms.take(3).joinToString())
            }
        }
        if (menstruationLabel != null) {
            append(". ")
            append("Day $menstruationLabel")
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .background(color = containerColor.copy(alpha = 0.2f))
            .padding(6.dp)
            .semantics { contentDescription = semanticsDescription }
            .clickable(
                onClick = onDayClick,
                enabled = day.isCurrentMonth
            )
    ) {
        if (isForecast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(ForecastPink.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isToday) 2.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                )
            }

            if (menstruationLabel != null) {
                Text(
                    text = "D$menstruationLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MenstruationPink.copy(alpha = 0.45f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (entry?.symptoms?.isNotEmpty() == true) {
                SymptomDots(entry.symptoms)
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (entry != null || isToday) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}

@Composable
private fun SymptomDots(symptoms: List<String>) {
    val colors = listOf(
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        symptoms.take(3).forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors[index % colors.size])
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayDetailsSheet(
    date: LocalDate,
    entry: MoodEntry?,
    onNavigateToEditEntry: () -> Unit,
    onAddPeriodStart: () -> Unit,
    onAddPeriodEnd: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        entry?.let {
            Text(text = localizedMoodLabel(it.moodLevel), style = MaterialTheme.typography.bodyLarge)
            Text(text = localizedPhaseLabel(it.cyclePhase), style = MaterialTheme.typography.bodyMedium)
            if (it.symptoms.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    it.symptoms.take(6).forEach { symptom ->
                        AssistChip(
                            onClick = {},
                            label = { Text(symptom) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
            it.note?.takeIf { note -> note.isNotBlank() }?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onNavigateToEditEntry) {
                Text(text = stringResource(id = R.string.edit))
            }
        } ?: Text(
            text = stringResource(id = R.string.no_entries_placeholder),
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onAddPeriodStart,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.cta_period_start))
            }
            Button(
                onClick = onAddPeriodEnd,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.cta_period_end))
            }
        }

        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(text = stringResource(id = R.string.close))
        }
    }
}

@Composable
private fun ForecastSummaryCard(cycleForecast: CycleForecast?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(id = R.string.cycle_prediction),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (cycleForecast == null || cycleForecast.insufficientData) {
                Text(text = stringResource(id = R.string.insufficient_data_hint), style = MaterialTheme.typography.bodyMedium)
            } else {
                val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.next_period), style = MaterialTheme.typography.labelMedium)
                        Text(text = cycleForecast.predictedStartDate?.toJavaLocalDate()?.format(formatter) ?: "—", style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.forecast_confidence_label), style = MaterialTheme.typography.labelMedium)
                        Text(text = stringResource(id = cycleForecast.confidenceLevel.labelRes), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(onNavigateToEntriesList: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.entries_list),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onNavigateToEntriesList) {
                Text(text = stringResource(id = R.string.view_entries))
            }
        }
    }
}

private data class DayCell(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

private fun buildMonthDays(yearMonth: YearMonth, firstDayOfWeek: java.time.DayOfWeek): List<DayCell> {
    val firstOfMonth = yearMonth.atDay(1)
    val lastOfMonth = yearMonth.atEndOfMonth()
    val daysBefore = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7)
    val totalDays = ((daysBefore + lastOfMonth.dayOfMonth).coerceAtLeast(42))
    val firstDisplayDate = firstOfMonth.minusDays(daysBefore.toLong())
    return (0 until 42).map { index ->
        val date = firstDisplayDate.plusDays(index.toLong())
        DayCell(date = date, isCurrentMonth = date.month == yearMonth.month)
    }
}

private fun buildMenstruationLabels(entries: List<MoodEntry>): Map<LocalDate, Int> {
    val periodEntries = entries
        .filter { it.isPeriod }
        .sortedBy { it.date }
    if (periodEntries.isEmpty()) return emptyMap()

    val labels = mutableMapOf<LocalDate, Int>()
    var currentLabel = 0
    var lastLabeledDate: LocalDate? = null

    periodEntries.forEach { entry ->
        val date = entry.date.toLocalDate().toJavaLocalDate()
        val shouldStartNewCycle = entry.isPeriodStart || (lastLabeledDate != null && date.isBefore(lastLabeledDate))
        if (shouldStartNewCycle) {
            currentLabel = 0
            lastLabeledDate = null
        }

        if (lastLabeledDate == null) {
            currentLabel = 1
            labels[date] = currentLabel
            lastLabeledDate = date
            return@forEach
        }

        val daysBetween = ChronoUnit.DAYS.between(lastLabeledDate, date).toInt()
        if (daysBetween <= 0) {
            labels[date] = currentLabel
        } else {
            (1..daysBetween).forEach { offset ->
                val currentDate = lastLabeledDate!!.plusDays(offset.toLong())
                currentLabel += 1
                labels[currentDate] = currentLabel
            }
            lastLabeledDate = date
        }
    }

    return labels
}

private fun org.threeten.bp.LocalDate.toJavaLocalDate(): LocalDate = LocalDate.parse(this.toString())

private fun org.threeten.bp.LocalDateTime.toJavaLocalDate(): LocalDate = LocalDate.parse(this.toLocalDate().toString())

@Composable
private fun localizedMoodLabel(level: MoodLevel): String = when (level) {
    MoodLevel.VERY_BAD -> stringResource(id = R.string.mood_very_bad)
    MoodLevel.BAD -> stringResource(id = R.string.mood_bad)
    MoodLevel.NEUTRAL -> stringResource(id = R.string.mood_neutral)
    MoodLevel.GOOD -> stringResource(id = R.string.mood_good)
    MoodLevel.VERY_GOOD -> stringResource(id = R.string.mood_very_good)
    MoodLevel.EXCELLENT -> stringResource(id = R.string.mood_excellent)
}

@Composable
private fun localizedPhaseLabel(phase: CyclePhase): String = when (phase) {
    CyclePhase.MENSTRUATION -> stringResource(id = R.string.phase_menstruation)
    CyclePhase.FOLLICULAR -> stringResource(id = R.string.phase_follicular)
    CyclePhase.OVULATION -> stringResource(id = R.string.phase_ovulation)
    CyclePhase.LUTEAL -> stringResource(id = R.string.phase_luteal)
    CyclePhase.PMS -> stringResource(id = R.string.phase_pms)
    CyclePhase.NONE -> stringResource(id = R.string.phase_none)
}

private fun colorForMood(level: MoodLevel): Color = when (level) {
    MoodLevel.VERY_BAD -> Color(0xFFD32F2F)
    MoodLevel.BAD -> Color(0xFFF57C00)
    MoodLevel.NEUTRAL -> Color(0xFF757575)
    MoodLevel.GOOD -> Color(0xFF388E3C)
    MoodLevel.VERY_GOOD -> Color(0xFF1976D2)
    MoodLevel.EXCELLENT -> Color(0xFFD81B60)
}
