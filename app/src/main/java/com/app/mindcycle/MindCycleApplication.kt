package com.app.mindcycle

import android.app.Application
import com.app.mindcycle.reminders.ReminderNotifications
import com.jakewharton.threetenabp.AndroidThreeTen
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MindCycleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        MobileAds.initialize(this) {
            // SDK initialization completed
        }

        ReminderNotifications.ensureChannel(this)
    }
}
