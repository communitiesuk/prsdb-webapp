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
    kotlin("jvm") version "2.3.21"
    id("io.gatling.gradle") version "3.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    // The ktlint configuration pulls logback 1.3.5; unlike the root project, Spring Boot's managed version does not apply here.
    // CVE-2023-6378, CVE-2024-12798, CVE-2024-12801, CVE-2025-11226, CVE-2026-1225, CVE-2026-9828, CVE-2026-10532:
    // logback-core / logback-classic advisories (fixed in 1.5.34; 1.5.38 matches the root project's managed version).
    constraints {
        "ktlint"("ch.qos.logback:logback-classic:1.5.38")
        "ktlint"("ch.qos.logback:logback-core:1.5.38")
    }
}

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
