import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.serialization") version "2.0.20"
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.flywaydb.flyway") version "10.18.0"
}

group = "uk.gov.communities.prsdb"
version = "latest"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // Migrations
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    // Auth
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Templating
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // External service clients
    implementation("uk.gov.service.notify:notifications-java-client:5.2.1-RELEASE")
    implementation("software.amazon.awssdk:s3:2.31.78")
    implementation("software.amazon.awssdk.crt:aws-crt:0.36.3")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.22.13")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.microsoft.playwright:playwright:1.47.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("com.deque.html.axe-core:playwright:4.4.1")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // PhoneNumbers
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.49")

    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // CSV Data Loading
    implementation("org.apache.commons:commons-csv:1.8")

    // Streaming upload without storing on local system
    implementation("org.apache.commons:commons-fileupload2-jakarta-servlet6:2.0.0-M5")

    // FF4J feature flags
    implementation("org.ff4j:ff4j-spring-boot-starter-webmvc:2.1")

    // Fake data generation
    implementation("net.datafaker:datafaker:2.4.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

val frontendAssetsSpec: CopySpec =
    copySpec {
        from("dist")
        include("**/*")
    }

tasks.register<Exec>("buildFrontendAssets") {
    group = "build"
    description = "Build frontend JavaScript and CSS assets using npm"
    if (OperatingSystem.current().isWindows) {
        commandLine("cmd", "/c", "npm", "run", "build")
    } else {
        commandLine("npm", "run", "build")
    }
}

tasks.register<Copy>("copyBuiltAssets") {
    dependsOn("buildFrontendAssets")
    into(layout.buildDirectory.dir("resources/main/static/assets"))
    with(frontendAssetsSpec)
    outputs.upToDateWhen { false }
}

tasks.withType<KotlinCompile> {
    dependsOn("copyBuiltAssets")
}

tasks.withType<Test> {
    useJUnitPlatform()
    dependsOn("copyBuiltAssets")
    maxHeapSize = "2g"
}

tasks.register<JavaExec>("playwright") {
    classpath(sourceSets["test"].runtimeClasspath)
    mainClass.set("com.microsoft.playwright.CLI")
}

tasks.register<Test>("testWithoutIntegration") {
    group = "verification"
    exclude("uk/gov/communities/prsdb/webapp/integration/**")
}

// Read .env file for local development configuration. The .env file is gitignored and only exists on developer
// machines. This is used solely to configure the Flyway Gradle plugin (flywayClean etc.) with the correct local
// database port — it has no effect on the Spring Boot application, deployed environments, or CI.
val envFile = file(".env")
val envVars = mutableMapOf<String, String>()
if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val (key, value) = trimmed.split("=", limit = 2)
            envVars[key.trim()] = value.trim().removeSurrounding("\"")
        }
    }
}

flyway {
    val postgresPort = envVars["POSTGRES_PORT"] ?: "5433"
    url = "jdbc:postgresql://localhost:$postgresPort/prsdblocal"
    user = "postgres"
    password = "notarealpassword"
}

tasks.register("messageFileCheck") {
    group = "verification"
    description = "Check message YAML files for escaped unicode apostrophes (\\u2019) that should be literal curly apostrophes"
    doLast {
        val messagesDir = file("src/main/resources/messages")
        val escapedApostrophe = "\\u2019"
        val violations = mutableListOf<String>()

        messagesDir.listFiles()?.filter { it.extension == "yml" }?.sorted()?.forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                if (line.contains(escapedApostrophe)) {
                    violations.add("${file.name}:${index + 1}: $line")
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Found escaped unicode apostrophes (\\u2019) in message files. " +
                    "Use the literal curly apostrophe character (\u2019) instead.\n" +
                    "Run './gradlew messageFileFormat' to auto-fix.\n\n" +
                    violations.joinToString("\n"),
            )
        }
    }
}

tasks.register("messageFileFormat") {
    group = "verification"
    description = "Replace escaped unicode apostrophes (\\u2019) with literal curly apostrophes in message YAML files"
    doLast {
        val messagesDir = file("src/main/resources/messages")
        val escapedApostrophe = "\\u2019"
        val curlyApostrophe = "\u2019"
        var totalReplacements = 0

        messagesDir.listFiles()?.filter { it.extension == "yml" }?.sorted()?.forEach { file ->
            val content = file.readText()
            if (content.contains(escapedApostrophe)) {
                val count = content.windowed(escapedApostrophe.length).count { it == escapedApostrophe }
                file.writeText(content.replace(escapedApostrophe, curlyApostrophe))
                totalReplacements += count
                println("Fixed $count occurrence(s) in ${file.name}")
            }
        }

        if (totalReplacements > 0) {
            println("\nReplaced $totalReplacements escaped apostrophe(s) with literal curly apostrophes.")
        } else {
            println("No escaped apostrophes found. All message files are clean.")
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.postgresql:postgresql:42.7.7")
        classpath("org.flywaydb:flyway-database-postgresql:10.18.0")
    }
}
