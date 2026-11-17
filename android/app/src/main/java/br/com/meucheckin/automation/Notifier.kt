package br.com.meucheckin.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object Notifier {
    private const val CHANNEL_ID = "checkin_automation_channel"

    fun notify(context: Context, titulo: String, mensagem: String, sucesso: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Check-in Automático",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de status de check-in"
            }
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }
}

