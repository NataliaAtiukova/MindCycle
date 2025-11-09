package com.app.mindcycle.data.repository

import com.app.mindcycle.data.db.MoodEntryDao
import com.app.mindcycle.data.forecast.CycleForecastCalculator
import com.app.mindcycle.data.model.ContraceptionLogEntry
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

class CycleRepository(
    private val dao: MoodEntryDao,
    private val preferences: UserPreferencesManager,
    private val calculator: CycleForecastCalculator
) {

    val modeFlow: Flow<CycleMode> = preferences.modeFlow
    val reminderConfigsFlow: Flow<Map<ReminderType, ReminderConfig>> = preferences.reminderConfigs

    suspend fun loadEntries(monthsBack: Long = 6): List<MoodEntry> {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minus(monthsBack, ChronoUnit.MONTHS)
        return dao.getEntriesBetweenDates(startDate, endDate)
    }

    suspend fun addEntry(entry: MoodEntry) {
        dao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: MoodEntry) {
        dao.deleteEntry(entry)
    }

    suspend fun buildForecast(mode: CycleMode): CycleForecast {
        val starts = dao.getPeriodStarts()
        return calculator.forecast(starts, mode)
    }

    suspend fun setMode(mode: CycleMode) {
        preferences.setMode(mode)
    }

    suspend fun updateReminder(type: ReminderType, transform: (ReminderConfig) -> ReminderConfig) {
        preferences.updateReminder(type, transform)
    }

    suspend fun getReminderConfig(type: ReminderType): ReminderConfig = preferences.getReminderConfig(type)

    suspend fun saveContraceptionLog(method: ContraceptionMethod, date: LocalDate, isSkipped: Boolean = false) {
        val existing = dao.getContraceptionEntry(date, method)
        val entry = (existing ?: ContraceptionLogEntry(
            method = method,
            scheduledDate = date
        )).copy(
            takenAt = if (!isSkipped) LocalDateTime.now() else null,
            isSkipped = isSkipped,
            note = existing?.note
        )
        dao.upsertContraceptionEntry(entry)
    }

    suspend fun saveContraceptionEntry(entry: ContraceptionLogEntry) {
        dao.upsertContraceptionEntry(entry)
    }

    suspend fun getRecentContraceptionLogs(limit: Int = 14): List<ContraceptionLogEntry> {
        return dao.getRecentContraceptionEntries(limit)
    }
}
