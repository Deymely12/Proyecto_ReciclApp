plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

val MAPS_API_KEY: String = project.findProperty("MAPS_API_KEY") as? String ?: ""

android {
    namespace = "com.example.reciclapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.reciclapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        resValue ("string", "google_maps_key", MAPS_API_KEY)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // La versión 11 está un poco desactualizada, 1.8 o 17 son más comunes.
        // Pero lo dejamos como lo tienes para no introducir más cambios.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ---- DEPENDENCIAS PRINCIPALES ----
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // ---- MAPAS (Versiones actualizadas para compatibilidad) ----
    implementation("com.google.maps.android:maps-compose:2.11.4") // Versión actualizada
    implementation("com.google.android.gms:play-services-maps:18.2.0")  // Versión actualizada
    implementation("com.google.android.gms:play-services-location:21.3.0") // Solo una declaración

    // ---- FIREBASE (Usando la BoM correctamente) ----
    // 1. Importa la Bill of Materials (BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // 2. Añade las dependencias que necesitas SIN especificar la versión.
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.material3) // Corregido el error de tipeo (era firxebase)


    // ---- DEPENDENCIAS DE TEST ----
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
