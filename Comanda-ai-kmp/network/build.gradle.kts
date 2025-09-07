import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
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

    jvm("desktop")

    // Read local.properties for network configuration from root project
    val localProperties = Properties()
    val localPropertiesFile = rootProject.parent?.file("local.properties") ?: rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }
    
    val baseIp = localProperties.getProperty("base.ip") ?: "192.168.2.200"
    val productionPort = localProperties.getProperty("production.port") ?: "8081"
    val debugPort = localProperties.getProperty("debug.port") ?: "8082"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Network"
            isStatic = true
        }
        
        // Pass network configuration as compiler arguments for iOS
        iosTarget.compilations.getByName("main") {
            kotlinOptions.freeCompilerArgs += listOf(
                "-Xopt-in=kotlin.ExperimentalStdlibApi",
                "-Xopt-in=kotlinx.cinterop.ExperimentalForeignApi"
            )
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Ktor dependencies for HttpClient
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-annotations-common"))
        }
    }
}

// Generate NetworkConfig for iOS/Desktop from local.properties
tasks.register("generateNetworkConfig") {
    doLast {
        val localProperties = Properties()
        val localPropertiesFile = file("../../local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        
        val baseIp = localProperties.getProperty("base.ip") ?: "192.168.2.200"
        val productionPort = localProperties.getProperty("production.port") ?: "8081"
        val debugPort = localProperties.getProperty("debug.port") ?: "8082"
        
        val configContent = """
// This file is auto-generated from local.properties
// DO NOT EDIT MANUALLY - changes will be overwritten

package co.kandalabs.comandaai.network

object GeneratedNetworkConfig {
    const val BASE_IP = "$baseIp"
    const val PRODUCTION_PORT = $productionPort
    const val DEBUG_PORT = $debugPort
}
        """.trimIndent()
        
        val outputDir = file("src/commonMain/kotlin/co/kandalabs/comandaai/network/generated")
        outputDir.mkdirs()
        file("${outputDir.path}/GeneratedNetworkConfig.kt").writeText(configContent)
    }
}

// Run before compilation
tasks.whenTaskAdded {
    if (name.startsWith("compileKotlin")) {
        dependsOn("generateNetworkConfig")
    }
}

android {
    namespace = "co.kandalabs.comandaai.network"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 25
        
        // Network configuration from local.properties
        val localProperties = Properties()
        val localPropertiesFile = file("../../local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        
        val baseIp = localProperties.getProperty("base.ip") ?: "192.168.2.200"
        val productionPort = localProperties.getProperty("production.port")?.toInt() ?: 8081
        val debugPort = localProperties.getProperty("debug.port")?.toInt() ?: 8082
        
        // Default para dispositivos físicos
        buildConfigField("String", "BASE_IP", "\"$baseIp\"")
        buildConfigField("int", "PRODUCTION_PORT", "$productionPort")
        buildConfigField("int", "DEBUG_PORT", "$debugPort")
        buildConfigField("String", "BUILD_TYPE", "\"debug\"")
    }
    
    buildTypes {
        getByName("debug") {
            // Debug build uses configuration from local.properties (already set in defaultConfig)
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
        }
        create("sandbox") {
            initWith(getByName("debug"))
            // Sandbox build uses production port (8081) for testing against production API
            buildConfigField("String", "BUILD_TYPE", "\"sandbox\"")
        }
        getByName("release") {
            // Release build uses configuration from local.properties (already set in defaultConfig)
            buildConfigField("String", "BUILD_TYPE", "\"release\"")
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