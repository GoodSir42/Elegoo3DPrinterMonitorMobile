import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.playUpload)
    alias(libs.plugins.kover)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "eu.kutscheid.elegoomonitor"
    compileSdk = 37

    defaultConfig {
        applicationId = "eu.kutscheid.elegoomonitor"
        minSdk = 30
        targetSdk = 36
        versionCode = 10
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "release-key"
            keyPassword = System.getenv("KEY_STORE_KEY_PASSWORD")
            storeFile = file("elegoo-app-keystore")
            storePassword = System.getenv("KEY_STORE_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
    }
    androidResources {
        // Generates res/xml/locale_config.xml from the available values-* folders and wires it
        // into the manifest, so users can pick the app language in system settings.
        generateLocaleConfig = true
    }
}

play {
    enabled = System.getenv("CI") == "true"
    // AUTO_OFFSET rather than AUTO: AUTO normalises any single-output module to
    // (highest code on Play + 1), so the phone and watch bundles would be assigned the same
    // version code. AUTO_OFFSET adds the local code to the Play maximum, keeping them distinct.
    resolutionStrategy = ResolutionStrategy.AUTO_OFFSET
    defaultToAppBundles = true
    track = "internal"
    releaseStatus = ReleaseStatus.COMPLETED
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {

    implementation(project(":shared"))
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.websockets)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kermit)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.dateTime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.composeViewmodel)
    implementation(libs.koin.workmanager)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.aboutlibraries.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.matching { it.name == "publishReleaseBundle" }.configureEach {
    mustRunAfter(":wear:publishReleaseBundle")
}