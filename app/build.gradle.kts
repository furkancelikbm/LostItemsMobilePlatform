plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.googleServices)
    kotlin("plugin.serialization") version "1.9.0"
    // Add Hilt and Kapt plugins
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.mycompose"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mycompose"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"

    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.webkit)
    implementation(libs.com.google.firebase)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    /*these my downloades*/
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.analitics)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.google.firebase.firestore.ktx)

    /*--------------------------------*/


    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)

    implementation (libs.places) //yerleri göstermesi için

    // Diğer bağımlılıklarınız burada
    implementation (libs.maps.compose)
    implementation (libs.play.services.maps.v1802)


    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.androidx.runtime.livedata) // Adjust version as needed



    /*-------------------------*/


    // Lifecycle
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.runtime.compose)

    // Logger
    implementation(libs.napier)

    // Ktor
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    //coil
    implementation (libs.coil.compose)

    // Hilt dependencies
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    //pager indicatorw
    implementation (libs.accompanist.pager)
    implementation (libs.accompanist.pager.indicators)

    implementation ("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    // TensorFlow Lite
    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite)



}

kapt {
    correctErrorTypes = true
}