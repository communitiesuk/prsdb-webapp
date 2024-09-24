# ADR-0015: End-to-End Test Framework

## Status

Proposed

## Context and Problem Statement

What tool(s) are we going to use to test our web app end-to-end (i.e. where the tests drive the app through the UI)?

## Considered Options

* Selenium WebDriver
* Cypress
* Puppeteer
* Playwright

## Decision Outcome

Playwright, because it is modern, powerful, has good browser support, and is increasingly popular.

## Pros and Cons of the Options

### Selenium WebDriver

Selenium is one of the oldest frameworks for automatically driving a web app through its UI. It is really a suite of
tools: WebDriver is responsible for communicating with browsers, but other tools exist to e.g. execute multiple tests in
parallel (Selenium Grid). Selenium scripts can be written in many languages, including Kotlin.

* Good, because it is commonly used (27% of surveyed respondents)
* Good, because it has broad browser support
* Good, because tests can be written in Kotlin
* Bad, because tests are not isolated out of the box
* Bad, because no visual comparison testing is included out of the box
* Good, because it integrates with JUnit
* Bad, because tests are often flaky (i.e. suffer intermittent failures)

### Cypress

Cypress is a more modern e2e web test framework, focusing on reliable tests and a visual environment to watch and debug
your tests. Cypress tests are written in Javascript.

* Neutral, because it is somewhat commonly used (15% of surveyed respondents)
* Bad, because it does not have strong browser support for Webkit browsers
* Bad, because tests must be written in Javascript
* Good, because tests are isolated
* Bad, because no visual comparison testing is included out of the box
* Neutral, because it uses its own bundled test runner (Mocha)
* Good, because it provides auto-waiting and retries to try and combat flaky tests
* Good, because it provides powerful debug tools

### Puppeteer

Puppeteer is a JavaScript library for controlling browsers, released in 2018.

* Bad, because it is relatively rarely used (3% of surveyed respondents)
* Bad, because it does not have strong support for Firefox and no support for Webkit
* Good, because tests are isolated
* Bad, because no visual comparison testing is included out of the box
* Neutral, because it requires an external JavaScript test runner (e.g. Jest or Mocha)
* Neutral, because auto-waiting and retry logic is possible but not simple
* Neutral, because some debugging tools are available but not as many as other options

### Playwright

Playwright is a relative newcomer, released by Microsoft in 2020, but is fast gaining adoption.

* Neutral, because it is somewhat commonly used (8% of surveyed respondents) and increasing
* Good, because it has broad browser support
* Good, because tests can be written in Kotlin
* Good, because tests are isolated
* Good, because visual comparison testing is included out of the box
* Good, because it can be integrated with JUnit
* Good, because it provides auto-waiting and retries to try and combat flaky tests
* Good, because it provides powerful debug tools

## More Information

* Article comparing the four
  options: https://betterstack.com/community/comparisons/playwright-cypress-puppeteer-selenium-comparison/
* JetBrains Dev Ecosystem survey 2023, automated testing
  frameworks: https://www.jetbrains.com/lp/devecosystem-2023/testing/#auto_tests_frameworks_two_years  