package com.example.smartnotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.entities.NotasTareas
import com.example.smartnotes.data.repository.ArchivosRepository
import com.example.smartnotes.data.repository.NotasTareasRepository
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
    private val archivosRepository: ArchivosRepository
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
            // Debemos usar un 'map' de Kotlin y llamar a la función suspend
            withContext(Dispatchers.IO) {
                listaNotasTareas.map { notaTarea ->
                    // NOTA: Esta llamada debe ser suspendida
                    notaTarea.toUiModel(archivosRepository)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /*fun updateItem(item: NotaTareaUiModel) {
        viewModelScope.launch {
            // Convierte el UiModel de vuelta a NotasTareas antes de actualizar
            notasTareasRepository.updateItem(item.toNotasTareasEntity())
        }
    }*/
    // En ItemsListViewModel.kt

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
}


// Mapeo de Entidad de BD a Modelo de UI
suspend fun NotasTareas.toUiModel(archivosRepository: ArchivosRepository): NotaTareaUiModel {
    val archivos = archivosRepository.getAllArchivosStream(this.id).firstOrNull() ?: emptyList()
    val rutasArchivos = archivos.filter { it.tipoArchivo != "audio" }.map { it.ruta }
    val rutasAudios = archivos.filter { it.tipoArchivo == "audio" }.map { it.ruta }

    return if (this.tipo == "task" || this.tipo == "0") {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        NotaTareaUiModel.Task(
            id = this.id.toString(),
            title = this.titulo,
            description = this.descripcion,
            dateTimeText = this.fechaCumplimiento?.let { sdf.format(Date.from(it.atZone(java.time.ZoneId.systemDefault()).toInstant())) } ?: "Sin fecha",
            completed = this.estaCumplida,
            attachments = rutasArchivos,
            audios = rutasAudios
        )
    } else {
        NotaTareaUiModel.Note(
            id = this.id.toString(),
            title = this.titulo,
            description = this.descripcion,
            attachments = rutasArchivos,
            audios = rutasAudios
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