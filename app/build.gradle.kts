import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
// Chave do Google Maps: NUNCA no código. Vem da variável de ambiente MAPS_API_KEY (ex.: GitHub Secret)
// ou de local.properties (MAPS_API_KEY=...), que está no .gitignore.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val mapsKey: String = System.getenv("MAPS_API_KEY")?.takeIf { it.isNotBlank() } ?: localProps.getProperty("MAPS_API_KEY") ?: ""
// URL do backend (Spring Boot). Emulador: http://10.0.2.2:8080/ | celular real: http://IP-DO-PC:8080/
val apiBaseUrl: String = System.getenv("API_BASE_URL")?.takeIf { it.isNotBlank() }
    ?: localProps.getProperty("API_BASE_URL") ?: "http://10.0.2.2:8080/"

android {
    namespace = "com.wesee.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.wesee.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
