package com.example.smartnotes.data.repository

import com.example.smartnotes.data.entities.NotasTareas
import kotlinx.coroutines.flow.Flow

interface NotasTareasRepository {
    /**Retrieve all the notas from the the given data source */
    fun getAllNotasStream(): Flow<List<NotasTareas>>

    /**Retrieve all the notas from the the given data source */
    fun getAllTareasStream(): Flow<List<NotasTareas>>

    /**Retrieve an item from the given data source that matches with the [id].*/
    fun getNotaTareaStream(id: Int): Flow<NotasTareas?>

    /**Insert item in the data source */
    suspend fun insertItem(item: NotasTareas)

    /**Delete item from the data source*/
    suspend fun deleteItem(item: NotasTareas)

    /**Update item in the data source*/
    suspend fun updateItem(item: NotasTareas)
}