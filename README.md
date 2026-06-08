# Personal Registry

**Personal Registry** es una aplicación de Android moderna y versátil diseñada para el seguimiento de cualquier métrica personal. Permite a los usuarios crear registros personalizados, visualizar su progreso mediante gráficos y adaptar la experiencia a sus necesidades específicas.

## 🚀 Características

- **Registros Personalizados:** Crea diferentes tipos de registros (peso, ahorros, pasos, etc.) con nombres, iconos o emojis personalizados.
- **Gestión de Unidades:** Configura hasta dos unidades de medida por registro (ej. kg y lb, o USD y ARS) para un seguimiento flexible.
- **Visualización de Datos:** Gráficos dinámicos integrados para observar tendencias y progreso a lo largo del tiempo.
- **Gestión de Historial:** Consulta, añade, edita o elimina entradas en tus registros con una interfaz intuitiva.
- **Configuración Personalizada:**
  - Recordatorios de notificaciones personalizables para asegurar que no olvides tus registros.
  - Soporte completo para temas Claro, Oscuro y Adaptativo (basado en el sistema).
- **Interfaz Moderna:** Desarrollada íntegramente con Jetpack Compose, aprovechando las últimas capacidades de Material Design 3.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Inyección de Dependencias:** Hilt
- **Persistencia de Datos:** 
  - Room Database (para el almacenamiento de registros y definiciones).
  - DataStore Preferences (para la persistencia de ajustes de usuario).
- **Gráficos:** Compose Charts.
- **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
- **Tareas en Segundo Plano:** WorkManager (para notificaciones programadas).

## 📦 Requisitos e Instalación

1. **Requisitos:** Dispositivo con Android 13 (API 33) o superior.
2. **Instalación:**
   - Clona este repositorio: `git clone https://github.com/lucaspujia/personal-registry.git`
   - Abre el proyecto en **Android Studio (Ladybug o superior)**.
   - Sincroniza el proyecto con los archivos Gradle.
   - Compila y ejecuta en un emulador o dispositivo físico.

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---
Desarrollado por [Lucas Pujia](https://github.com/lucaspujia).
