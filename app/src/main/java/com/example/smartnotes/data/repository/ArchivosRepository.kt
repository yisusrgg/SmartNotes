package com.example.smartnotes.data.repository

import com.example.smartnotes.data.entities.ArchivosAdjuntos
import kotlinx.coroutines.flow.Flow

interface ArchivosRepository {
    /**Retrieve all the archivos from the the given data source that matches with the [id]*/
    fun getAllArchivosStream(notaTareaId: Int): Flow<List<ArchivosAdjuntos>>

    /**Retrieve an item from the given data source that matches with the [id].*/
    fun getArchivoStream(id: Int): Flow<ArchivosAdjuntos?>

    /**Insert item in the data source */
    suspend fun insertItem(item: ArchivosAdjuntos)

    /**Delete item from the data source*/
    suspend fun deleteItem(item: ArchivosAdjuntos)

    /**Update item in the data source*/
    suspend fun updateItem(item: ArchivosAdjuntos)
}