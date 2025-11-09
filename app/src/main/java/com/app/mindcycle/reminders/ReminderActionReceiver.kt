package com.app.mindcycle.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.data.preferences.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeId = intent.getStringExtra(ReminderNotifications.EXTRA_REMINDER_TYPE) ?: return
        val type = ReminderType.fromId(typeId)
        val pendingResult = goAsync()
        val prefs = UserPreferencesManager(context)
        val scheduler = ReminderScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ReminderNotifications.ACTION_SNOOZE -> {
                        val minutes = intent.getIntExtra(ReminderNotifications.EXTRA_MINUTES, 15)
                        val snoozeUntil = System.currentTimeMillis() + minutes * 60 * 1000L
                        prefs.snoozeUntil(type, snoozeUntil)
                    }
                    ReminderNotifications.ACTION_SKIP_TODAY -> {
                        val today = LocalDate.now().toEpochDay()
                        prefs.muteForDay(type, today)
                        prefs.snoozeUntil(type, null)
                    }
                }
                val config = prefs.getReminderConfig(type)
                scheduler.schedule(type, config)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
