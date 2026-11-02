import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val integrationProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val lookWeaveBackendUrl = integrationProperties.getProperty("LOOKWEAVE_BACKEND_URL")
    ?: System.getenv("LOOKWEAVE_BACKEND_URL")
    ?: "http://127.0.0.1:8787"

android {
    namespace = "com.vizeno.loomie"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.vizeno.loomie"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "LOOKWEAVE_BACKEND_URL", "\"${lookWeaveBackendUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Phone (arm64) only — keeps debug APK small enough for wireless adb install.
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("io.elevenlabs:elevenlabs-android:0.12.1")
    implementation("org.maplibre.gl:android-sdk:11.8.0")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.mlkit:pose-detection:18.0.0-beta5")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
