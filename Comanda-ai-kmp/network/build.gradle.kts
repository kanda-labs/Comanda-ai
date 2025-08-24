import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Network"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // No external dependencies - completely isolated module
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-annotations-common"))
        }
    }
}

android {
    namespace = "co.kandalabs.comandaai.network"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 25
        
        // Network configuration - SINGLE PLACE TO CHANGE IP
        buildConfigField("String", "BASE_IP", "\"127.0.0.1\"")
        buildConfigField("int", "PRODUCTION_PORT", "8081")
        buildConfigField("int", "DEBUG_PORT", "8082")
    }
    
    buildTypes {
        getByName("debug") {
            // Para emulador Android - usar 10.0.2.2 (IP especial do emulador que aponta para host)
            buildConfigField("String", "BASE_IP", "\"10.0.2.2\"")
            buildConfigField("int", "PRODUCTION_PORT", "8081") 
            buildConfigField("int", "DEBUG_PORT", "8082")
        }
        create("sandbox") {
            initWith(getByName("debug"))
            // Sandbox aponta para produção (porta 8081)
            buildConfigField("String", "BASE_IP", "\"127.0.0.1\"")
            buildConfigField("int", "PRODUCTION_PORT", "8081")
            buildConfigField("int", "DEBUG_PORT", "8082")
        }
        getByName("release") {
            buildConfigField("String", "BASE_IP", "\"127.0.0.1\"")
            buildConfigField("int", "PRODUCTION_PORT", "8081")
            buildConfigField("int", "DEBUG_PORT", "8082")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}