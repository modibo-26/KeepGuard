package com.modibo.keepguard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.modibo.keepguard.data.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KeepGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}