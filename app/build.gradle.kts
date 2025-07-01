plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myksu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myksu"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Используем встроенную debug-конфигурацию
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.11.0")

    // Карты и местоположение
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Для работы с Directions API
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("org.slf4j:slf4j-simple:1.7.25")

    // Для отрисовки маршрута
    implementation("com.google.maps.android:android-maps-utils:3.8.0")

    // Для HTTP-запросов
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.10.1")
}