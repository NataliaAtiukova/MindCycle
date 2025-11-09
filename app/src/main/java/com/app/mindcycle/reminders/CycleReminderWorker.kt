package com.app.mindcycle.reminders

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.mindcycle.R
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.data.preferences.UserPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CycleReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val typeId = inputData.getString(KEY_REMINDER_TYPE) ?: return@withContext Result.success()
        val type = ReminderType.fromId(typeId)
        val prefs = UserPreferencesManager(applicationContext)
        val config = prefs.getReminderConfig(type)
        if (!config.enabled) {
            return@withContext Result.success()
        }

        ReminderNotifications.ensureChannel(applicationContext)
        prefs.snoozeUntil(type, null)

        val builder = NotificationCompat.Builder(applicationContext, ReminderNotifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(type.titleRes))
            .setContentText(applicationContext.getString(bodyText(type)))
            .setContentIntent(ReminderNotifications.buildContentIntent(applicationContext, type))
            .addAction(0, applicationContext.getString(R.string.action_snooze_15), ReminderNotifications.buildSnoozePendingIntent(applicationContext, type, 15))
            .addAction(0, applicationContext.getString(R.string.action_snooze_30), ReminderNotifications.buildSnoozePendingIntent(applicationContext, type, 30))
            .addAction(0, applicationContext.getString(R.string.action_snooze_60), ReminderNotifications.buildSnoozePendingIntent(applicationContext, type, 60))
            .addAction(0, applicationContext.getString(R.string.action_skip_today), ReminderNotifications.buildSkipTodayIntent(applicationContext, type))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setSilent(true)
        ReminderNotifications.notify(applicationContext, notificationId(type), builder)

        val scheduler = ReminderScheduler(applicationContext)
        val refreshedConfig = prefs.getReminderConfig(type)
        scheduler.schedule(type, refreshedConfig)

        Result.success()
    }

    private fun bodyText(type: ReminderType): Int = when (type) {
        ReminderType.DAY_LOG -> R.string.reminder_day_log_body
        ReminderType.SYMPTOM_LOG -> R.string.reminder_symptom_log_body
        ReminderType.CONTRACEPTION_INTAKE -> R.string.reminder_hc_body
    }

    private fun notificationId(type: ReminderType) = 500 + type.ordinal

    companion object {
        const val KEY_REMINDER_TYPE = "key_reminder_type"
    }
}
