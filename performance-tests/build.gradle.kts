import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    configurations.classpath {
        resolutionStrategy {
            // The Gatling plugin pulls jackson 2.22.2 onto the build classpath via gatling-enterprise-plugin-commons.
            // CVE-2026-89407, -89425, -91776, -91777: jackson-core / jackson-databind DoS advisories (fixed in 2.22.3).
            force("com.fasterxml.jackson:jackson-bom:2.22.3")
        }
    }
}

plugins {
    kotlin("jvm") version "1.9.25"
    id("io.gatling.gradle") version "3.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    // The ktlint configuration pulls logback 1.3.5; unlike the root project, Spring Boot's managed version does not apply here.
    // CVE-2023-6378, CVE-2024-12798, CVE-2024-12801, CVE-2025-11226, CVE-2026-1225, CVE-2026-9828, CVE-2026-10532:
    // logback-core / logback-classic advisories (fixed in 1.5.34; 1.5.35 matches the root project's logback.version).
    constraints {
        "ktlint"("ch.qos.logback:logback-classic:1.5.35")
        "ktlint"("ch.qos.logback:logback-core:1.5.35")
    }

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
