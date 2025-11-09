package com.app.mindcycle

import android.app.Application
import com.app.mindcycle.data.preferences.UserPreferencesManager
import com.app.mindcycle.reminders.ReminderNotifications
import com.app.mindcycle.reminders.ReminderScheduler
import com.jakewharton.threetenabp.AndroidThreeTen
import com.yandex.mobile.ads.common.MobileAds

class MindCycleApplication : Application() {
    lateinit var userPreferencesManager: UserPreferencesManager
        private set
    lateinit var reminderScheduler: ReminderScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        
        // Initialize Yandex Mobile Ads SDK
        MobileAds.initialize(this) {
            // SDK initialization completed
        }

        userPreferencesManager = UserPreferencesManager(this)
        reminderScheduler = ReminderScheduler(this)
        ReminderNotifications.ensureChannel(this)
    }
}
