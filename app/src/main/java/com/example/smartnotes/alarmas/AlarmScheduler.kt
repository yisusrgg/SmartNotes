package com.example.smartnotes.alarmas

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId

//AlarmScheduler =========================================================
data class AlarmItem(
    val alarmTime: LocalDateTime,
    val message: String,
    val title: String,
    val taskId: Int,     // ID de la tarea para referencia
    val reminderId: Int  // ID del recordatorio para que sea único
)

interface AlarmScheduler {
    fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    //private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @SuppressLint("ScheduleExactAlarm")
    override fun schedule(item: AlarmItem) {
        // Crear el Intent que irá al Receiver
        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
            putExtra("EXTRA_TITLE", item.title)
            putExtra("EXTRA_TASK_ID", item.taskId)
        }

        // Crear PendingIntent único usando el reminderId como requestCode
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.reminderId, // ID ÚNICO PARA CADA RECORDATORIO
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Convertir LocalDateTime a milisegundos
        val alarmMillis = item.alarmTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Programar la alarma exacta
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmMillis,
            pendingIntent
        )
    }

    override fun cancel(item: AlarmItem) {
        val intent = Intent(context, AlarmNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.reminderId, // Debe coincidir con el ID usado al crearla
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
