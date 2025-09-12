import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pilltip.pilltip"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pilltip.pilltip"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties().apply {
            load(FileInputStream(rootProject.file("local.properties")))
        }
        val kakaoKey = properties.getProperty("KAKAO_KEY") ?: ""

        buildConfigField("String", "KAKAO_KEY", "\"$kakaoKey\"")
        resValue("string", "KAKAO_KEY", kakaoKey)
        manifestPlaceholders["kakao_scheme"] = "kakao$kakaoKey"
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
        buildConfig = true
    }
}

dependencies {
    /* Splash */
    implementation ("androidx.core:core-splashscreen:1.0.1")

    /* FireBase */
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-appcheck-debug")

    /* Kakao */
    implementation("com.kakao.sdk:v2-common:2.20.6")
    implementation("com.kakao.maps.open:android:2.12.11")
    implementation("com.kakao.sdk:v2-user:2.20.6")

    //Hilt-Dagger
    implementation (libs.hilt.android)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.datastore.core.android)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.camera.view)

    ksp (libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //Room
    implementation (libs.androidx.room.runtime)
    ksp (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    // Coroutines
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.kotlinx.coroutines.play.services)

    /*Status Bar & HorizontalPager*/
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")

    //직렬화
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    /* AsyncImage */
    implementation("io.coil-kt:coil-compose:2.4.0")

    /* RatingBar */
    implementation ("com.github.a914-gowtham:compose-ratingbar:1.3.12")

    /* QR */
    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    // CameraX Core
    implementation ("androidx.camera:camera-core:1.4.2")
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    implementation ("androidx.camera:camera-view:1.3.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}