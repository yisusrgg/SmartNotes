package com.example.smartnotes.ui

import androidx.lifecycle.ViewModelProvider
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

        // 1. Inicializador para ItemsListViewModel
        // Este ViewModel necesita ambos repositorios
        initializer {
            val container = smartNotesApplication().container
            ItemsListViewModel(
                notasTareasRepository = container.notasTareasRepository,
                archivosRepository = container.archivosRepository
            )
        }

        // 2. Inicializador para AddNoteTaskViewModel
        // Este ViewModel solo necesita el NotasTareasRepository
        initializer {
            val container = smartNotesApplication().container
            AddNoteTaskViewModel(
                repository = container.notasTareasRepository,
                archivosRepository = container.archivosRepository,
                recordatorioRepository = container.recordatoriosRepository
            )
        }

        /* // 3. Inicializador para DetailScreenViewModel (si creas uno)
        initializer {
            DetailScreenViewModel(
                this.createSavedStateHandle(), // Si necesitas el SavedStateHandle para el ID
                smartNotesApplication().container.notasTareasRepository,
                smartNotesApplication().container.archivosRepository
            )
        }
        */
    }
}