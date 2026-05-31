package com.lucaspujia.personalregistry

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase de aplicación principal para Personal Registry.
 * Anotada con @HiltAndroidApp para habilitar la inyección de
 * dependencias con Hilt en toda la aplicación.
 */
@HiltAndroidApp
class PersonalRegistryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
