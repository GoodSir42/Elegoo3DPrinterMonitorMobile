import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.playUpload)
}

android {
    namespace = "eu.kutscheid.elegoomonitor.wear"
    compileSdk = 37

    defaultConfig {
        // Must match the phone app: the Wearable Data Layer only exchanges data between apps that
        // share an application id and signing key.
        applicationId = "eu.kutscheid.elegoomonitor"
        minSdk = 30
        targetSdk = 36
        // Must differ from the phone app's: Play requires unique version codes across form
        // factors, and both bundles go into the same release.
        versionCode = 5
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            keyAlias = "release-key"
            keyPassword = System.getenv("KEY_STORE_KEY_PASSWORD")
            // Same keystore as the phone app, so the pair is recognised as one app.
            storeFile = rootProject.file("app/elegoo-app-keystore")
            storePassword = System.getenv("KEY_STORE_PASSWORD")
        }
    }

    buildTypes {
        debug {
            // Mirrors the phone app's suffix so debug builds pair with each other.
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
        generateLocaleConfig = true
    }
}

/**
 * Mirrors the phone app's configuration. Because both modules share an applicationId, the plugin
 * keys its Play edit and its commit task on that id — so publishing both in a single Gradle
 * invocation puts the two bundles into one release and commits once.
 */
play {
    enabled = System.getenv("CI") == "true"
    resolutionStrategy = ResolutionStrategy.AUTO_OFFSET
    defaultToAppBundles = true
    track = "wear:internal"
    commit = true
    releaseStatus = ReleaseStatus.COMPLETED
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.watchface.complications.data.source.ktx)

    implementation(libs.play.services.wearable)
    implementation(libs.kermit)

    debugImplementation(libs.androidx.ui.tooling)
}
