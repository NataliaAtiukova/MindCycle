package com.app.mindcycle.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.mindcycle.data.db.MoodDatabase
import com.app.mindcycle.data.db.MoodEntryDao
import com.app.mindcycle.data.model.CyclePrediction
import com.app.mindcycle.data.model.MoodEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import com.app.mindcycle.R

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MoodDatabase.buildDatabase(application)
    private val moodEntryDao = database.moodEntryDao()

    private val _entries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val entries: StateFlow<List<MoodEntry>> = _entries.asStateFlow()

    private val _cyclePrediction = MutableStateFlow<CyclePrediction?>(null)
    val cyclePrediction: StateFlow<CyclePrediction?> = _cyclePrediction.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            try {
                val endDate = LocalDateTime.now()
                val startDate = endDate.minus(3, ChronoUnit.MONTHS)
                _entries.value = moodEntryDao.getEntriesBetweenDates(startDate, endDate)
                // _cyclePrediction.value = moodEntryDao.predictNextCycle()
            } catch (e: Exception) {
                throw Exception("Failed to load data: ${e.message}")
            }
        }
    }

    fun addEntry(entry: MoodEntry) {
        viewModelScope.launch {
            try {
                moodEntryDao.insertEntry(entry)
                // Reload all entries to reflect the change (add or update)
                val endDate = LocalDateTime.now()
                val startDate = endDate.minus(3, ChronoUnit.MONTHS)
                _entries.value = moodEntryDao.getEntriesBetweenDates(startDate, endDate)
                // _cyclePrediction.value = moodEntryDao.predictNextCycle()
            } catch (e: Exception) {
                throw Exception("Failed to add entry: ${e.message}")
            }
        }
    }

    fun deleteEntry(entry: MoodEntry) {
        viewModelScope.launch {
            try {
                moodEntryDao.deleteEntry(entry)
                _entries.value = _entries.value - entry
                // _cyclePrediction.value = moodEntryDao.predictNextCycle()
            } catch (e: Exception) {
                throw Exception("Failed to delete entry: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        database.close()
    }
} 