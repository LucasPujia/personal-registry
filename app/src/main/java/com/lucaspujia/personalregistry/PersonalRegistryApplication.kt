package com.lucaspujia.personalregistry

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de aplicación principal para Personal Registry.
 * Anotada con @HiltAndroidApp para habilitar la inyección de
 * dependencias con Hilt en toda la aplicación.
 */
@HiltAndroidApp
class PersonalRegistryApplication : Application()
