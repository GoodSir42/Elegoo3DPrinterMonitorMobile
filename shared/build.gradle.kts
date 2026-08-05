import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "eu.kutscheid.elegoomonitor.shared"
    compileSdk = 37

    defaultConfig {
        // Wear OS 3 is API 30, matching the phone app's floor.
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // `api` so consumers compiling their own @Serializable types against PrinterStatus
    // (e.g. the phone widget's WidgetPrinter) see the serialization runtime.
    api(libs.kotlinx.serialization.json)
}
