---
name: verification-before-completion
description: Use when about to claim work is complete, fixed, or passing. Requires running verification commands and confirming output before making any success claims.
allowed-tools: 'shell(.\gradlew:*) shell(./gradlew:*) shell(npm test) jetbrains'
---

# Verification Before Completion

**Core principle:** Evidence before claims, always.

If you have not run the verification command in this session, you cannot claim it
passes.

## The Gate

Before claiming any status:

1. **IDENTIFY** — What command proves this claim?
2. **RUN** — Execute the full command (fresh, not cached)
3. **READ** — Full output, check exit code, count failures
4. **VERIFY** — Does output confirm the claim?
5. **ONLY THEN** — Make the claim with evidence

## Verification Commands

| Claim | Command (PowerShell) | Command (Bash) |
|-------|---------------------|----------------|
| Unit tests pass | `.\gradlew testWithoutIntegration --console=plain` | `./gradlew testWithoutIntegration --console=plain` |
| Specific test passes | `.\gradlew test --tests "fully.qualified.Class" --console=plain` | `./gradlew test --tests "fully.qualified.Class" --console=plain` |
| All tests pass | `.\gradlew test --console=plain` | `./gradlew test --console=plain` |
| Lint clean | `.\gradlew ktlintCheck --console=plain` | `./gradlew ktlintCheck --console=plain` |
| Build succeeds | `jetbrains-build_project` with worktree `projectPath` | same |
| Frontend tests pass | `npm test` | `npm test` |
| No compilation errors | `jetbrains-get_file_problems` on modified files | same |
| Bug fixed | Reproduce → fix → verify symptom gone | same |

## Streaming Output

**CRITICAL:** Tests can take up to 20 minutes. Commands MUST stream output so
progress can be monitored.

### PowerShell

```powershell
# Use sync mode with long initial_wait
.\gradlew test --console=plain
```

Via the powershell tool:
```
powershell:
  command: ".\gradlew test --console=plain"
  mode: "sync"
  initial_wait: 300
```

If still running after `initial_wait`, use `read_powershell` every 30-60 seconds
to check progress.

### Bash

```bash
./gradlew test --console=plain
```

Via the powershell tool:
```
powershell:
  command: "./gradlew test --console=plain"
  mode: "sync"
  initial_wait: 300
```

### Progress Indicators

Look for these to confirm tests are progressing:
- `> Task :test` — tests starting
- `uk.gov.communities.prsdb.webapp.` — individual test classes
- `X tests completed, Y failed` — periodic summary
- `BUILD SUCCESSFUL` / `BUILD FAILED` — completion

If no new output for 2+ minutes, investigate (database locks, Docker health).

## What Counts as Evidence

| ✓ Evidence | ✗ Not Evidence |
|-----------|---------------|
| Command output showing 0 failures | Previous run from earlier |
| Exit code 0 from fresh run | "Should pass based on changes" |
| `BUILD SUCCESSFUL` in output | Linter passing (does not prove tests pass) |
| Test originally failing, now passing | Code changed, assumed fixed |
| `jetbrains-build_project` reporting 0 errors | File looks correct |

## Common Failures

- Claiming tests pass without running them
- Running only one test class and claiming "all tests pass"
- Assuming linter passing means build passes
- Not re-running after a fix (verifying the fix actually worked)
- Running tests from stale context (wrong branch, outdated code)

## Smoke Test Verification

For UI/runtime claims, use the `smoke-testing` skill (Playwright CLI via
`.claude/skills/playwright-cli/`). Verify pages load, forms render, and
navigation works as expected.
