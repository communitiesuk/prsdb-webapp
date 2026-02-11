# ADR-0039: Performance test frameworks

## Status

Draft

Date of decision: {yyyy-MM-dd}

## Context and Problem Statement

There are several open-source performance testing frameworks available, each with their own strengths and weaknesses. We need to choose one
that fits well with our existing technology stack and development practices.

## Considered Options

* Gatling (Kotlin DSL)
* k6 (JavaScript)
* Artillery (YAML / JavaScript)

Options which required introducing languages or runtimes not already in use within the project (e.g. JMeter using XML or Locust using
Python) were not considered further.

## Decision Outcome

Gatling, because despite its steeper learning curve and less elegant intergration with GitHub Actions, it allows for code reuse and sharing
with the main application, and is already used in other MHCLG and Government projects.

## Pros and Cons of the Options

### Gatling (Kotlin DSL)

Gatling is an open-source JVM performance testing framework, and can be used with a Kotlin-based DSL.

* Good, because it allows us to use Kotlin to define our tests.
* Good, because any more complex or reusable logic (e.g. managing authentication) can be implemented in Kotlin alongside the tests.
* Good, because we can reuse or share code between our application and performance tests, e.g. generating POST request fields from our form
  models.
* Good, because it is already used in at least one other MHCLG project (EPB) and several other Gov projects (Pay, some parts of One Login).
* Neutral, because although it can be run in a GitHub Action it is just running the gradle task from the command line.
* Bad, because the learning curve for Gatling's DSL and concepts may be steep compared to simpler tools.

### k6 (JavaScript)

k6 is an open-source performance testing framework that uses JavaScript as the scripting language.

* Good, because the simple JavaScript API is easy to learn.
* Good, because any more complex or reusable logic (e.g. managing authentication) can be implemented in JavaScript alongside the tests.
* Good, because other Gov projects (e.g. some parts of One Login) already use k6 for performance testing.
* Good, because it supports running tests from a GitHub Action out of the box.
* Bad, because there is no opportunity to easily share code with the WebApp, requiring either manually keeping the tests in-sync with
  changes to forms, or additional work on tooling to keep them aligned.

### Artillery (YAML / JavaScript)

Artillery is an open-source performance testing framework that uses a combination of YAML and JavaScript for scripting.

* Good, because the YAML interface has an extremely shallow learning curve.
* Good, because it can be run from its own GitHub action, and so doesn't require us to add Artillery as a dependency of our project.
* Neutral, because it supports deploying and running the tests to AWS ECS/Fargate out of the box, but this not our current target
  deployment environment.
* Bad, because the YAML interface is limited in flexibility for more complex or reusable logic (e.g. managing authentication), requiring
  JavaScript extensions.
* Bad, because any more complex or reusable logic (e.g. managing authentication) would need to be implemented in JavaScript alongside the
  tests.
* Bad, because there is no opportunity to easily share code with the WebApp, requiring either manually keeping the tests in-sync with
  changes to forms, or additional work on tooling to keep them aligned.

