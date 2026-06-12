# Phase 3 — Plan

1. Invoke the `writing-plans` skill. The plan must be written and printed to
   the screen in the main process (not in a sub-agent) so the user can review
   it. **The plan file must be saved to the session workspace**
   (`~/.copilot/session-state/<session-id>/files/plan.md`), not to the
   repository. Plans are session artifacts and must not be committed.

2. **PR splitting is mandatory for non-trivial tasks.** The plan must define
   an explicit, numbered list of PRs. Each PR entry must specify:
    - A short title
    - The exact scope: which tasks, files, or layers of change are included
    - What is explicitly **excluded** (deferred to a later PR)

3. **The plan must include a verification strategy.** See the verification
   strategy requirements below.

4. **TODO comment handling is mandatory.** Before writing the plan, search
   the codebase for TODO comments that reference the ticket ID:

   ```bash
   grep -rn "TODO.*PDJB-XXX" src/
   ```
   ```powershell
   Select-String -Path src\** -Pattern "TODO.*PDJB-XXX" -Recurse
   ```

   Or using the `making-code-edits` skill (JetBrains MCP):
   ```
   jetbrains-search_in_files_by_regex:
     regexPattern: "TODO.*PDJB-XXX"
     directoryToSearch: "src"
   ```

   For each TODO found, the plan must include a note describing:
    - Which PR and task addresses it
    - How it will be resolved
    - If intentionally not addressed: why (out of scope, deferred, not applicable)

   Present these in the plan's TODO table alongside Jira acceptance criteria.

5. **Feature flag assessment is mandatory for non-bug-fix changes.** If the change
   alters user-visible behaviour (new pages, changed navigation, modified content,
   new form fields, etc.), it must be assessed for feature flagging:

   a. **Identify the need:** Does this change alter behaviour that the user sees?
      If yes, it likely needs a feature flag. Confirm with the user.
   b. **Check existing flags:** Review `FeatureFlagNames.kt` and `application.yml`
      to determine if an existing feature flag is suitable for this change.
      Present options to the user.
   c. **New flag if needed:** If no existing flag is suitable, propose a new flag
      name following the project's naming convention. Confirm with the user.
   d. **Release association:** Check which release the flag should be added to by
      reviewing `FeatureFlagReleaseNames.kt`. Ask the user whether to use an
      existing release or create a new one.

   Bug fixes do NOT require feature flags (they restore correct behaviour).

6. After the plan is written, present the PR breakdown and verification
   strategy to the user for confirmation. Do not proceed until the user agrees.

7. If the plan defines more than one PR, ask the user:
   *"Should the PRs be raised as a stack (all raised at once, each targeting the
   previous branch — faster but harder to manage if early PRs change significantly)
   or sequentially (one at a time, waiting for each to merge before raising the
   next — slower but simpler to manage)?"*

8. Record the PR strategy and number of PRs for use in the per-PR cycle.

## Verification Strategy Requirements

The verification strategy must cover:

- **TDD suitability** — assess whether TDD is appropriate. If so, specify which
  tests to write before implementation.

- **Test plan by type** — consider each type and state whether applicable or not:
  1. Unit tests (services, helpers, models, validation)
  2. Controller tests
  3. Journey step configuration tests
  4. Integration tests — journey tests
  5. Integration tests — single page tests
  6. Integration tests — standalone page and component tests
  7. Frontend JavaScript tests

- **Full test suite** — whether needed (takes ~20 minutes; prefer targeted unless
  cross-cutting)

- **Local smoke test** — unless the change has no observable runtime effect

- **Figma comparison** — if changes affect UI or content

- **Bug-specific verification** — if a bug fix: reproduce first, verify fix after,
  add regression test

**Override:** When the `writing-plans` skill offers an execution handoff choice,
skip it. This orchestrator manages execution directly.
