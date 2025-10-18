package com.example.smartnotes.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "archivos",
    foreignKeys = [
        ForeignKey(
            entity = NotasTareas::class,
            parentColumns = ["id"],
            childColumns = ["notaTareaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notaTareaId")]
)
data class ArchivosAdjuntos(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notaTareaId: Int,
    val tipoArchivo: String,
    val ruta: String
)
