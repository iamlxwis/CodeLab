import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keyFile = System.getenv("CODELAB_KEYSTORE_FILE")
val keyPassword = System.getenv("CODELAB_KEYSTORE_PASSWORD")
val keyAlias = System.getenv("CODELAB_KEY_ALIAS")
val keyEntryPassword = System.getenv("CODELAB_KEY_PASSWORD")
val hasReleaseSigning = !keyFile.isNullOrBlank() &&
    !keyPassword.isNullOrBlank() && !keyAlias.isNullOrBlank() &&
    !keyEntryPassword.isNullOrBlank() && File(keyFile).isFile

android {
    namespace = "site.lxwis.codelab"
    compileSdk = 35
    defaultConfig {
        applicationId = "site.lxwis.codelab"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    signingConfigs {
        if (hasReleaseSigning) {
            create("production") {
                storeFile = File(requireNotNull(keyFile))
                storePassword = keyPassword
                keyAlias = keyAlias
                keyPassword = keyEntryPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // A valid, stable release key is required for in-place updates across CI runs.
            // No secrets: keep PR/CI builds installable without claiming stable update signing.
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("production")
                else signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("org.mozilla:rhino:1.7.15.1")
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("io.github.beanshell:bsh:2.1.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
