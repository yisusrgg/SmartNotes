package com.example.smartnotes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartnotes.data.DateTimeConverters
import com.example.smartnotes.data.dao.ArchivosDao
import com.example.smartnotes.data.dao.NotasTareasDao
import com.example.smartnotes.data.dao.RecordatoriosDao
import com.example.smartnotes.data.entities.ArchivosAdjuntos
import com.example.smartnotes.data.entities.NotasTareas
import com.example.smartnotes.data.entities.Recordatorios

@TypeConverters(DateTimeConverters::class)
@Database(entities = [NotasTareas::class, ArchivosAdjuntos::class, Recordatorios::class], version = 2, exportSchema = false)
abstract class SmartNotesDatabase: RoomDatabase() {
    abstract fun notasTareasDao(): NotasTareasDao
    abstract fun archivosDao(): ArchivosDao
    abstract fun recordatoriosDao(): RecordatoriosDao

    companion object {
        @Volatile
        private var Instance: SmartNotesDatabase? = null

        fun getDatabase(context: Context): SmartNotesDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SmartNotesDatabase::class.java, "smartnotes_database")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }

    }
}

val MIGRATION_1_2 = object : Migration(1, 2) { // De la versión 1 a la versión 2
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE recordatorios ADD COLUMN opcion INTEGER NOT NULL DEFAULT 0"
        )
    }
}