package com.app.mindcycle.data.model

/**
 * Stores reminder configuration persisted via DataStore.
 */
data class ReminderConfig(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val snoozedUntilEpochMillis: Long? = null,
    val mutedUntilEpochDay: Long? = null
)
