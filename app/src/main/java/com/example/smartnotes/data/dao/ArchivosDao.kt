package com.example.smartnotes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartnotes.data.entities.ArchivosAdjuntos
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchivosDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(archivos: ArchivosAdjuntos)

    @Update
    suspend fun update(archivos: ArchivosAdjuntos)

    @Delete
    suspend fun delete(archivos: ArchivosAdjuntos)

    @Query("SELECT * FROM archivos")
    fun getAllArchivos(): Flow<List<ArchivosAdjuntos>>

    //todos los archivos de una nota o tarea
    @Query("SELECT * FROM archivos WHERE id = :notaTareaId")
    fun getArchivoById(notaTareaId: Int): Flow<List<ArchivosAdjuntos>>
}