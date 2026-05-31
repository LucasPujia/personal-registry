package com.lucaspujia.personalregistry.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val WORK_NAME = "weight_reminder_work"
    }

    fun scheduleNotification(frequency: NotificationFrequency) {
        val workManager = WorkManager.getInstance(context)

        if (frequency == NotificationFrequency.OFF) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val repeatInterval = when (frequency) {
            NotificationFrequency.DAYS_1 -> 1L
            NotificationFrequency.DAYS_3 -> 3L
            NotificationFrequency.DAYS_7 -> 7L
        }

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
