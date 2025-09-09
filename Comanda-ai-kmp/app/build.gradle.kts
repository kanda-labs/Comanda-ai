@file:OptIn(ExperimentalComposeLibrary::class)

import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kover)
}

sqldelight {
    databases {
        create("ComandaAiDatabase") {
            packageName.set("co.kandalabs.comandaai.sqldelight.db")
        }
    }
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

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            linkerOpts("-lsqlite3")
            baseName = "ComposeApp"
            isStatic = true
        }
    }


    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(projects.network)
            implementation(projects.auth)
            implementation(projects.designsystem)
            implementation(projects.core)
            implementation(projects.domain)
            implementation(projects.kitchen)
            implementation(projects.features.attendance)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.kodein)
            implementation(libs.voyager.screenmodel)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kodein.di)
            implementation(libs.kodein.compose)
            implementation(libs.ktorfit.library)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlin.reflect)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.coroutines.extensions)
            implementation(libs.runtime)
            implementation(libs.date.time)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-annotations-common"))
            implementation(libs.assertk)
            implementation(libs.turbine)
            implementation(compose.uiTest)
            implementation(compose.desktop.uiTestJUnit4)

        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqlite.driver)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        iosMain.dependencies {
            api(libs.ktor.client.darwin)
            implementation(libs.native.driver)
        }

    }

}


android {
    namespace = "co.kandalabs.comandaai"
    compileSdk = 35
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        applicationId = "co.kandalabs.comandaai"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        create("sandbox") {
            initWith(getByName("debug"))
            isDebuggable = true
            applicationIdSuffix = ".sandbox"
            versionNameSuffix = "-sandbox"
            matchingFallbacks += listOf("debug", "release")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            all {
                it.exclude("**/composable/**")
            }
        }
    }

    dependencies {
        implementation(libs.voyager.navigator)
        implementation(libs.voyager.screenmodel)
        implementation(libs.voyager.transitions)
        implementation(libs.voyager.kodein)
        debugImplementation(compose.uiTooling)
        implementation(libs.ktor.client.okhttp)
        implementation(libs.ktor.client.android)
        implementation(libs.android.driver)
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.test.manifest)
    }
}

compose.desktop {
    application {
        mainClass = "co.kandalabs.comandaai.MainKt"
        
        // Detecta o build variant atual
        val currentVariant = System.getProperty("buildVariant") ?: "debug"
        
        val variantSuffix = when(currentVariant) {
            "debug" -> "-debug"
            "sandbox" -> "-sandbox"
            "release" -> ""
            else -> "-debug"
        }
        
        val displayName = when(currentVariant) {
            "debug" -> "ComandaAI Debug"
            "sandbox" -> "ComandaAI Sandbox"
            "release" -> "ComandaAI"
            else -> "ComandaAI Debug"
        }
        
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "ComandaAi$variantSuffix"
            packageVersion = "1.0.0"
            description = displayName
            vendor = "Kanda Labs"
            
            macOS {
                val bundleSuffix = when(currentVariant) {
                    "debug" -> ".debug"
                    "sandbox" -> ".sandbox"
                    "release" -> ""
                    else -> ".debug"
                }
                bundleID = "co.kandalabs.comandaai.desktop$bundleSuffix"
                appCategory = "public.app-category.productivity"
            }
            
            windows {
                dirChooser = true
                perUserInstall = true
            }
            
            linux {
                packageName = "comandaai$variantSuffix"
                debMaintainer = "contato@kandalabs.co"
            }
        }
        
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
        
        jvmArgs("-DbuildVariant=$currentVariant")
    }
}

// Tarefas customizadas para cada ambiente
tasks.register("createDistributableDebug") {
    group = "distribution"
    description = "Cria distribuição desktop para ambiente DEBUG (porta 8082)"
    
    doFirst {
        System.setProperty("buildVariant", "debug")
    }
    
    finalizedBy("createDistributable")
}

tasks.register("createDistributableSandbox") {
    group = "distribution"
    description = "Cria distribuição desktop para ambiente SANDBOX (porta 8081)"
    
    doFirst {
        System.setProperty("buildVariant", "sandbox")
    }
    
    finalizedBy("createDistributable")
}

tasks.register("createDistributableRelease") {
    group = "distribution"
    description = "Cria distribuição desktop para ambiente RELEASE/PRODUCTION (porta 8081)"
    
    doFirst {
        System.setProperty("buildVariant", "release")
    }
    
    finalizedBy("createDistributable")
}

tasks.register("createDistributableAll") {
    group = "distribution"
    description = "Cria todas as distribuições desktop (debug, sandbox, release)"
    dependsOn("createDistributableDebug", "createDistributableSandbox", "createDistributableRelease")
}
