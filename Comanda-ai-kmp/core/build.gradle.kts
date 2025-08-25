plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.io.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation(project(":network"))
        }
        
        androidMain.dependencies {
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-annotations-common"))
            implementation(libs.assertk)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "co.kandalabs.comandaai.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 25
        targetSdk = 35
    }

    buildTypes {
        create("sandbox") {
            initWith(getByName("debug"))
        }
    }
}
