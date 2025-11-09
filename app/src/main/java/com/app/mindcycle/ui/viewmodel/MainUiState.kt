package com.app.mindcycle.ui.viewmodel

import com.app.mindcycle.data.model.ContraceptionLogEntry
import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType

data class MainUiState(
    val entries: List<MoodEntry> = emptyList(),
    val forecast: CycleForecast? = null,
    val mode: CycleMode = CycleMode.STANDARD,
    val reminderConfigs: Map<ReminderType, ReminderConfig> = emptyMap(),
    val contraceptionLogs: List<ContraceptionLogEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
