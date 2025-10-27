package com.example.smartnotes.data

import android.content.Context
import com.example.smartnotes.data.database.SmartNotesDatabase
import com.example.smartnotes.data.repository.ArchivosRepository
import com.example.smartnotes.data.repository.NotasTareasRepository
import com.example.smartnotes.data.repository.OfflineArchivosRepository
import com.example.smartnotes.data.repository.OfflineNotasTareasRepository
import com.example.smartnotes.data.repository.OfflineRecordatoriosRepository
import com.example.smartnotes.data.repository.RecordatoriosRepository

interface AppContainer {
    val notasTareasRepository: NotasTareasRepository
    val archivosRepository: ArchivosRepository
    val recordatoriosRepository: RecordatoriosRepository
}

/**
 * Implementación concreta del contenedor de dependencias.
 * Crea las instancias de base de datos y repositorios.
 */
class AppDataContainer(private val context: Context) : AppContainer {
    // Base de datos
    private val database by lazy {
        SmartNotesDatabase.getDatabase(context)
    }

    // Repositorio de Notas y Tareas
    override val notasTareasRepository: NotasTareasRepository by lazy {
        OfflineNotasTareasRepository(
            database.notasTareasDao()
        )
    }

    // Repositorio de Archivos Adjuntos
    override val archivosRepository: ArchivosRepository by lazy {
        OfflineArchivosRepository(database.archivosDao())
    }

    // Repositorio de Recordatorios
    override val recordatoriosRepository: RecordatoriosRepository by lazy {
        OfflineRecordatoriosRepository(database.recordatoriosDao())
    }
}