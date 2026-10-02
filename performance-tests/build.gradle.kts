import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.25"
    id("io.gatling.gradle") version "3.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.gatling:gatling-http-java:3.16.0")
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testRuntimeOnly("io.gatling:gatling-app:3.16.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("performance.test.classpath", sourceSets.test.get().runtimeClasspath.asPath)
}

gatling {
    includeTestOutput = false
    systemProperties =
        System
            .getProperties()
            .stringPropertyNames()
            .filter { it.startsWith("gatling.") }
            .associateWith { System.getProperty(it) }
}

// No dependency on project(":")  — the  simulations don't reuse any webapp classes. If we want to use these in the future
// spring will need updating as the current managed Netty version is 4.1.x which conflicts with the Netty 4.2.x that Gatling
// 3.16.0 requires at runtime. If a future simulation genuinely needs to reuse webapp types,
// extract a small shared module (e.g. :api-model) that both the webapp and performance-tests depend on or upgrade Spring Boot.

kotlin {
    compilerOptions {
        // Gatling's Kotlin DSL support requires the module's JVM target to match the toolchain explicitly,
        // otherwise Kotlin/JDK version mismatches cause compilation failures.
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}
