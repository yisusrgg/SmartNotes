package com.example.smartnotes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartnotes.data.entities.NotasTareas
import kotlinx.coroutines.flow.Flow


@Dao
interface NotasTareasDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notasTareas: NotasTareas)

    @Update
    suspend fun update(notasTareas: NotasTareas)

    @Delete
    suspend fun delete(notasTareas: NotasTareas)

    @Query("SELECT * from notasTareas WHERE id = :id")
    fun getNotaTarea(id: Int): Flow<NotasTareas>

    @Query("SELECT * from notasTareas WHERE tipo = :tipo ORDER BY fechaRegistro")
    fun getAllNotas(tipo: Int): Flow<NotasTareas>

    @Query("SELECT * from notasTareas WHERE tipo = :tipo ORDER BY fechaCumplimiento")
    fun getAllTareas(tipo: Int): Flow<NotasTareas>
}