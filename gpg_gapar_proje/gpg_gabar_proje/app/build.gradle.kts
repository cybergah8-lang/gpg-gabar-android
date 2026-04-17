plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

import java.util.Properties

android {
    namespace = "com.kardoxi.gpg_gabar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kardoxi.gpg_gabar"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Load signing config from keystore.properties (local only; not committed)
            val keystorePropertiesFile = rootProject.file("signing/keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties().apply {
                    keystorePropertiesFile.inputStream().use { this.load(it) }
                }
                signingConfig = signingConfigs.create("release").apply {
                    storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                    storePassword = keystoreProperties.getProperty("storePassword")
                    keyAlias = keystoreProperties.getProperty("keyAlias")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                }
            }
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // Rename APK outputs to a fixed name pattern for both debug and release
    applicationVariants.all {
        outputs.all {
            val out = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            out.outputFileName = if (buildType.name == "release") {
                "gpg_Gabar v.${versionName}.apk"
            } else {
                "gpg_Gabar-debug.apk"
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.appcompat)
    // Material Components for Android (XML Material3 themes such as Theme.Material3.DayNight.*)
    implementation(libs.google.material)
    // Coroutines for background threading in Compose
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // Bouncy Castle for OpenPGP (PGP key generation and processing)
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78.1")
    implementation("org.bouncycastle:bcpg-jdk15to18:1.78.1")
    // SQLCipher for encrypted SQLite database (new artifact coordinates)
    implementation("net.zetetic:sqlcipher-android:4.10.0@aar")
    // AndroidX SQLite (core and framework) required by SQLCipher integration
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("androidx.sqlite:sqlite-framework:2.4.0")
    // AndroidX Security for EncryptedSharedPreferences (stores DB key using Keystore)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    // Needed for R8 when libraries reference javax.annotation.* (e.g., Tink)
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Apache Commons Compress for handling TAR within GZIP (tar.gz)
    implementation("org.apache.commons:commons-compress:1.26.1")

    // Unit test dependencies
    testImplementation(libs.junit)
    // Android instrumentation test dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
