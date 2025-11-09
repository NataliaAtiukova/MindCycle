package com.app.mindcycle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.data.repository.CycleRepository
import com.app.mindcycle.reminders.ReminderScheduler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CycleRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private val gson = Gson()

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

    fun exportEntries(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val data = repository.getAllEntries()
            onResult(gson.toJson(data))
        }
    }

    fun importEntries(payload: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val listType = object : TypeToken<List<MoodEntry>>() {}.type
                val entries: List<MoodEntry> = gson.fromJson(payload, listType)
                repository.importEntries(entries)
                refreshEntries()
            }.onSuccess {
                onComplete(true)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
                onComplete(false)
            }
        }
    }
}
