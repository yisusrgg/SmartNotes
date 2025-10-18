package com.example.smartnotes.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateTimeConverters {
    /**
     * Convierte un Long (milisegundos) de la base de datos a un objeto LocalDateTime.
     * Usamos UTC como referencia de zona horaria para la conversión.
     */
    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let {
            // Instant.ofEpochMilli(it) crea un punto en el tiempo
            // .atOffset(ZoneOffset.UTC) lo convierte a LocalDateTime
            Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDateTime()
        }
    }

    /**
     * Convierte un objeto LocalDateTime en un Long (milisegundos) para la base de datos.
     * Usamos UTC como referencia de zona horaria para la conversión.
     */
    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): Long? {
        return date?.let {
            // .toInstant(ZoneOffset.UTC) obtiene el instante,
            // .toEpochMilli() obtiene los milisegundos
            it.toInstant(ZoneOffset.UTC).toEpochMilli()
        }
    }
}