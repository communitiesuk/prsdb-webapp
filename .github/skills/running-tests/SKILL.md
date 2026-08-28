---
name: running-tests
description: Use when choosing or running automated tests in prsdb-webapp, including targeted Gradle tests, integration tests, frontend tests, or full-suite verification.
allowed-tools: 'jetbrains'
---

# Running tests

1. Use `get_run_configurations` with the test file path to find its test method or
   class run point.
2. Use `execute_run_configuration` with that `filePath` and `line`, setting
   `waitForExit: true` and a suitable timeout.
3. Check `exitCode`. IntelliJ Gradle tests can return `output: ""` even when the
   IDE has captured the complete failure; empty MCP output is not evidence that
   no diagnostic exists.
4. For a failed run, read the matching result file under
   `build/test-results/test/TEST-<fully-qualified-test-class>.xml`. Extract the
   `<failure message>` and its text content, which contains the stack trace.
5. If there is no file present, this may suggest a build fail. Use running builds
   skill to diagnose.
