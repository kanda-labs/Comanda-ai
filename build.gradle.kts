plugins {
    // Apply base plugin for root project
    id("base")
}

group = "com.kandalabs"
version = "1.0.0"

// Configure sub-projects
subprojects {
    group = rootProject.group
    version = rootProject.version
}

// Root project tasks
tasks.register("cleanAll") {
    description = "Clean all sub-projects"
    group = "build"
    
    doLast {
        exec {
            workingDir("CommanderAPI")
            commandLine("./gradlew", "clean")
        }
        exec {
            workingDir("Comanda-ai-kmp")
            commandLine("./gradlew", "clean")
        }
    }
}

tasks.register("buildAll") {
    description = "Build all sub-projects"
    group = "build"
    
    doLast {
        exec {
            workingDir("CommanderAPI")
            commandLine("./gradlew", "build")
        }
        exec {
            workingDir("Comanda-ai-kmp")
            commandLine("./gradlew", "build")
        }
    }
}

tasks.register("testAll") {
    description = "Test all sub-projects"
    group = "verification"
    
    doLast {
        exec {
            workingDir("CommanderAPI")
            commandLine("./gradlew", "test")
        }
        exec {
            workingDir("Comanda-ai-kmp")
            commandLine("./gradlew", "test")
        }
    }
}

tasks.register("buildInstallStartApp") {
    description = "Build, install and start Android app"
    group = "mobile"
    
    doLast {
        exec {
            workingDir("Comanda-ai-kmp")
            commandLine("./gradlew", ":app:assembleDebug")
        }
        exec {
            workingDir("Comanda-ai-kmp")
            commandLine("./gradlew", ":app:installDebug")
        }
        exec {
            commandLine("adb", "shell", "am", "start", "-n", "co.touchlab.dogify/co.touchlab.dogify.MainActivity")
        }
    }
}