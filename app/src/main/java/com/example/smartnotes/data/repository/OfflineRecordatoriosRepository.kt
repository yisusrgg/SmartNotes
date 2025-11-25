package com.example.smartnotes.data.repository

import com.example.smartnotes.data.dao.RecordatoriosDao
import com.example.smartnotes.data.entities.Recordatorios
import kotlinx.coroutines.flow.Flow

class OfflineRecordatoriosRepository(
    private val recordatoriosDao: RecordatoriosDao
) : RecordatoriosRepository {
    override fun getAllRecordatoriosStream(tareaId: Int): Flow<List<Recordatorios>> =
        recordatoriosDao.getAllRecordatorios(tareaId)

    override fun getRecordatoriStream(id: Int): Flow<Recordatorios?> =
        recordatoriosDao.getRecordatorio(id)

    override suspend fun insertItem(item: Recordatorios): Long =
        recordatoriosDao.insert(item)

    override suspend fun deleteItem(item: Recordatorios) =
        recordatoriosDao.delete(item)

    override suspend fun updateItem(item: Recordatorios) =
        recordatoriosDao.update(item)

}