package com.lucaspujia.personalregistry.notifications

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val WORK_NAME = "weight_reminder_work"
        private const val TAG = "NotificationScheduler"
    }

    fun scheduleNotification(frequency: NotificationFrequency, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)

        if (frequency == NotificationFrequency.OFF) {
            Log.d(TAG, "Recordatorios desactivados. Cancelando work: $WORK_NAME")
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val repeatInterval = when (frequency) {
            NotificationFrequency.DAYS_1 -> 1L
            NotificationFrequency.DAYS_3 -> 3L
            NotificationFrequency.DAYS_7 -> 7L
        }

        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Log.d(
            TAG,
            "Hora actual: ${formatter.format(currentTime.time)} | " +
                "Próxima notificación: ${formatter.format(scheduledTime.time)} | " +
                "Delay(ms): $initialDelay | Frecuencia(días): $repeatInterval"
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
