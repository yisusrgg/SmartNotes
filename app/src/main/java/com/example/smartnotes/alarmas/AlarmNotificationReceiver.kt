package com.example.smartnotes.alarmas

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartnotes.MainActivity
import com.example.smartnotes.R
import java.time.LocalDateTime
import java.time.ZoneId


class AlarmNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Recuperar datos pasados desde el Scheduler
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: R.string.reminder_message_defaul.toString()
        val title = intent.getStringExtra("EXTRA_TITLE") ?: R.string.reminder_title_defaul.toString()
        val taskId = intent.getIntExtra("EXTRA_TASK_ID", 0)

        val channelId = "task_reminders_channel"

        // Crear Intent para abrir la App al tocar la notificación
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la Notificación (documentación oficial)
        val builder = NotificationCompat.Builder(context, channelId)
            //.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSmallIcon(R.drawable.smart_icono)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Acción al tocar
            .setAutoCancel(true)

        //Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Usamos el taskId (o un ID único) para identificar la notificación
        notificationManager.notify(taskId, builder.build())
    }
}

