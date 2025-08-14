rootProject.name = "Comanda-ai"

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

// Include sub-projects as composite builds
includeBuild("CommanderAPI") {
    name = "commander-api"
}
includeBuild("Comanda-ai-kmp") {
    name = "mobile-app"
}