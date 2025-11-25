package com.example.smartnotes.data.repository

import com.example.smartnotes.data.entities.Recordatorios
import kotlinx.coroutines.flow.Flow

interface RecordatoriosRepository {
    /**Retrieve all the recordatorios from the the given data source with the [id]*/
    fun getAllRecordatoriosStream(tareaId: Int): Flow<List<Recordatorios>>

    /**Retrieve an recordatorio from the given data source that matches with the [id].*/
    fun getRecordatoriStream(id: Int): Flow<Recordatorios?>

    /**Insert item in the data source and return id*/
    suspend fun insertItem(item: Recordatorios): Long

    /**Delete item from the data source*/
    suspend fun deleteItem(item: Recordatorios)

    /**Update item in the data source*/
    suspend fun updateItem(item: Recordatorios)
}