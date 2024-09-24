# ADR-0014: Unit Test Framework

## Status

Accepted

Date of decision: 2024-09-20

## Context and Problem Statement

What tool(s) are we going to use to conduct automated tests of our Kotlin web app code? The types of automated tests in
scope include individual methods and classes, collaborating collections of classes, and mocked HTTP requests/responses,
but stops short of tests that exercise the “front end.”

## Considered Options

* JUnit 4
* JUnit 5
* Kotest

## Decision Outcome

JUnit 5, because it is a popular, proven solution, and one that has been adopted by other MHCLG Kotlin projects.

## Pros and Cons of the Options

### JUnit 4

JUnit is the most popular test framework for the Java language by far, and thus is also very popular for other JVM
languages including Kotlin. JUnit 4 is the most recent of the “traditional” JUnit releases.

* Good, because it is widely used, and many developers will be familiar with it.
* Good, because it is very mature with a broad ecosystem.
* Bad, because it is superseded by JUnit 5.

### JUnit 5

JUnit 5 is the most recent version of JUnit, first introduced in 2017. It is broadly similar to JUnit 4 but introduces
some differences in its API.

* Good, because it is widely used, and many developers will be familiar with it.
* Good, because it is now relatively mature, with few compatibility issues in the ecosystem.
* Good, because it is the latest version of JUnit, with all the innovations and improvements that entails.
* Good, because it is used by other MHCLG Kotlin projects.

### Kotest

Kotest is a Kotlin-first test framework and assertions library.

* Good, because it takes advantage of Kotlin to provide a more concise and expressive syntax.
* Good, because it supports property-based testing.
* Bad, because although it is increasingly popular with Kotlin projects, it is still much less well known than JUnit.
* Bad, because it is not (to our knowledge) used elsewhere within MHCLG.

## More Information

* Test dependencies on spring-boot-starter-test, using JUnit 5:
    * [eip-ero-notifications-api](https://github.com.mcas.ms/communitiesuk/eip-ero-notifications-api/blob/main/build.gradle.kts#L88)
    * [eip-ero-register-checker-api](https://github.com.mcas.ms/communitiesuk/eip-ero-register-checker-api/blob/main/build.gradle.kts#L119)
    * [eip-ero-print-api](https://github.com.mcas.ms/communitiesuk/eip-ero-print-api/blob/main/build.gradle.kts#L126)
    * [eip-ero-ems-integration-api](https://github.com.mcas.ms/communitiesuk/eip-ero-ems-integration-api/blob/main/build.gradle.kts#L98)
* [Java unit testing frameworks in the JetBrains Developer Ecosystem survey ](https://www.jetbrains.com/lp/devecosystem-2023/java/#java_unittesting)