package com.app.mindcycle.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.mindcycle.MainActivity
import com.app.mindcycle.R
import com.app.mindcycle.data.model.ReminderType

object ReminderNotifications {
    const val CHANNEL_ID = "cycle_reminders"
    const val EXTRA_DEEP_LINK = "reminder_deep_link"
    const val EXTRA_REMINDER_TYPE = "reminder_type"
    const val ACTION_SNOOZE = "com.app.mindcycle.action.SNOOZE"
    const val ACTION_SKIP_TODAY = "com.app.mindcycle.action.SKIP_TODAY"
    const val EXTRA_MINUTES = "reminder_minutes"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.reminder_channel_desc)
                setShowBadge(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildContentIntent(context: Context, type: ReminderType): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, type.deepLinkRoute)
        }
        return PendingIntent.getActivity(
            context,
            100 + type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun buildSnoozePendingIntent(context: Context, type: ReminderType, minutes: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_TYPE, type.id)
            putExtra(EXTRA_MINUTES, minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            200 + type.ordinal * 10 + minutes,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun buildSkipTodayIntent(context: Context, type: ReminderType): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SKIP_TODAY
            putExtra(EXTRA_REMINDER_TYPE, type.id)
        }
        return PendingIntent.getBroadcast(
            context,
            300 + type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun notify(context: Context, id: Int, builder: NotificationCompat.Builder) {
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
