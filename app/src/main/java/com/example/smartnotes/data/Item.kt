package com.example.smartnotes.data

import java.util.UUID

sealed class Item {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val completed: Boolean
    abstract val attachments: List<String> // URIs/paths de imágenes/archivos
    abstract val audios: List<String> // Paths de audios

    data class Task(
        override val id: String = UUID.randomUUID().toString(),
        override var title: String,
        override var description: String,
        var dateTimeText: String,
        override var completed: Boolean = false,
        override val attachments: List<String> = emptyList(),
        override val audios: List<String> = emptyList(),
        var reminderText: String = "1 día antes"
    ) : Item()

    data class Note(
        override val id: String = UUID.randomUUID().toString(),
        override var title: String,
        override var description: String,
        override var completed: Boolean = false,
        override val attachments: List<String> = emptyList(),
        override val audios: List<String> = emptyList()
    ) : Item()
}