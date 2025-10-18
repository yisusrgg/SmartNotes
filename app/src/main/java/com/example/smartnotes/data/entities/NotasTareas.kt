package com.example.smartnotes.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notasTareas")
data class NotasTareas(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val tipo: Int, // "0 nota" o "1 tarea"
    val estaCumplida: Boolean = false,
    val fechaRegistro: LocalDateTime,
    val fechaCumplimiento: LocalDateTime? = null
)
