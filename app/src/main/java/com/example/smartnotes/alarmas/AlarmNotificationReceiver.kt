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
        Log.d("AlarmReceiver", "¡Alarma recibida! Intentando mostrar notificación...")

        // 1. Recuperar datos pasados desde el Scheduler
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Tienes una tarea pendiente"
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Recordatorio de Tarea"
        val taskId = intent.getIntExtra("EXTRA_TASK_ID", 0)

        val channelId = "task_reminders_channel"

        // 2. Crear Intent para abrir la App al tocar la notificación
        // (Opcional: Podrías navegar directo al detalle usando el taskId)
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Construir la Notificación (Basado en la documentación oficial)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener un icono válido
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Acción al tocar
            .setAutoCancel(true) // Se borra al tocarla

        // 4. Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Usamos el taskId (o un ID único) para identificar la notificación
        notificationManager.notify(taskId, builder.build())
    }
}

