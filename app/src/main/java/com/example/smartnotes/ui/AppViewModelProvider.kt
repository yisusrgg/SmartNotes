package com.example.smartnotes.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartnotes.SmartNotesApplication
import com.example.smartnotes.ui.viewmodels.AddNoteTaskViewModel
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel

/**
 * Función de extensión para obtener la instancia de SmartNotesApplication
 * a partir de las CreationExtras.
 */
fun CreationExtras.smartNotesApplication(): SmartNotesApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartNotesApplication)

object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Inicializador para ItemsListViewModel
        initializer {
            val container = smartNotesApplication().container
            ItemsListViewModel(
                notasTareasRepository = container.notasTareasRepository,
                archivosRepository = container.archivosRepository,
                recordatoriosRepository = container.recordatoriosRepository,
                context = smartNotesApplication()
            )
        }

        // Inicializador para AddNoteTaskViewModel
        initializer {
            val container = smartNotesApplication().container
            AddNoteTaskViewModel(
                repository = container.notasTareasRepository,
                archivosRepository = container.archivosRepository,
                recordatorioRepository = container.recordatoriosRepository,
                context = smartNotesApplication()
            )
        }
    }
}