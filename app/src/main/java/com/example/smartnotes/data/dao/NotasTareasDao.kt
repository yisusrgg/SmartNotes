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

    //traer una nota o tarea en especifico
    @Query("SELECT * from notasTareas WHERE id = :id")
    fun getNotaTarea(id: Int): Flow<NotasTareas>

    //traer todas las notas ornedas por fecha de registro
    @Query("SELECT * from notasTareas WHERE tipo = 0 ORDER BY fechaRegistro")
    fun getAllNotas(): Flow<List<NotasTareas>>

    //traer todas la tareas ordenas por fecha de cumplimiento
    @Query("SELECT * from notasTareas WHERE tipo = 1 ORDER BY fechaCumplimiento")
    fun getAllTareas(): Flow<List<NotasTareas>>
}