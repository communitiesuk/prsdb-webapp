# Phase 5 — Verify

## Process

1. Re-read the verification strategy from the plan.
2. Note any adaptations needed based on what was actually implemented.
3. Present the verification plan to the user for confirmation.
4. Execute the approved plan (see Delegated Verification below).
5. For bug fixes: verify the bug is fixed locally.
6. If verification fails, return to Phase 4 to fix, then re-verify.

## Delegated Verification

Dispatch a single sub-agent (general-purpose) with:
- The verification strategy from the plan
- The working directory (worktree path or current workspace) as `projectPath`
- Instructions to run each step and return a structured report

The sub-agent should use these commands:

| Verification | PowerShell | Bash |
|-------------|------------|------|
| Unit + controller tests | `.\gradlew testWithoutIntegration --console=plain` | `./gradlew testWithoutIntegration --console=plain` |
| Targeted test class | `.\gradlew test --tests "fully.qualified.TestClass" --console=plain` | `./gradlew test --tests "fully.qualified.TestClass" --console=plain` |
| Full suite | `.\gradlew test --console=plain` | `./gradlew test --console=plain` |
| Linting | `.\gradlew ktlintCheck --console=plain` | `./gradlew ktlintCheck --console=plain` |
| Frontend JS tests | `npm test` | `npm test` |
| Build check | `jetbrains-build_project` with projectPath | `jetbrains-build_project` with projectPath |
| Smoke test | Use `smoke-testing` skill (Playwright CLI) | Use `smoke-testing` skill (Playwright CLI) |

The sub-agent returns a structured report:
```json
{
  "unitTests": "pass|fail|skipped",
  "controllerTests": "pass|fail|skipped",
  "integrationTests": "pass|fail|skipped",
  "failures": [{"test": "TestClass.method", "error": "message"}],
  "smokeTest": "pass|fail|skipped",
  "linting": "pass|fail",
  "lintErrors": []
}
```

The orchestrator acts only on this report.

## Mid-Phase Checkpoints

After each verification step completes, update the checkpoint with the result and
what remains.

## Streaming Output

**CRITICAL:** Tests can take up to 20 minutes. Sub-agents and subshells MUST stream
output rather than waiting silently for completion. Progress checks must verify that
tests are actually progressing, not just wait for them to finish.

### PowerShell (Windows)

```powershell
# Run tests with streaming output — use sync mode with long initial_wait
# The --console=plain flag prevents Gradle's rich console from suppressing output
.\gradlew test --console=plain
```

When running via the powershell tool:
```
powershell:
  command: ".\gradlew test --console=plain"
  mode: "sync"
  initial_wait: 300
```
If the command is still running after `initial_wait`, it continues in the background.
Use `read_powershell` periodically (every 30-60 seconds) to check progress.

### Bash (Linux/macOS)

```bash
# Run with unbuffered streaming output
./gradlew test --console=plain
```

When running via the powershell tool:
```
powershell:
  command: "./gradlew test --console=plain"
  mode: "sync"
  initial_wait: 300
```

### Progress Indicators

Look for these in the streaming output to confirm progress:
- `> Task :test` — tests are starting
- `uk.gov.communities.prsdb.webapp.` — individual test classes running
- `X tests completed, Y failed` — periodic Gradle summary
- `BUILD SUCCESSFUL` / `BUILD FAILED` — completion

If the output stalls (no new lines for 2+ minutes), the tests may be stuck.
Check for database lock issues or Docker container health.

## Parallelising Work

When the full test suite is running (~20 minutes), consider parallel work:
- Launch code review (Phase 6) while tests run
- Draft PR description
- Any other non-conflicting task
