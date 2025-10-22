package com.example.smartnotes.data.repository

import com.example.smartnotes.data.dao.ArchivosDao
import com.example.smartnotes.data.entities.ArchivosAdjuntos
import kotlinx.coroutines.flow.Flow

class OfflineArchivosRepository(
    private val archivosDao: ArchivosDao
) : ArchivosRepository {


    override fun getAllArchivosStream(notaTareaid: Int): Flow<List<ArchivosAdjuntos>> =
        archivosDao.getAllArchivos(notaTareaid)

    override fun getArchivoStream(id: Int): Flow<ArchivosAdjuntos?> =
        archivosDao.getArchivoById(id)

    override suspend fun insertItem(item: ArchivosAdjuntos) =
        archivosDao.insert(item)

    override suspend fun deleteItem(item: ArchivosAdjuntos) =
        archivosDao.delete(item)

    override suspend fun updateItem(item: ArchivosAdjuntos) =
        archivosDao.update(item)

}