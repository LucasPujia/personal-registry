package com.lucaspujia.personalregistry.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lucaspujia.personalregistry.database.weight.WeightRecord
import com.lucaspujia.personalregistry.database.weight.WeightRecordDao

@Database(entities = [WeightRecord::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightRecordDao(): WeightRecordDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weights_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { instance = it }
            }
        }
    }
}

