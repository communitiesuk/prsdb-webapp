---
name: running-lint
description: Use when formatting or linting prsdb-webapp Kotlin code with ktlint through IntelliJ MCP.
allowed-tools: 'jetbrains'
---

# Running lint

1. Use `get_run_configurations` to find the Gradle configuration ending in
   `[ktlintFormat]`.
2. Use `execute_run_configuration` with its name, `waitForExit: true`, and a
   suitable timeout.
3. Check `exitCode`. A non-zero exit means at least one violation could not be
   auto-corrected; files may still have been partially formatted.
