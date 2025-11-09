package com.app.mindcycle.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.app.mindcycle.R
import com.app.mindcycle.data.model.ContraceptionLogEntry
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.ui.viewmodel.MainUiState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    uiState: MainUiState,
    onNavigateToAddEntry: (String, Boolean, String?) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToEntries: () -> Unit,
    onRecordContraception: (ContraceptionMethod) -> Unit,
    onOpenSymptomJournal: () -> Unit,
    onOpenReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val headerDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))
    }
    val haptics = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = lazyListState,
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.today_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = headerDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CycleOverviewCard(
                uiState = uiState,
                modifier = Modifier.fillMaxWidth(),
                onOpenCalendar = onNavigateToCalendar,
                onOpenEntries = onNavigateToEntries
            )
        }

        item {
            QuickActionsRow(
                isLoading = uiState.isLoading,
                onStart = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAddEntry(today.toString(), true, null)
                },
                onEnd = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAddEntry(today.toString(), false, null)
                },
                onOpenSymptomJournal = onOpenSymptomJournal
            )
        }

        item {
            SymptomChipsSection(
                isLoading = uiState.isLoading,
                onChipSelected = { symptom ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigateToAddEntry(today.toString(), false, symptom)
                }
            )
        }

        if (uiState.mode.enableContraceptionJournal) {
            item {
                ContraceptionJournalCard(
                    logs = uiState.contraceptionLogs,
                    onRecord = onRecordContraception,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            ReminderOverviewCard(
                reminders = uiState.reminderConfigs,
                onOpenReminders = onOpenReminders
            )
        }
    }
}

@Composable
private fun CycleOverviewCard(
    uiState: MainUiState,
    modifier: Modifier = Modifier,
    onOpenCalendar: () -> Unit,
    onOpenEntries: () -> Unit
) {
    val placeholderModifier = Modifier.placeholder(
        visible = uiState.isLoading,
        highlight = PlaceholderHighlight.shimmer()
    )
    val forecast = uiState.forecast
    val lastStart = remember(uiState.entries) {
        uiState.entries.filter { it.isPeriodStart }.maxByOrNull { it.date }
    }
    val medianLength = forecast?.medianCycleLengthDays ?: 28.0
    val progress = remember(lastStart, medianLength) {
        if (lastStart == null || medianLength == 0.0) 0f
        else (ChronoUnit.DAYS.between(lastStart.date, LocalDateTime.now()) / medianLength)
            .toFloat().coerceIn(0f, 1f)
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "cycle_progress")

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.today_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = placeholderModifier.weight(1f)
                )
                SuggestionChip(onClick = {}, label = {
                    Text(text = stringResource(id = uiState.mode.titleRes))
                })
            }

            val progressPercent = (animatedProgress * 100).toInt()
            val progressDescription = stringResource(R.string.cycle_progress_announce, progressPercent)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .placeholder(
                            visible = uiState.isLoading,
                            highlight = PlaceholderHighlight.shimmer()
                        )
                        .semantics {
                            contentDescription = progressDescription
                        }
                ) {
                    CycleProgressArc(progress = animatedProgress, modifier = Modifier.fillMaxSize())
                    Text(
                        text = "$progressPercent%",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next_period),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = forecast?.predictedStartDate?.format(DateTimeFormatter.ofPattern("d MMM"))
                            ?: stringResource(R.string.insufficient_data),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    ProbabilityBar(forecast = forecast)
                }
            }

            AnimatedVisibility(visible = forecast == null || forecast.insufficientData) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.Url("https://assets3.lottiefiles.com/packages/lf20_V9t630.json")
                )
                val progressLottie by animateLottieCompositionAsState(composition)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progressLottie },
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.insufficient_data_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun CycleProgressArc(progress: Float, modifier: Modifier = Modifier) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val strokeWidth = 18.dp.toPx()
        val startAngle = 135f
        val sweepAngle = 270f
        val arcSize = Size(size.minDimension, size.minDimension)
        drawArc(
            color = outlineColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = primaryColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ProbabilityBar(forecast: CycleForecast?) {
    if (forecast == null || forecast.windowStart == null || forecast.windowEnd == null) return
    val formatter = DateTimeFormatter.ofPattern("d MMM")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.forecast_window_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = 1f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = forecast.windowStart.format(formatter), style = MaterialTheme.typography.labelSmall)
            Text(text = forecast.windowEnd.format(formatter), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuickActionsRow(
    isLoading: Boolean,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onOpenSymptomJournal: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.log_entry),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .placeholder(isLoading, highlight = PlaceholderHighlight.shimmer())
                ) {
                    Text(stringResource(R.string.cta_period_start))
                }
                Button(
                    onClick = onEnd,
                    modifier = Modifier
                        .weight(1f)
                        .placeholder(isLoading, highlight = PlaceholderHighlight.shimmer()),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.cta_period_end))
                }
            }
            TextButton(onClick = onOpenSymptomJournal) {
                Text(stringResource(R.string.symptoms_label))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomChipsSection(
    isLoading: Boolean,
    onChipSelected: (String) -> Unit
) {
    val chipLabels = listOf(
        R.string.symptom_fatigue,
        R.string.symptom_pain,
        R.string.symptom_irritation,
        R.string.symptom_anxiety,
        R.string.symptom_headache,
        R.string.symptom_cramps,
        R.string.symptom_bloating,
        R.string.symptom_insomnia
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.quick_symptoms_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chipLabels.forEach { labelRes ->
                    val labelText = stringResource(id = labelRes)
                    AssistChip(
                        onClick = { onChipSelected(labelText) },
                        label = { Text(labelText) },
                        modifier = Modifier.placeholder(isLoading, highlight = PlaceholderHighlight.shimmer())
                    )
                }
            }
        }
    }
}

@Composable
private fun ContraceptionJournalCard(
    logs: List<ContraceptionLogEntry>,
    onRecord: (ContraceptionMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM • HH:mm", Locale.getDefault()) }
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.hc_journal_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ContraceptionMethod.values().forEach { method ->
                    OutlinedButton(onClick = { onRecord(method) }, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = method.labelRes), textAlign = TextAlign.Center)
                    }
                }
            }
            val lastEntry = logs.firstOrNull()
            Text(
                text = lastEntry?.takenAt?.format(formatter)
                    ?.let { stringResource(R.string.hc_last_taken, it) }
                    ?: stringResource(R.string.hc_no_log),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ReminderOverviewCard(
    reminders: Map<ReminderType, ReminderConfig>,
    onOpenReminders: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.reminders_preview_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenReminders) {
                    Text(text = stringResource(R.string.reminder_edit_time))
                }
            }
            reminders.entries.forEach { (type, config) ->
                ReminderRow(type = type, config = config)
                Divider()
            }
        }
    }
}

@Composable
private fun ReminderRow(type: ReminderType, config: ReminderConfig?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = type.titleRes), style = MaterialTheme.typography.bodyLarge)
            val label = when {
                config == null || !config.enabled -> stringResource(R.string.reminder_disabled)
                config.mutedUntilEpochDay == LocalDate.now().toEpochDay() -> stringResource(R.string.reminder_muted_today)
                else -> stringResource(R.string.reminder_time_label, config.hour, config.minute)
            }
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = {}) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.ic_launcher_foreground)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(32.dp)
                    .width(32.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        }
    }
}
