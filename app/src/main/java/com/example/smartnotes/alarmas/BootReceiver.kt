package com.example.smartnotes.alarmas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartnotes.SmartNotesApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Dispositivo reiniciado. Reprogramando alarmas...")

            val app = context.applicationContext as SmartNotesApplication
            val recordatorioRepository = app.container.recordatoriosRepository
            val notasTareasRepository = app.container.notasTareasRepository
            val alarmScheduler = AndroidAlarmScheduler(context)

            val scope = CoroutineScope(Dispatchers.IO)
            val pendingResult = goAsync()

            scope.launch {
                try {
                    // CORRECCIÓN: Usamos el método que sí existe en tu repositorio: getAllTareasStream()
                    val todasLasTareas = notasTareasRepository.getAllTareasStream().first()
                    
                    // Filtramos solo las que no están cumplidas
                    val tareasPendientes = todasLasTareas.filter { !it.estaCumplida }

                    tareasPendientes.forEach { tarea ->
                        val recordatorios = recordatorioRepository.getAllRecordatoriosStream(tarea.id).first()
                        
                        recordatorios.forEach { recordatorio ->
                            if (!recordatorio.fueNotificado) {
                                val alarmTime = java.time.Instant.ofEpochMilli(recordatorio.fecha)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime()

                                // Solo reprogramar si es en el futuro
                                if (alarmTime.isAfter(LocalDateTime.now())) {
                                    val alarmItem = AlarmItem(
                                        alarmTime = alarmTime,
                                        message = "Tienes pendiente: ${tarea.descripcion.take(20)}...",
                                        title = tarea.titulo,
                                        taskId = tarea.id,
                                        reminderId = recordatorio.id 
                                    )
                                    alarmScheduler.schedule(alarmItem)
                                    Log.d("BootReceiver", "Alarma reprogramada para tarea: ${tarea.titulo}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error al reprogramar alarmas", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
