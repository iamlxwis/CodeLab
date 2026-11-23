import java.io.File

plugins {
    id("com.android.application")
}
val releaseFile = System.getenv("CODELAB_KEYSTORE_FILE")
val releaseStorePassword = System.getenv("CODELAB_KEYSTORE_PASSWORD")
val releaseAlias = System.getenv("CODELAB_KEY_ALIAS")
val releaseEntryPassword = System.getenv("CODELAB_KEY_PASSWORD")
val signed = !releaseFile.isNullOrBlank() && !releaseStorePassword.isNullOrBlank() &&
    !releaseAlias.isNullOrBlank() && !releaseEntryPassword.isNullOrBlank() && File(releaseFile).isFile

android {
    namespace = "site.lxwis.codelab.engine.java"
    compileSdk = 35
    defaultConfig {
        applicationId = "site.lxwis.codelab.engine.java"
        minSdk = 26
        targetSdk = 35
        versionCode = System.getenv("CODELAB_VERSION_CODE")?.toIntOrNull()?.takeIf { it >= 2 } ?: 2
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    signingConfigs {
        if (signed) create("production") {
            storeFile = File(requireNotNull(releaseFile))
            storePassword = releaseStorePassword
            keyAlias = releaseAlias
            keyPassword = releaseEntryPassword
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (signed) signingConfigs.getByName("production")
                else signingConfigs.getByName("debug")
        }
    }
}
dependencies { implementation("io.github.beanshell:bsh:2.1.1") }
