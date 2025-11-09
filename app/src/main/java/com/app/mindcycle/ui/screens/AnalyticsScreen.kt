package com.app.mindcycle.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CyclePhase
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.ui.viewmodel.MainUiState
import org.threeten.bp.temporal.ChronoUnit

@Composable
fun AnalyticsScreen(
    uiState: MainUiState,
    onNavigateToEntries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = uiState.entries
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onNavigateToEntries) {
            Text(text = stringResource(R.string.view_entries))
        }

        if (entries.size < 3) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    text = stringResource(R.string.analytics_empty),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            CycleLengthCard(entries = entries)
            Spacer(modifier = Modifier.height(4.dp))
            MoodByPhaseCard(entries = entries)
            Spacer(modifier = Modifier.height(4.dp))
            SymptomFrequencyCard(entries = entries)
        }
    }
}

@Composable
private fun CycleLengthCard(entries: List<MoodEntry>) {
    val starts = entries.filter { it.isPeriodStart }.sortedBy { it.date }
    val lengths = starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a.date, b.date).toFloat() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.analytics_cycle_length_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (lengths.isEmpty()) {
                Text(text = stringResource(R.string.analytics_empty))
            } else {
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)) {
                    val max = lengths.maxOrNull() ?: 0f
                    val min = lengths.minOrNull() ?: 0f
                    val range = (max - min).coerceAtLeast(1f)
                    val stepX = size.width / (lengths.size - 1).coerceAtLeast(1)
                    val path = Path()
                    lengths.forEachIndexed { index, value ->
                        val x = index * stepX
                        val normalized = (value - min) / range
                        val y = size.height - normalized * size.height
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
                }
            }
        }
    }
}

@Composable
private fun MoodByPhaseCard(entries: List<MoodEntry>) {
    val phases = CyclePhase.values().associateWith { phase ->
        val phaseEntries = entries.filter { it.cyclePhase == phase }
        if (phaseEntries.isEmpty()) 0f else phaseEntries.map { it.moodLevel.ordinal }.average().toFloat()
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.analytics_mood_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            for ((phase, value) in phases) {
                if (value > 0f) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(text = stringResource(id = when (phase) {
                            CyclePhase.MENSTRUATION -> R.string.phase_menstruation
                            CyclePhase.FOLLICULAR -> R.string.phase_follicular
                            CyclePhase.OVULATION -> R.string.phase_ovulation
                            CyclePhase.LUTEAL -> R.string.phase_luteal
                            CyclePhase.PMS -> R.string.phase_pms
                            CyclePhase.NONE -> R.string.phase_none
                        }), modifier = Modifier.weight(1f))
                        val barWidth = (value / 5f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .fillMaxWidth(barWidth)
                                .padding(horizontal = 8.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomFrequencyCard(entries: List<MoodEntry>) {
    val counts = entries.flatMap { it.symptoms }.groupingBy { it }.eachCount()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.analytics_symptom_frequency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (counts.isEmpty()) {
                Text(text = stringResource(R.string.analytics_empty))
            } else {
                val topSymptoms = counts.entries.sortedByDescending { it.value }.take(5)
                for ((label, value) in topSymptoms) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = label, modifier = Modifier.weight(1f))
                        Text(text = value.toString())
                    }
                }
            }
        }
    }
}
