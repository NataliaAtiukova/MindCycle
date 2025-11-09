package com.app.mindcycle.ui.screens

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CyclePhase
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.MoodLevel
import org.threeten.bp.LocalDateTime
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.MoodBad
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.RemoveCircle
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import org.threeten.bp.ZoneId
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEntryScreen(
    entryToEdit: MoodEntry?,
    initialDate: LocalDateTime?,
    defaultIsPeriodStart: Boolean = false,
    quickSymptom: String? = null,
    onNavigateBack: () -> Unit,
    onSaveEntry: (MoodEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf(entryToEdit?.moodLevel) }
    var selectedPhase by remember { mutableStateOf(entryToEdit?.cyclePhase) }
    var note by remember { mutableStateOf(entryToEdit?.note ?: "") }
    var isPeriodStart by remember { mutableStateOf(entryToEdit?.isPeriodStart ?: defaultIsPeriodStart) }
    var selectedSymptoms by remember { mutableStateOf(entryToEdit?.symptoms?.toSet() ?: quickSymptom?.let { setOf(it) } ?: emptySet()) }
    var entryDate by remember { mutableStateOf(entryToEdit?.date ?: initialDate ?: LocalDateTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val symptomsList = listOf(
        stringResource(R.string.symptom_fatigue) to Icons.Rounded.BatteryAlert,
        stringResource(R.string.symptom_pain) to Icons.Rounded.Healing,
        stringResource(R.string.symptom_irritation) to Icons.Rounded.MoodBad,
        stringResource(R.string.symptom_anxiety) to Icons.Rounded.SentimentDissatisfied,
        stringResource(R.string.symptom_headache) to Icons.Rounded.Psychology,
        stringResource(R.string.symptom_cramps) to Icons.Rounded.FlashOn,
        stringResource(R.string.symptom_bloating) to Icons.Rounded.Air,
        stringResource(R.string.symptom_insomnia) to Icons.Rounded.NightsStay
    )

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = entryDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    var tempHour by remember { mutableStateOf(entryDate.hour) }
    var tempMinute by remember { mutableStateOf(entryDate.minute) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (entryToEdit != null) stringResource(R.string.edit) else stringResource(R.string.add_entry),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (entryToEdit != null) {
                        IconButton(
                            onClick = {
                                // TODO: Implement delete functionality
                            }
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Date and Time Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { showDatePicker = true }) {
                            Text(stringResource(R.string.edit))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mood),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val moodOptions = listOf(
                        MoodLevel.VERY_BAD to Icons.Rounded.MoodBad,
                        MoodLevel.BAD to Icons.Rounded.SentimentDissatisfied,
                        MoodLevel.NEUTRAL to Icons.Rounded.Psychology,
                        MoodLevel.GOOD to Icons.Rounded.SentimentSatisfied,
                        MoodLevel.VERY_GOOD to Icons.Rounded.SentimentVerySatisfied,
                        MoodLevel.EXCELLENT to Icons.Rounded.Star
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodOptions.forEach { (mood, icon) ->
                            val isSelected = selectedMood == mood
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { selectedMood = mood }
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(
                                        when (mood) {
                                            MoodLevel.VERY_BAD -> R.string.mood_very_bad
                                            MoodLevel.BAD -> R.string.mood_bad
                                            MoodLevel.NEUTRAL -> R.string.mood_neutral
                                            MoodLevel.GOOD -> R.string.mood_good
                                            MoodLevel.VERY_GOOD -> R.string.mood_very_good
                                            MoodLevel.EXCELLENT -> R.string.mood_excellent
                                        }
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phase Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.phase),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val phaseOptions = listOf(
                        CyclePhase.MENSTRUATION to Icons.Rounded.Bloodtype,
                        CyclePhase.FOLLICULAR to Icons.Rounded.Spa,
                        CyclePhase.OVULATION to Icons.Rounded.WbSunny,
                        CyclePhase.LUTEAL to Icons.Rounded.Nightlight,
                        CyclePhase.PMS to Icons.Rounded.MoodBad,
                        CyclePhase.NONE to Icons.Rounded.RemoveCircle
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        phaseOptions.forEach { (phase, icon) ->
                            val isSelected = selectedPhase == phase
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { selectedPhase = phase }
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(
                                        when (phase) {
                                            CyclePhase.MENSTRUATION -> R.string.phase_menstruation
                                            CyclePhase.FOLLICULAR -> R.string.phase_follicular
                                            CyclePhase.OVULATION -> R.string.phase_ovulation
                                            CyclePhase.LUTEAL -> R.string.phase_luteal
                                            CyclePhase.PMS -> R.string.phase_pms
                                            CyclePhase.NONE -> R.string.phase_none
                                        }
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Symptoms Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.symptoms),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptomsList.forEach { (symptom, icon) ->
                            val isSelected = selectedSymptoms.contains(symptom)
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSymptoms = if (selectedSymptoms.contains(symptom))
                                                selectedSymptoms - symptom else selectedSymptoms + symptom
                                        }
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = symptom,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.notes_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Start Toggle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isPeriodStart) Modifier.background(Color(0xFFFFCDD2), RoundedCornerShape(8.dp)) else Modifier),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.period_start),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isPeriodStart,
                                onClick = { isPeriodStart = !isPeriodStart }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.period_start_label))
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isPeriodStart,
                            onCheckedChange = { isPeriodStart = it }
                        )
                        if (isPeriodStart) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Red)
                            )
                        }
                    }
                }
            }

            // Display selected mood beautifully
            if (selectedMood != null) {
                val moodIcons = mapOf(
                    MoodLevel.VERY_BAD to Icons.Rounded.MoodBad,
                    MoodLevel.BAD to Icons.Rounded.SentimentDissatisfied,
                    MoodLevel.NEUTRAL to Icons.Rounded.Psychology,
                    MoodLevel.GOOD to Icons.Rounded.SentimentSatisfied,
                    MoodLevel.VERY_GOOD to Icons.Rounded.SentimentVerySatisfied,
                    MoodLevel.EXCELLENT to Icons.Rounded.Star
                )
                val moodLabels = mapOf(
                    MoodLevel.VERY_BAD to stringResource(R.string.mood_very_bad),
                    MoodLevel.BAD to stringResource(R.string.mood_bad),
                    MoodLevel.NEUTRAL to stringResource(R.string.mood_neutral),
                    MoodLevel.GOOD to stringResource(R.string.mood_good),
                    MoodLevel.VERY_GOOD to stringResource(R.string.mood_very_good),
                    MoodLevel.EXCELLENT to stringResource(R.string.mood_excellent)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = moodIcons[selectedMood] ?: Icons.Rounded.Psychology,
                        contentDescription = moodLabels[selectedMood] ?: selectedMood.toString(),
                        tint = when (selectedMood) {
                            MoodLevel.VERY_BAD -> Color(0xFFD32F2F)
                            MoodLevel.BAD -> Color(0xFFF57C00)
                            MoodLevel.NEUTRAL -> Color(0xFF757575)
                            MoodLevel.GOOD -> Color(0xFF388E3C)
                            MoodLevel.VERY_GOOD -> Color(0xFF1976D2)
                            MoodLevel.EXCELLENT -> Color(0xFFD81B60)
                            else -> Color.Unspecified
                        },
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = moodLabels[selectedMood] ?: selectedMood.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = when (selectedMood) {
                            MoodLevel.VERY_BAD -> Color(0xFFD32F2F)
                            MoodLevel.BAD -> Color(0xFFF57C00)
                            MoodLevel.NEUTRAL -> Color(0xFF757575)
                            MoodLevel.GOOD -> Color(0xFF388E3C)
                            MoodLevel.VERY_GOOD -> Color(0xFF1976D2)
                            MoodLevel.EXCELLENT -> Color(0xFFD81B60)
                            else -> Color.Unspecified
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isPeriodStart) {
                val periodLength = 5 // days
                val cycleLength = 28 // days
                val periodStart = entryDate.toLocalDate()
                val periodEnd = periodStart.plusDays((periodLength - 1).toLong())
                val nextPeriodStart = periodStart.plusDays(cycleLength.toLong())
                val nextPeriodEnd = nextPeriodStart.plusDays((periodLength - 1).toLong())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFFF8BBD0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(stringResource(R.string.example_period), color = Color(0xFFD81B60))
                    Text(
                        "${periodStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} — ${periodEnd.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                        color = Color(0xFFD81B60),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.next_cycle_expected), color = Color(0xFF7B1FA2))
                    Text(
                        "${nextPeriodStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} — ${nextPeriodEnd.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date and Time Picker Dialogs
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                                val selectedMillis = datePickerState.selectedDateMillis
                                if (selectedMillis != null) {
                                    val selectedInstant = org.threeten.bp.Instant.ofEpochMilli(selectedMillis)
                                    entryDate = LocalDateTime.ofInstant(selectedInstant, ZoneId.systemDefault())
                                        .withHour(entryDate.hour)
                                        .withMinute(entryDate.minute)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            entryDate = entryDate.withHour(tempHour).withMinute(tempMinute)
                            showTimePicker = false
                        }) { Text(stringResource(R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
                    },
                    title = { Text(stringResource(R.string.select_time)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.hours))
                            Slider(
                                value = tempHour.toFloat(),
                                onValueChange = { tempHour = it.toInt() },
                                valueRange = 0f..23f,
                                steps = 23
                            )
                            Text(tempHour.toString().padStart(2, '0'))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.minutes))
                            Slider(
                                value = tempMinute.toFloat(),
                                onValueChange = { tempMinute = it.toInt() },
                                valueRange = 0f..59f,
                                steps = 59
                            )
                            Text(tempMinute.toString().padStart(2, '0'))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedMood != null && selectedPhase != null) {
                        val entry = MoodEntry(
                            id = entryToEdit?.id ?: 0,
                            date = entryDate,
                            moodLevel = selectedMood!!,
                            cyclePhase = selectedPhase!!,
                            note = note,
                            symptoms = selectedSymptoms.toList(),
                            isPeriodStart = isPeriodStart
                        )
                        onSaveEntry(entry)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMood != null && selectedPhase != null
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
} 
