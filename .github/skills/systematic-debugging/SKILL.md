---
name: systematic-debugging
description: Use when encountering any bug, test failure, or unexpected behavior. Requires root cause investigation before proposing fixes.
allowed-tools: 'write shell(.\gradlew:*) shell(./gradlew:*) jetbrains'
---

# Systematic Debugging

**Core principle:** Find root cause before attempting fixes. Symptom fixes are
failure.

## When to Use

Use for ANY technical issue: test failures, bugs, unexpected behaviour, build
failures, integration issues.

Use ESPECIALLY when:
- "Just one quick fix" seems obvious
- You have already tried multiple fixes
- Previous fix did not work

## The Four Phases

Complete each phase before proceeding to the next.

### Phase 1: Reproduce and Observe

1. **Reproduce the failure** with a specific command:

   PowerShell:
   ```powershell
   .\gradlew test --tests "uk.gov.communities.prsdb.webapp.FailingTest" --console=plain
   ```
   Bash:
   ```bash
   ./gradlew test --tests "uk.gov.communities.prsdb.webapp.FailingTest" --console=plain
   ```

2. **Record exact output:** error message, stack trace, exit code
3. **Identify the failing assertion:** what was expected vs actual?

### Phase 2: Gather Evidence

Use these project-specific evidence sources:

| Source | Tool | What It Shows |
|--------|------|---------------|
| Stack trace | Test output | Call chain to failure point |
| Spring logs | JetBrains console / test output | Request handling, bean wiring |
| Controller mapping | `jetbrains-search_symbol` for handler method | URL routing |
| Service logic | `jetbrains-read_file` with line ranges | Business logic flow |
| Thymeleaf rendering | Template file + model attributes | View layer issues |
| Journey step flow | Step config + StepId enum | Navigation/routing logic |
| Form validation | `@ValidatedBy` annotations + validator classes | Validation chain |
| Database state | Migration files + entity definitions | Schema/data issues |
| Integration test page objects | `src/test/.../integration/pageObjects/` | Expected page structure |
| JetBrains inspections | `jetbrains-get_file_problems` | Compile errors, type issues |

**Do NOT skip this phase.** Gather at least 3 pieces of evidence before forming
a hypothesis.

### Phase 3: Form and Test Hypothesis

1. State the hypothesis explicitly: "The failure occurs because..."
2. Identify what evidence supports it
3. Identify what evidence would disprove it
4. Test the hypothesis with a targeted check (read code, add logging, evaluate
   expression in debugger)

If hypothesis is wrong: return to Phase 2 with new evidence.

### Phase 4: Fix and Verify

1. Make the minimal fix that addresses root cause
2. Run the original failing test — must now pass
3. Run related tests to check for regressions:

   PowerShell:
   ```powershell
   .\gradlew test --tests "uk.gov.communities.prsdb.webapp.related.*" --console=plain
   ```
   Bash:
   ```bash
   ./gradlew test --tests "uk.gov.communities.prsdb.webapp.related.*" --console=plain
   ```

4. If fixing a bug: add a regression test that would have caught it

## Using the Debugger

For complex issues, use JetBrains MCP debugging tools:

1. Set breakpoint: `jetbrains-xdebug_set_breakpoint`
2. Start debug session: `jetbrains-xdebug_start_debugger_session`
3. Wait for pause: `jetbrains-xdebug_control_session` (WAIT_FOR_PAUSE)
4. Inspect variables: `jetbrains-xdebug_get_frame_values`
5. Evaluate expressions: `jetbrains-xdebug_evaluate_expression`
6. Step through: `jetbrains-xdebug_control_session` (STEP_OVER / STEP_INTO)

## Anti-Patterns

- Changing code without understanding why it failed
- Fixing the test instead of the code (unless the test was wrong)
- Adding try/catch to suppress errors
- "It works now" without understanding what changed
- Guessing and checking repeatedly
