plugins {
    alias(libs.plugins.agp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.primarydetailcompose"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    // Need to include this for lambda
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        resValues = true
    }

    namespace = "com.example.primarydetailcompose"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test> {
    jvmArgs("-noverify")
}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
        allWarningsAsErrors = true

        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
        )
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.kotlin.stdlib)

    // Feature Bundles
    implementation(libs.bundles.compose)
    implementation(libs.bundles.hilt)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)

    // Individual Architecture/UI Libs
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlin.serialization.json)
    runtimeOnly(libs.coroutines.android)

    // Annotation Processors
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)

    // Unit Testing
    testImplementation(libs.bundles.test.unit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ext.junit)
    testImplementation(libs.compose.ui.test.junit4)
    kspTest(libs.hilt.compiler)

    // Instrumentation Testing
    androidTestImplementation(libs.bundles.test.android)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
