/*
package com.example.smartnotes.ui.viewmodels

// EditNoteTaskViewModel.kt

import androidx.lifecycle.SavedStateHandle // 🛑 Importante para obtener el ID de la navegación
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.repository.ArchivosRepository
import com.example.smartnotes.data.repository.NotasTareasRepository
import com.example.smartnotes.data.repository.RecordatoriosRepository
import kotlinx.coroutines.launch

// ... (Otros imports necesarios: Repositorios, UiState, etc.)

class EditNoteTaskViewModel(
    private val savedStateHandle: SavedStateHandle, // Recibido por inyección
    private val notasTareasRepository: NotasTareasRepository,
    private val archivosRepository: ArchivosRepository,
    private val recordatoriosRepository: RecordatoriosRepository
    // ... (Inicializadores de estado de UI como en AddNoteTaskViewModel)
) : ViewModel() {

    // 1. Obtener el ID del argumento de navegación
    private val itemId: Int = checkNotNull(savedStateHandle["itemId"])

    init {
        // 2. Iniciar la carga de datos al crearse el ViewModel
        viewModelScope.launch {
            loadItemDetails()
        }
    }

    private suspend fun loadItemDetails() {
        // Cargar la entidad principal
        val notaTareaEntity = notasTareasRepository.getNotaTareaById(itemId) ?: return

        // Mapear a NotaTareaDetails (ya existe la extensión toNotaTareaDetails)
        val details = notaTareaEntity.toNotaTareaDetails()

        // 🛑 Tarea Pendiente: Cargar Archivos y Recordatorios de sus repositorios
        // y asignarlos a las propiedades 'attachments', 'audios' y 'recordatoriosList'

        // 3. Actualizar el estado de la UI del formulario con los datos cargados
        updateUiState(
            NotaTareaDetails(
                id = notaTareaEntity.id,
                titulo = notaTareaEntity.titulo,
                // ... mapear todos los campos ...
            )
        )
    }

    // 🛑 Reutiliza las funciones de AddNoteTaskViewModel
    // (updateUiState, validateInput, saveNotaTarea, etc.)

    suspend fun saveNotaTarea() {
        // Usaremos la misma lógica de guardado/actualización, pero ahora el ID es != 0
        // Room sabrá que es una ACTUALIZACIÓN si el ID existe.
        // ... (Lógica de saveNotaTarea de AddNoteTaskViewModel)
    }
}*/
