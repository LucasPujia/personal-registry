package com.lucaspujia.personalregistry.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Record
import com.lucaspujia.personalregistry.database.registry.RecordsStorage
import com.lucaspujia.personalregistry.database.registry.RegistriesStorage
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Receptor para procesar la entrada directa desde la notificación.
 * Permite al usuario añadir un valor sin abrir la aplicación.
 */
@AndroidEntryPoint
class NotificationReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recordsStorage: RecordsStorage

    @Inject
    lateinit var registriesStorage: RegistriesStorage

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val registryId = intent.getLongExtra(EXTRA_REGISTRY_ID, -1L)
        if (registryId == -1L) return

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val valueStr = remoteInput?.getCharSequence(KEY_TEXT_REPLY)?.toString()
        val value = valueStr?.replace(',', '.')?.toDoubleOrNull()

        CoroutineScope(Dispatchers.IO).launch {
            val registry = registriesStorage.getRegistryById(registryId) ?: return@launch

            if (value != null) {
                // Guardar el registro en un hilo de fondo
                val newRecord = Record(
                    registryId = registryId,
                    value1 = value,
                    dateKey = localDateToDateKey(now()),
                    createdAt = forDatePicker(LocalDate.now())
                )
                recordsStorage.insertRecord(newRecord)

                // Reemplazar por notificación de éxito
                notificationHelper.showSuccessNotification(registry)
            } else {
                // Notificar error y volver a pedir
                notificationHelper.showReminderNotification(
                    registry = registry,
                    errorMessage = context.getString(R.string.invalid_value)
                )
            }
        }
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val EXTRA_REGISTRY_ID = "extra_registry_id"
    }
}
