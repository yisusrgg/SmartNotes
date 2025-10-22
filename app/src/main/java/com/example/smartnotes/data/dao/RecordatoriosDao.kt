package com.example.smartnotes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartnotes.data.entities.Recordatorios
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatoriosDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recordatorio: Recordatorios)

    @Update
    suspend fun update(recordatorio: Recordatorios)

    @Delete
    suspend fun delete(recordatorio: Recordatorios)

    //seleccionar todos los recordatorios de una tarea en especifico
    @Query("SELECT * FROM recordatorios WHERE id=:tareaId")
    fun getAllRecordatorios(tareaId: Int): Flow<List<Recordatorios>>

    //seleccionar un recordatorio en especifico
    @Query("SELECT * FROM recordatorios WHERE id=:id")
    fun getRecordatorio(id: Int): Flow<Recordatorios>
}