plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "3.0.2"
    kotlin("plugin.serialization") version "2.0.0"
    java // Explicitly apply the Java plugin
}

import java.util.Properties
import java.io.FileInputStream

group = "kandalabs.commander"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://repo.jetbrains.space/public/p/ktor/eap") // For Ktor EAP if needed
}

// Define versions
val ktorVersion = "3.0.2"
val koinVersion = "3.5.3"
val logbackVersion = "1.4.14"
val exposedVersion = "0.46.0"

// Force consistent test framework versions
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-test-junit5:2.0.0")
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-test-junit") {
                useTarget("org.jetbrains.kotlin:kotlin-test-junit5:2.0.0")
                because("Force JUnit 5 for consistency")
            }
        }
    }
}

dependencies {
    // Ktor Server Core
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    
    // Additional Ktor Features for Security and Monitoring
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics:$ktorVersion")
    implementation("io.ktor:ktor-server-request-validation:$ktorVersion")
    implementation("io.ktor:ktor-server-sse:$ktorVersion")
    
    // OpenAPI/Swagger Support
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-openapi:$ktorVersion")
    
    // Koin for Dependency Injection
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // SQLite JDBC
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Test dependencies - using JUnit 5 consistently
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.0")
    testImplementation("io.insert-koin:koin-test:$koinVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test")
    }
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test")
    }
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17) // Use JVM 17 for better compatibility
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("kandalabs.commander.application.ApplicationKt")
}

// Read network configuration from local.properties  
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val baseIp = localProperties.getProperty("base.ip") ?: "192.168.2.200"
val productionPort = localProperties.getProperty("production.port") ?: "8081" 
val debugPort = localProperties.getProperty("debug.port") ?: "8082"

// Se o IP for 10.0.2.2 (emulador), a API deve usar 0.0.0.0 para aceitar conexões
val apiHost = if (baseIp == "10.0.2.2") "0.0.0.0" else baseIp

// Task para rodar em modo debug
tasks.register<JavaExec>("runDebug") {
    group = "application"
    description = "Runs the application in debug mode using local.properties configuration"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("kandalabs.commander.application.ApplicationKt")
    environment("PORT", debugPort)
    environment("HOST", apiHost)
    environment("LOG_LEVEL", "DEBUG")
    environment("ENVIRONMENT", "debug")
}

// Task para rodar em modo production
tasks.register<JavaExec>("runProduction") {
    group = "application"
    description = "Runs the application in production mode using local.properties configuration"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("kandalabs.commander.application.ApplicationKt")
    environment("PORT", productionPort)
    environment("HOST", apiHost)
    environment("LOG_LEVEL", "INFO")
    environment("ENVIRONMENT", "production")
}

// Fat JAR configuration using standard Gradle jar task
tasks.jar {
    archiveFileName.set("CommanderAPI.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "kandalabs.commander.application.ApplicationKt",
                "Implementation-Title" to "CommanderAPI",
                "Implementation-Version" to version.toString(),
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version")
            )
        )
    }
    
    // Include all dependencies in the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // Exclude signature files to avoid conflicts
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

// Create a fat JAR task
val fatJarTask = tasks.register<Jar>("fatJar") {
    archiveFileName.set("CommanderAPI-fat.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "kandalabs.commander.application.ApplicationKt",
                "Implementation-Title" to "CommanderAPI",
                "Implementation-Version" to version.toString(),
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version")
            )
        )
    }
    
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

// jpackage configuration
val jpackageTask = tasks.register<Exec>("jpackage") {
    dependsOn(fatJarTask)
    
    group = "distribution"
    description = "Create native application installer using jpackage"
    
    val jarFile = fatJarTask.get().archiveFile.get().asFile
    val appName = "CommanderAPI"
    val appVersion = version.toString().replace("-SNAPSHOT", "")
    val outputDir = file("build/jpackage")
    
    doFirst {
        // Ensure output directory exists
        outputDir.mkdirs()
        
        // Clean previous builds
        file("$outputDir/$appName").deleteRecursively()
    }
    
    val javaHome = System.getProperty("java.home")
    executable = "$javaHome/bin/jpackage"
    
    args(
        "--input", jarFile.parent,
        "--main-jar", jarFile.name,
        "--main-class", "kandalabs.commander.application.ApplicationKt",
        "--name", appName,
        "--app-version", appVersion,
        "--description", "CommanderAPI Restaurant Order Management System",
        "--vendor", "KandaLabs",
        "--dest", outputDir.absolutePath,
        "--java-options", "-Xmx512m"
    )
    
    // Add Windows-specific options
    if (org.gradle.internal.os.OperatingSystem.current().isWindows()) {
        args(
            "--win-console",
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut"
        )
    }
    
    // Add macOS-specific options
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX()) {
        args(
            "--mac-package-name", appName,
            "--mac-package-identifier", "co.kandalabs.commander.api"
        )
    }
    
    // Add Linux-specific options
    if (org.gradle.internal.os.OperatingSystem.current().isLinux()) {
        args(
            "--linux-package-name", appName.lowercase(),
            "--linux-app-category", "Office",
            "--linux-shortcut"
        )
    }
}

// Build all distribution formats
tasks.register("buildDistribution") {
    group = "distribution"
    description = "Build all distribution formats (JAR + Native)"
    
    dependsOn(fatJarTask, jpackageTask)
    
    doLast {
        val jarFile = fatJarTask.get().archiveFile.get().asFile
        val jpackageDir = file("build/jpackage")
        
        println("Distribution build completed!")
        println("=============================================")
        println("JAR File: ${jarFile.absolutePath}")
        println("JAR Size: ${jarFile.length() / (1024 * 1024)} MB")
        println("Native Installer: ${jpackageDir.absolutePath}")
        println("=============================================")
        println("Usage:")
        println("  JAR: java -jar ${jarFile.name}")
        println("  Native: Install and run from ${jpackageDir.name}/")
    }
}