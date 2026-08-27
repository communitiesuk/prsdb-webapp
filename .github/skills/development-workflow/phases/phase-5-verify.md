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

The sub-agent should use the `running-tests` skill for all test execution (unit,
controller, integration, targeted, and frontend tests). For other verification:

| Verification | Command |
|-------------|---------|
| Tests (all types) | Use the `running-tests` skill |
| Linting | Use the `running-lint` skill |
| Build check | Use the `running-builds` skill |
| Smoke test | Use the `smoke-testing` skill |

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

## Long-Running Verification

**CRITICAL:** Tests can take up to 20 minutes. Use the `running-tests` skill for
execution via IntelliJ, but note that MCP does not stream test output.

When running the full suite via the powershell tool instead (e.g. for streaming
progress), use `--console=plain` and `mode: "sync"` with `initial_wait: 300`.
If still running after `initial_wait`, use `read_powershell` every 30–60 seconds.

### PowerShell

```powershell
.\gradlew test --console=plain
```

### Progress Indicators

- `> Task :test` — tests starting
- `uk.gov.communities.prsdb.webapp.` — individual test classes running
- `X tests completed, Y failed` — periodic Gradle summary
- `BUILD SUCCESSFUL` / `BUILD FAILED` — completion

If no new output for 2+ minutes, investigate (database locks, Docker health).

## Parallelising Work

When the full test suite is running (~20 minutes), consider parallel work:
- Launch code review (Phase 6) while tests run
- Draft PR description
- Any other non-conflicting task
