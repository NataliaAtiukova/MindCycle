package com.app.mindcycle.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.mindcycle.MindCycleApplication
import com.app.mindcycle.data.db.MoodDatabase
import com.app.mindcycle.data.forecast.CycleForecastCalculator
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.data.repository.CycleRepository
import com.app.mindcycle.reminders.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MoodDatabase.buildDatabase(application)
    private val app = application as MindCycleApplication
    private val repository = CycleRepository(
        dao = database.moodEntryDao(),
        preferences = app.userPreferencesManager,
        calculator = CycleForecastCalculator()
    )
    private val reminderScheduler: ReminderScheduler = app.reminderScheduler

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeMode()
        observeReminders()
        refreshEntries()
        refreshContraceptionLog()
    }

    private fun observeMode() {
        viewModelScope.launch {
            repository.modeFlow.collect { mode ->
                _uiState.update { it.copy(mode = mode) }
                refreshForecast(mode)
            }
        }
    }

    private fun observeReminders() {
        viewModelScope.launch {
            repository.reminderConfigsFlow.collect { configs ->
                _uiState.update { it.copy(reminderConfigs = configs) }
                reminderScheduler.scheduleAll(configs)
            }
        }
    }

    private fun refreshEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val entries = repository.loadEntries()
                _uiState.update { it.copy(entries = entries, isLoading = false) }
                refreshForecast(_uiState.value.mode)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun refreshForecast(mode: CycleMode = _uiState.value.mode) {
        viewModelScope.launch {
            try {
                val forecast = repository.buildForecast(mode)
                _uiState.update { it.copy(forecast = forecast) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun refreshContraceptionLog() {
        viewModelScope.launch {
            val logs = repository.getRecentContraceptionLogs()
            _uiState.update { it.copy(contraceptionLogs = logs) }
        }
    }

    fun loadInitialData() {
        refreshEntries()
    }

    fun addEntry(entry: MoodEntry) {
        viewModelScope.launch {
            try {
                repository.addEntry(entry)
                refreshEntries()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteEntry(entry: MoodEntry) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(entry)
                refreshEntries()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun setMode(mode: CycleMode) {
        viewModelScope.launch {
            repository.setMode(mode)
        }
    }

    fun updateReminderEnabled(type: ReminderType, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReminder(type) { it.copy(enabled = enabled) }
            val config = repository.getReminderConfig(type)
            reminderScheduler.schedule(type, config)
        }
    }

    fun updateReminderTime(type: ReminderType, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateReminder(type) { config ->
                config.copy(hour = hour, minute = minute, snoozedUntilEpochMillis = null)
            }
            val config = repository.getReminderConfig(type)
            reminderScheduler.schedule(type, config)
        }
    }

    fun muteReminderForToday(type: ReminderType) {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            repository.updateReminder(type) { config ->
                config.copy(mutedUntilEpochDay = today, snoozedUntilEpochMillis = null)
            }
            val config = repository.getReminderConfig(type)
            reminderScheduler.schedule(type, config)
        }
    }

    fun recordContraception(method: ContraceptionMethod, date: LocalDate, skipped: Boolean = false) {
        viewModelScope.launch {
            repository.saveContraceptionLog(method, date, skipped)
            refreshContraceptionLog()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        database.close()
    }
}
