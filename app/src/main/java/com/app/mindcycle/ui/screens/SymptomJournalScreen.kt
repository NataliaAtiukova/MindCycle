package com.app.mindcycle.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.MoodEntry
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SymptomJournalScreen(
    entries: List<MoodEntry>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onQuickAdd: (String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault()) }
    val symptomEntries = remember(entries) { entries.filter { it.symptoms.isNotEmpty() } }
    var activeFilter by remember { mutableStateOf<String?>(null) }
    val visibleEntries = remember(activeFilter, symptomEntries) {
        activeFilter?.let { filter ->
            symptomEntries.filter { filter in it.symptoms }
        } ?: symptomEntries
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.symptom_journal_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeFilter = null
                    },
                    label = { Text(stringResource(R.string.symptom_filter_all)) },
                    modifier = Modifier.placeholder(isLoading, highlight = PlaceholderHighlight.shimmer())
                )
                symptomEntries.flatMap { it.symptoms }
                    .distinct()
                    .sorted()
                    .forEach { symptom ->
                        AssistChip(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeFilter = if (activeFilter == symptom) null else symptom
                            },
                            label = { Text(symptom) },
                            modifier = Modifier.placeholder(isLoading, highlight = PlaceholderHighlight.shimmer())
                        )
                    }
            }

            AnimatedVisibility(visible = visibleEntries.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.symptom_journal_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    TextButton(onClick = { onQuickAdd(activeFilter.orEmpty()) }) {
                        Text(text = stringResource(R.string.symptom_journal_add))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleEntries, key = { it.id }) { entry ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = entry.date.format(formatter),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                entry.symptoms.forEach { symptom ->
                                    AssistChip(onClick = { onQuickAdd(symptom) }, label = { Text(symptom) })
                                }
                            }
                            entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                                Text(text = note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
