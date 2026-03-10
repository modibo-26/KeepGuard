package com.modibo.keepguard.data.worker

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    private val manager: WorkManager
) {

    fun schedule(id: String, title: String, message: String, triggerAtMillis: Long) {

        val delay = triggerAtMillis -  System.currentTimeMillis()
//        Delay de test des notification (10 sec)
//        val delay = 10_000L
        val data =  workDataOf("title" to title, "message" to message)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(id)
            .build()
        manager.enqueue(request)
    }

    fun cancel(tag: String) {
        manager.cancelAllWorkByTag(tag)
    }

}