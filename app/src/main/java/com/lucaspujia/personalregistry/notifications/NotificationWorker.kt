package com.lucaspujia.personalregistry.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lucaspujia.personalregistry.database.registry.RecordsStorage
import com.lucaspujia.personalregistry.database.registry.RegistriesStorage
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val registriesStorage: RegistriesStorage,
    private val recordsStorage: RecordsStorage,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val registries = registriesStorage.getAllRegistriesFlow().first()
        val todayKey = localDateToDateKey(now())

        if (registries.isEmpty()) {
            notificationHelper.showReminderNotification()
        } else {
            registries.forEach { registry ->
                val records = recordsStorage.getRecordsByRegistry(registry.id)
                val hasRecordToday = records.any { it.dateKey == todayKey }
                
                if (!hasRecordToday) {
                    notificationHelper.showReminderNotification(registry)
                }
            }
        }

        return Result.success()
    }
}
