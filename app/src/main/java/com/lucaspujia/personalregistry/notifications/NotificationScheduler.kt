package com.lucaspujia.personalregistry.notifications

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationDay
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val WORK_NAME = "record_reminder_work"
        private const val TAG = "NotificationScheduler"
    }

    fun scheduleNotification(frequency: NotificationFrequency, dayOfWeek: NotificationDay, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)

        if (frequency == NotificationFrequency.OFF) {
            Log.d(TAG, "Recordatorios desactivados. Cancelando work: $WORK_NAME")
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val now = LocalDateTime.now()
        var scheduledTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        if (frequency == NotificationFrequency.DAYS_7) {
            scheduledTime = scheduledTime.with(TemporalAdjusters.nextOrSame(dayOfWeek.dayOfWeek))
        }

        if (scheduledTime.isBefore(now)) {
            scheduledTime = if (frequency == NotificationFrequency.DAYS_7) {
                scheduledTime.plusWeeks(1)
            } else {
                scheduledTime.plusDays(1)
            }
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        Log.d(TAG, "Hora actual: ${now.format(formatter)} | Próxima notificación: ${scheduledTime.format(formatter)}")

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(frequency.days, TimeUnit.DAYS)
            .setInitialDelay(Duration.between(now, scheduledTime))
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
