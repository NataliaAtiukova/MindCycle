package com.app.mindcycle.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleAll(configs: Map<ReminderType, ReminderConfig>) {
        configs.forEach { (type, config) ->
            schedule(type, config)
        }
    }

    fun schedule(type: ReminderType, config: ReminderConfig) {
        val delayMillis = computeDelayMillis(config)
        val workName = workName(type)
        if (!config.enabled || delayMillis == null) {
            workManager.cancelUniqueWork(workName)
            return
        }

        val request = OneTimeWorkRequestBuilder<CycleReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    CycleReminderWorker.KEY_REMINDER_TYPE to type.id
                )
            )
            .build()

        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(type: ReminderType) {
        workManager.cancelUniqueWork(workName(type))
    }

    private fun computeDelayMillis(config: ReminderConfig): Long? {
        val now = ZonedDateTime.now()
        config.snoozedUntilEpochMillis?.let { snoozed ->
            val diff = snoozed - System.currentTimeMillis()
            if (diff > 0) return diff
        }

        var target = now
            .withHour(config.hour)
            .withMinute(config.minute)
            .withSecond(0)
            .withNano(0)

        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }

        val today = now.toLocalDate().toEpochDay()
        if (config.mutedUntilEpochDay != null && config.mutedUntilEpochDay == today) {
            target = target.plusDays(1)
        }

        val duration = Duration.between(now, target)
        val millis = duration.toMillis()
        return millis.takeIf { it > 0 }
    }

    private fun workName(type: ReminderType) = "cycle_reminder_${type.id}"
}
