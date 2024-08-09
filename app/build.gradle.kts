plugins {
    // Alias para el plugin de aplicación de Android
    alias(libs.plugins.android.application)
    
    // Alias para el plugin de Kotlin para Android
    alias(libs.plugins.jetbrains.kotlin.android)
    
    // Plugin de Google DevTools KSP
    id("com.google.devtools.ksp")
    
    // Alias para el plugin de servicios de Google
    alias(libs.plugins.google.gms.google.services)
}

android {
    // Espacio de nombres del paquete de la aplicación
    namespace = "com.jlobatonm.snapshots"
    
    // Versión del SDK de compilación
    compileSdk = 34
    
    defaultConfig {
        // ID de la aplicación
        applicationId = "com.jlobatonm.snapshots"
        
        // Versión mínima del SDK
        minSdk = 24
        
        // Versión objetivo del SDK
        targetSdk = 34
        
        // Código de versión de la aplicación
        versionCode = 1
        
        // Nombre de versión de la aplicación
        versionName = "1.0"
        
        // Runner para pruebas de instrumentación
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildTypes {
        release {
            // Deshabilitar la minificación de código
            isMinifyEnabled = false
            
            // Archivos de reglas de ProGuard
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        // Compatibilidad con la versión de Java
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        // Versión de JVM para Kotlin
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        // Habilitar ViewBinding
        viewBinding = true
    }
}

dependencies {
    // Dependencia para Glide
    implementation(libs.glide)
    
    // Dependencia para Firebase Storage
    implementation(libs.firebase.storage)
    
    // Importar el BoM para la plataforma Firebase
    implementation(platform(libs.firebase.bom))
    
    // Dependencia para la biblioteca de autenticación de Firebase
    implementation(libs.firebase.auth.ktx)
    
    // FirebaseUI para Firebase Auth
    implementation(libs.firebaseui.firebase.ui.auth)
    
    // Dependencia para Firebase Database
    implementation(libs.firebase.database.ktx)
    
    // Procesador de anotaciones (se omite si no se desea usar bibliotecas de integración o configurar Glide)
    annotationProcessor(libs.compiler)
    
    // Procesador de Kotlin Symbol Processing (KSP)
    ksp(libs.ksp)
    
    // Dependencias de AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Dependencias para pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    // FirebaseUI para Firebase Realtime Database
    implementation(libs.firebase.ui.database)
}