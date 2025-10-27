package com.example.smartnotes.data.repository

import com.example.smartnotes.data.dao.NotasTareasDao
import com.example.smartnotes.data.entities.NotasTareas
import kotlinx.coroutines.flow.Flow

class OfflineNotasTareasRepository  (
    private val notasTareasDao : NotasTareasDao
) : NotasTareasRepository {
    override fun getAllNotasStream(): Flow<List<NotasTareas>> =
        notasTareasDao.getAllNotas()

    override fun getAllTareasStream(): Flow<List<NotasTareas>> =
        notasTareasDao.getAllTareas()

    override fun getNotaTareaStream(id: Int): Flow<NotasTareas?> =
        notasTareasDao.getNotaTarea(id)

    override suspend fun insertItem(item: NotasTareas) =
        notasTareasDao.insert(item)

    override suspend fun deleteItem(item: NotasTareas) =
        notasTareasDao.delete(item)

    override suspend fun updateItem(item: NotasTareas) =
        notasTareasDao.update(item)
    override suspend fun getNotaTareaById(id: Int): NotasTareas? =
        notasTareasDao.getNotaTareaById(id)

}