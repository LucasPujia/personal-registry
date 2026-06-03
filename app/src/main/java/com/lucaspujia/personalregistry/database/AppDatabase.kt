package com.lucaspujia.personalregistry.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lucaspujia.personalregistry.database.registry.Record
import com.lucaspujia.personalregistry.database.registry.RecordDao
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.database.registry.RegistryConverters
import com.lucaspujia.personalregistry.database.registry.RegistryDao

@Database(
    entities = [Registry::class, Record::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(RegistryConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun registryDao(): RegistryDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "records_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { instance = it }
            }
        }
    }
}

