import org.gradle.api.internal.FeaturePreviews

rootProject.name = "ComandaAi"
enableFeaturePreview(FeaturePreviews.Feature.TYPESAFE_PROJECT_ACCESSORS.toString())

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
include(":core:sdk")
include(":core:network")
include(":core:auth")
include(":designsystem")
include(":features:domain")
include(":features:kitchen")
include(":features:attendance")