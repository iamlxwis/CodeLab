import java.io.File

plugins {
    id("com.android.application")
    id("com.chaquo.python")
}

val keyFile = System.getenv("CODELAB_KEYSTORE_FILE")
val keyStorePassword = System.getenv("CODELAB_KEYSTORE_PASSWORD")
val keyAliasValue = System.getenv("CODELAB_KEY_ALIAS")
val keyEntryPassword = System.getenv("CODELAB_KEY_PASSWORD")
val hasProduction = !keyFile.isNullOrBlank() && !keyStorePassword.isNullOrBlank() &&
    !keyAliasValue.isNullOrBlank() && !keyEntryPassword.isNullOrBlank() && File(keyFile).isFile

android {
    namespace = "site.lxwis.codelab.engine.python"
    compileSdk = 35
    defaultConfig {
        applicationId = "site.lxwis.codelab.engine.python"
        minSdk = 26
        targetSdk = 35
        // The optional companion must also be upgradable without uninstalling.
        versionCode = System.getenv("CODELAB_VERSION_CODE")?.toIntOrNull()
            ?.takeIf { it >= 2 } ?: 2
        versionName = "3.13.1-alpha"
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    signingConfigs {
        if (hasProduction) {
            create("production") {
                storeFile = File(requireNotNull(keyFile))
                storePassword = keyStorePassword
                keyAlias = keyAliasValue
                keyPassword = keyEntryPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (hasProduction) signingConfigs.getByName("production")
                else signingConfigs.getByName("debug")
        }
    }
}
chaquopy { defaultConfig { version = "3.13" } }
