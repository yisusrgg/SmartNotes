package com.example.smartnotes.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.entities.NotasTareas
import com.example.smartnotes.data.repository.ArchivosRepository
import com.example.smartnotes.data.repository.NotasTareasRepository
import com.example.smartnotes.data.repository.RecordatoriosRepository
import com.example.smartnotes.ui.components.AndroidAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemsListViewModel(
    private val notasTareasRepository: NotasTareasRepository,
    private val archivosRepository: ArchivosRepository,
    private val recordatoriosRepository: RecordatoriosRepository,
    private val context: Context
) : ViewModel() {

    // Combina los flujos de Notas y Tareas de la base de datos
    val itemsUiState: StateFlow<List<NotaTareaUiModel>> = combine(
        notasTareasRepository.getAllTareasStream(), // Flow<List<NotasTareas>> (tipo="0")
        notasTareasRepository.getAllNotasStream()    // Flow<List<NotasTareas>> (tipo="1")
    ) { tareas, notas ->
        // Mapea las entidades de la BD a modelos de UI
        (tareas + notas)
    }
        .map { listaNotasTareas ->
            // Se usa map para iterar y llamar a la función suspend
                listaNotasTareas.map { notaTarea ->
                    // NOTA: Esta llamada debe ser suspendida
                    notaTarea.toUiModel(archivosRepository, recordatoriosRepository)
                }

        }
        //Mueve la ejecución de todo el mapeo pesado (las llamadas a la BD) a un hilo IO
        .flowOn(Dispatchers.IO)

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun updateItem(item: NotaTareaUiModel) {
        viewModelScope.launch {
            val id = item.id.toIntOrNull() ?: return@launch
            // Obtener la entidad original para preservar metadatos (fechas, etc.)
            val originalItem = notasTareasRepository.getNotaTareaById(id) ?: return@launch
            // Crear una nueva entidad, aplicando solo los cambios de la UI
            val updatedEntity = originalItem.copy(
                titulo = item.title,
                descripcion = item.description,
                // Solo actualizamos 'estaCumplida' si es una tarea
                estaCumplida = if (item is NotaTareaUiModel.Task) item.completed else originalItem.estaCumplida
            )
            //Actualizar la entidad completa
            notasTareasRepository.updateItem(updatedEntity)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            val id = itemId.toIntOrNull() ?: return@launch

            //Obtener la entidad completa
            val itemToDelete = notasTareasRepository.getNotaTareaById(id)

            if (itemToDelete != null) {
                //Ejecutar la eliminación.
                notasTareasRepository.deleteItem(itemToDelete)
            }
        }
    }

    //AUDIOS --------------
    private val player by lazy {
        AndroidAudioPlayer(context)
    }
}


// Mapeo de Entidad de BD a Modelo de UI
suspend fun NotasTareas.toUiModel(
    archivosRepository: ArchivosRepository,
    recordatoriosRepository : RecordatoriosRepository
): NotaTareaUiModel {

    val archivosAdjuntosEntities = archivosRepository.getAllArchivosStream(this.id).firstOrNull() ?: emptyList()
    val recordatoriosEntities = recordatoriosRepository.getAllRecordatoriosStream(this.id).firstOrNull() ?: emptyList()

    //lista de entidades a lista de ArchivoAdjuntoDetails y recordatoriosUiModel (Modelo de UI)
    val archivosUiModel = archivosAdjuntosEntities.map { archivoEntity ->
        ArchivoAdjuntoDetails(
            ruta = archivoEntity.ruta,
            tipoArchivo = archivoEntity.tipoArchivo
        )
    }
    val recordatoriosUiModel = recordatoriosEntities.map { recordatorioEntity ->
        RecordatorioDetails(
            opcionResId = recordatorioEntity.opcion,
            fechaMillis = recordatorioEntity.fecha
        )
    }

    return if (this.tipo == "task" || this.tipo == "0") {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        NotaTareaUiModel.Task(
            id = this.id.toString(),
            title = this.titulo,
            description = this.descripcion,
            dateTimeText = this.fechaCumplimiento?.let {
                sdf.format(Date.from(it.atZone(java.time.ZoneId.systemDefault()).toInstant()))
            } ?: "Sin fecha",
            completed = this.estaCumplida,
            attachments = archivosUiModel,
            reminders = recordatoriosUiModel
        )
    } else {
        NotaTareaUiModel.Note(
            id = this.id.toString(),
            title = this.titulo,
            description = this.descripcion,
            attachments = archivosUiModel
        )
    }
}

// Mapeo de Modelo de UI a Entidad de BD (para actualización)
fun NotaTareaUiModel.toNotasTareasEntity(): NotasTareas {
    // Necesitarás obtener la entidad original del repositorio para no perder datos como fechaRegistro, etc.
    // Pero para una actualización básica de estado, haremos una conversión parcial:
    return when (this) {
        is NotaTareaUiModel.Task -> NotasTareas(
            id = this.id.toIntOrNull() ?: 0,
            titulo = this.title,
            descripcion = this.description,
            tipo = "task",
            estaCumplida = this.completed,
            fechaRegistro = java.time.LocalDateTime.MIN, // **CUIDADO: Esto debe ser cargado de la BD.**
            fechaCumplimiento = null // **CUIDADO: Esto debe ser cargado de la BD.**
        )
        is NotaTareaUiModel.Note -> NotasTareas(
            id = this.id.toIntOrNull() ?: 0,
            titulo = this.title,
            descripcion = this.description,
            tipo = "note",
            estaCumplida = false,
            fechaRegistro = java.time.LocalDateTime.MIN, // **CUIDADO**
            fechaCumplimiento = null // **CUIDADO**
        )
    }
}