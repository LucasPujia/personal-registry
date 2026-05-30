package com.lucaspujia.personalregistry.di

import android.content.Context
import com.lucaspujia.personalregistry.database.AppDatabase
import com.lucaspujia.personalregistry.database.weight.RoomWeightsStorage
import com.lucaspujia.personalregistry.database.weight.WeightRecordDao
import com.lucaspujia.personalregistry.database.weight.WeightsStorage
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Módulo de Hilt para proporcionar dependencias a lo largo de la aplicación.
 * Define cómo se crean y proporcionan las instancias de las clases necesarias,
 * como la base de datos, el almacenamiento de pesos, el repositorio de configuraciones
 * y el modelo de la actividad principal.
 */
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideWeightRecordDao(database: AppDatabase): WeightRecordDao {
        return database.weightRecordDao()
    }

    @Provides
    @Singleton
    fun provideWeightsStorage(dao: WeightRecordDao): WeightsStorage {
        return RoomWeightsStorage(dao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideMainActivityModel(storage: WeightsStorage): MainActivityModel {
        return MainActivityModel(storage)
    }
}
