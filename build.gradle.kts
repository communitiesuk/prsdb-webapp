import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.serialization") version "2.0.20"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.flywaydb.flyway") version "10.18.0"
}

group = "uk.gov.communities.prsdb"
version = "latest"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
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
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.4.10")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.10")
    runtimeOnly("org.postgresql:postgresql:42.7.7")

    // Migrations
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.4.10")
    implementation("org.springframework.session:spring-session-data-redis:4.0.0-M2")

    // Auth
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client:3.4.10")

    // Templating
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:3.4.10")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // External service clients
    implementation("uk.gov.service.notify:notifications-java-client:5.2.1-RELEASE")
    implementation("software.amazon.awssdk:s3:2.31.78")
    implementation("software.amazon.awssdk.crt:aws-crt:0.36.3")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.22.13")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools:3.4.10")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.10")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:3.4.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.security:spring-security-test:6.4.11")
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
    implementation("org.apache.commons:commons-fileupload2-jakarta:2.0.0-M1")

    // FF4J feature flags
    implementation("org.ff4j:ff4j-spring-boot-starter-webmvc:2.0.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val frontendAssetsSpec: CopySpec =
    copySpec {
        from("dist")
        include("**/*")
    }

tasks.register<Copy>("copyBuiltAssets") {
    into(layout.buildDirectory.dir("resources/main/static/assets"))
    with(frontendAssetsSpec)
    outputs.upToDateWhen { false }
}

tasks.withType<KotlinCompile> {
    dependsOn("copyBuiltAssets")
}

tasks.register<JavaExec>("playwright") {
    classpath(sourceSets["test"].runtimeClasspath)
    mainClass.set("com.microsoft.playwright.CLI")
}

tasks.register<Test>("testWithoutIntegration") {
    group = "verification"
    exclude("uk/gov/communities/prsdb/webapp/integration/**")
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
