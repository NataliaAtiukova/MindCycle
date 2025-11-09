package com.app.mindcycle.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.ui.viewmodel.MainUiState
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

@Composable
fun AnalyticsScreen(
    uiState: MainUiState,
    onNavigateToEntries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val cycleLengths = remember(uiState.entries) { calculateCycleLengths(uiState.entries) }
    val symptomCounts = remember(uiState.entries) {
        uiState.entries.flatMap { it.symptoms }.groupingBy { it }.eachCount()
    }

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
        CycleTrendCard(cycleLengths)
        SymptomFrequencyCard(symptomCounts)
    }
}

private fun calculateCycleLengths(entries: List<MoodEntry>): List<Float> {
    val starts = entries.filter { it.isPeriodStart }.sortedBy { it.date }
    return starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a.date, b.date).toFloat() }
}

@Composable
private fun CycleTrendCard(lengths: List<Float>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TitleRow(text = stringResource(R.string.analytics_cycle_length_title))
            if (lengths.size < 2) {
                Text(text = stringResource(R.string.analytics_empty))
            } else {
                val minValue = lengths.minOrNull() ?: 0f
                val maxValue = lengths.maxOrNull() ?: 0f
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val stepX = size.width / (lengths.size - 1)
                    val range = max(1f, maxValue - minValue)
                    var previous: Offset? = null
                    lengths.forEachIndexed { index, value ->
                        val normalized = (value - minValue) / range
                        val point = Offset(index * stepX, size.height - normalized * size.height)
                        previous?.let { drawLine(Color.Magenta, it, point, strokeWidth = 6f) }
                        previous = point
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomFrequencyCard(symptomCounts: Map<String, Int>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TitleRow(text = stringResource(R.string.analytics_symptom_frequency))
            if (symptomCounts.isEmpty()) {
                Text(text = stringResource(R.string.analytics_empty))
            } else {
                val topSymptoms = symptomCounts.entries.sortedByDescending { it.value }.take(5)
                val maxValue = max(1, topSymptoms.maxOf { it.value })
                val barColor = MaterialTheme.colorScheme.primary
                topSymptoms.forEach { (label, value) ->
                    Column {
                        Text(text = "$label ($value)", style = MaterialTheme.typography.bodyMedium)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        ) {
                            val barWidth = size.width * (value / maxValue.toFloat())
                            drawRect(
                                color = barColor,
                                topLeft = Offset.Zero,
                                size = Size(barWidth, this.size.height)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.Analytics, contentDescription = null)
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}
