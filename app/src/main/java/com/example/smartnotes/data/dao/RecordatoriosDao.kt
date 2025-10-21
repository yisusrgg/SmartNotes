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

    @Query("SELECT * FROM recordatorios WHERE id=:id")
    fun getAllRecordatorios(id: Int): Flow<List<Recordatorios>>
}