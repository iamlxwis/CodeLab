import java.io.File
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("jacoco")
}

val keyFile = System.getenv("CODELAB_KEYSTORE_FILE")
val releaseStorePassword = System.getenv("CODELAB_KEYSTORE_PASSWORD")
val releaseAlias = System.getenv("CODELAB_KEY_ALIAS")
val releaseEntryPassword = System.getenv("CODELAB_KEY_PASSWORD")
val hasReleaseSigning = !keyFile.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank() && !releaseAlias.isNullOrBlank() &&
    !releaseEntryPassword.isNullOrBlank() && File(keyFile).isFile

android {
    namespace = "site.lxwis.codelab"
    compileSdk = 35
    defaultConfig {
        applicationId = "site.lxwis.codelab"
        minSdk = 26
        targetSdk = 35
        // CI releases use an increasing run number; local builds use a stable base.
        versionCode = System.getenv("CODELAB_VERSION_CODE")?.toIntOrNull()
            ?.takeIf { it >= 6 } ?: 6
        versionName = "0.6.0-alpha"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    lint {
        // Release checks must fail on real lint errors; warnings remain visible.
        abortOnError = true
        warningsAsErrors = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    signingConfigs {
        if (hasReleaseSigning) {
            create("production") {
                storeFile = File(requireNotNull(keyFile))
                // Do not name outer variables like SigningConfig's properties:
                // Kotlin DSL receiver shadowing silently assigned null here.
                storePassword = releaseStorePassword
                keyAlias = releaseAlias
                keyPassword = releaseEntryPassword
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
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("org.mozilla:rhino:1.7.15.1")
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("io.github.beanshell:bsh:2.1.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

/** Unit-test coverage evidence, not a universal coverage threshold. */
jacoco { toolVersion = "0.8.12" }

tasks.register<JacocoReport>("debugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    val excludes = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/*Test*.*")
    classDirectories.setFrom(files(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug").get().asFile) {
            exclude(excludes)
        },
        fileTree(layout.buildDirectory.dir(
            "intermediates/javac/debug/compileDebugJavaWithJavac/classes").get().asFile) {
            exclude(excludes)
        }
    ))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco").get().asFile) {
        include("testDebugUnitTest.exec")
    })
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
