package com.lucaspujia.personalregistry.di

import android.content.Context
import com.lucaspujia.personalregistry.database.AppDatabase
import com.lucaspujia.personalregistry.database.registry.RecordDao
import com.lucaspujia.personalregistry.database.registry.RecordsStorage
import com.lucaspujia.personalregistry.database.registry.RegistriesStorage
import com.lucaspujia.personalregistry.database.registry.RegistryDao
import com.lucaspujia.personalregistry.database.registry.RoomRecordsStorage
import com.lucaspujia.personalregistry.database.registry.RoomRegistriesStorage
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
    fun provideRegistryDao(database: AppDatabase): RegistryDao {
        return database.registryDao()
    }

    @Provides
    fun provideRecordDao(database: AppDatabase): RecordDao {
        return database.recordDao()
    }

    @Provides
    @Singleton
    fun provideRegistriesStorage(registryDao: RegistryDao): RegistriesStorage {
        return RoomRegistriesStorage(registryDao)
    }

    @Provides
    @Singleton
    fun provideRecordsStorage(recordDao: RecordDao): RecordsStorage {
        return RoomRecordsStorage(recordDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideMainActivityModel(
        registriesStorage: RegistriesStorage,
        recordsStorage: RecordsStorage
    ): MainActivityModel {
        return MainActivityModel(registriesStorage, recordsStorage)
    }
}
