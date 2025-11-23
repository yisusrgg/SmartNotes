package com.example.smartnotes.ui.viewmodels

// Este es el modelo que la UI consumirá (reemplaza a la clase Item)
sealed class NotaTareaUiModel {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val attachments: List<ArchivoAdjuntoDetails> // Rutas de archivos

    data class Task(
        override val id: String,
        override val title: String,
        override val description: String,
        val dateTimeText: String, // Fecha/Hora de cumplimiento formateada
        val completed: Boolean = false,
        override val attachments: List<ArchivoAdjuntoDetails> = emptyList(),
        val reminders: List<RecordatorioDetails> = emptyList(),
    ) : NotaTareaUiModel()

    data class Note(
        override val id: String,
        override val title: String,
        override val description: String,
        override val attachments: List<ArchivoAdjuntoDetails> = emptyList()
    ) : NotaTareaUiModel()
}