package com.app.mindcycle.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "mindcycle_user_prefs"

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class UserPreferencesManager(private val context: Context) {

    private val dataStore = context.userPreferencesDataStore

    val modeFlow: Flow<CycleMode> = dataStore.data.map { prefs ->
        CycleMode.fromId(prefs[MODE_KEY])
    }

    val reminderConfigs: Flow<Map<ReminderType, ReminderConfig>> = dataStore.data.map { prefs ->
        ReminderType.values().associateWith { type ->
            reminderFromPreferences(type, prefs)
        }
    }

    suspend fun setMode(mode: CycleMode) {
        dataStore.edit { prefs ->
            prefs[MODE_KEY] = mode.id
        }
    }

    suspend fun updateReminder(type: ReminderType, transform: (ReminderConfig) -> ReminderConfig) {
        dataStore.edit { prefs ->
            val current = reminderFromPreferences(type, prefs)
            val updated = transform(current)
            prefs[enabledKey(type)] = updated.enabled
            prefs[hourKey(type)] = updated.hour
            prefs[minuteKey(type)] = updated.minute
            prefs[snoozeKey(type)] = updated.snoozedUntilEpochMillis ?: 0L
            prefs[mutedKey(type)] = updated.mutedUntilEpochDay ?: 0L
        }
    }

    suspend fun snoozeUntil(type: ReminderType, epochMillis: Long?) {
        updateReminder(type) { config ->
            config.copy(snoozedUntilEpochMillis = epochMillis)
        }
    }

    suspend fun muteForDay(type: ReminderType, epochDay: Long?) {
        updateReminder(type) { config ->
            config.copy(mutedUntilEpochDay = epochDay)
        }
    }

    suspend fun getReminderConfig(type: ReminderType): ReminderConfig {
        val prefs = dataStore.data.first()
        return reminderFromPreferences(type, prefs)
    }

    private fun reminderFromPreferences(type: ReminderType, prefs: Preferences): ReminderConfig {
        val defaults = defaultTime(type)
        return ReminderConfig(
            enabled = prefs[enabledKey(type)] ?: false,
            hour = prefs[hourKey(type)] ?: defaults.first,
            minute = prefs[minuteKey(type)] ?: defaults.second,
            snoozedUntilEpochMillis = prefs[snoozeKey(type)]?.takeIf { it != 0L },
            mutedUntilEpochDay = prefs[mutedKey(type)]?.takeIf { it != 0L }
        )
    }

    private fun enabledKey(type: ReminderType) = booleanPreferencesKey("${type.id}_enabled")
    private fun hourKey(type: ReminderType) = intPreferencesKey("${type.id}_hour")
    private fun minuteKey(type: ReminderType) = intPreferencesKey("${type.id}_minute")
    private fun snoozeKey(type: ReminderType) = longPreferencesKey("${type.id}_snoozed_until")
    private fun mutedKey(type: ReminderType) = longPreferencesKey("${type.id}_muted_until")

    private fun defaultTime(type: ReminderType): Pair<Int, Int> = when (type) {
        ReminderType.DAY_LOG -> 9 to 0
        ReminderType.SYMPTOM_LOG -> 12 to 0
        ReminderType.CONTRACEPTION_INTAKE -> 21 to 0
    }

    companion object {
        private val MODE_KEY = stringPreferencesKey("cycle_mode")
    }
}
