package com.lucaspujia.personalregistry.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "record_reminders"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification(registry: Registry? = null, errorMessage: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = registry?.id?.toInt() ?: NOTIFICATION_ID
        val title = if (errorMessage != null) {
            "${context.getString(R.string.warning)}: ${registry?.name ?: ""}"
        } else {
            registry?.let { "${it.emoji} ${it.name}" } ?: context.getString(R.string.notification_title)
        }
        
        val text = when {
            errorMessage != null -> errorMessage
            registry != null -> context.getString(R.string.notification_text_registry, registry.name)
            else -> context.getString(R.string.notification_text)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Usar un icono de notificación apropiado
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (registry != null) {
            val replyLabel = context.getString(R.string.notification_reply_title)
            val remoteInput = RemoteInput.Builder(NotificationReplyReceiver.KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build()

            val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
                putExtra(NotificationReplyReceiver.EXTRA_REGISTRY_ID, registry.id)
            }
            val replyPendingIntent = PendingIntent.getBroadcast(
                context,
                registry.id.toInt(),
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_add,
                context.getString(R.string.add),
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            builder.addAction(action)
        }

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (_: SecurityException) {}
        }
    }

    fun showSuccessNotification(registry: Registry) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${registry.emoji} ${registry.name}")
            .setContentText(context.getString(R.string.record_added_success))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(registry.id.toInt(), builder.build())
            } catch (_: SecurityException) {}
        }
    }
}
