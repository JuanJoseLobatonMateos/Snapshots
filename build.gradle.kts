// Archivo de configuración de nivel superior donde puedes agregar opciones de configuración comunes a todos los subproyectos/módulos.
plugins {
    // Alias para el plugin de aplicación de Android, no se aplica en este archivo
    alias(libs.plugins.android.application) apply false
    
    // Alias para el plugin de Kotlin para Android, no se aplica en este archivo
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    
    // Plugin de Google DevTools KSP, versión especificada, no se aplica en este archivo
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
    
    // Alias para el plugin de servicios de Google, no se aplica en este archivo
    alias(libs.plugins.google.gms.google.services) apply false
}