package com.example.smartnotes.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recordatorios",
    foreignKeys = [
        ForeignKey(
            entity = NotasTareas::class,
            parentColumns = ["id"],
            childColumns = ["tareaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Recordatorios(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tareaId: Int,
    val fecha: Long,
    val fueNotificado: Boolean = false
)
