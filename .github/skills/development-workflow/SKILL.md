---
name: development-workflow
description: Use when starting a new development task. Orchestrates the full lifecycle from setup through implementation, review, and PR creation.
---

# Development Workflow

Orchestrates the full development lifecycle for prsdb repositories.

The workflow has two stages:

1. **One-time setup** (Phases 0–3): run once at the start of a task.
2. **Per-PR cycle** (Phases 4–8): repeat for each PR defined in the plan.

Follow each phase in order, completing one before moving to the next.

> **IMPORTANT — Phase tracking:** At every phase transition, announce the
> phase you are entering and (when applicable) the PR you are working on.
>
> **When the PR plan is known** (typically Phases 4–8), use this exact format
> at the start of each phase:
>
> ```
> ═══ Phase N — <Phase Name> [PR M of T] ═══
> ```
>
> **Before the PR plan is confirmed** (typically Phases 0–2 and the start of
> Phase 3), use a placeholder instead:
>
> ```
> ═══ Phase N — <Phase Name> [PR TBD] ═══
> ```
>
> These announcements are mandatory. Do not skip them. They ensure both the
> user and the agent maintain awareness of the current position in the
> workflow.

---

## One-Time Setup

---

### Phase 0 — Preflight

Invoke the `preflight-checks` skill to verify all required tools are available.

Treat preflight results as follows:

- **Critical** (block if missing): `gh` CLI, IntelliJ CLI, JetBrains MCP,
  Docker, Playwright CLI.
- **Task-dependent** (block only for UI/content tasks): Figma MCP.

Do not proceed if any applicable critical tool is missing unless the user
explicitly confirms they want to continue.

---

### Phase 1 — Setup

1. Ask the user for the task description and Jira ticket ID.
   If there is no ticket, use `PDJB-NONE`.
2. Use the `branch-and-commit-naming` skill to determine the branch name for
   the first PR. Each PR in the plan will get its own branch — the first
   branch is created now; subsequent branches are created in Phase 8.
3. Use the `using-git-worktrees` skill to create a worktree for the branch.
4. Launch IntelliJ in the worktree folder using the command recorded during
   preflight (e.g. `idea64 <worktree-path>`).
5. Change the working directory to the worktree.

---

### Phase 2 — Brainstorm

1. If the task involves UI, presentational, or content changes and no Figma
   link has been provided, ask the user for the relevant Figma file or
   fragment URL. Alternatively, the user can select the relevant layer or
   screen in the Figma desktop app (the MCP server can see the current
   selection).
2. Invoke the `brainstorming` skill. Include any Figma link as context.

---

### Phase 3 — Plan

1. Invoke the `writing-plans` skill. The plan must be written and printed to
   the screen in the main process (not in a sub-agent) so the user can review
   it in the current session.
2. **PR splitting is mandatory for non-trivial tasks.** The plan must define
   an explicit, numbered list of PRs. Each PR entry must specify:
    - A short title (e.g. "PR 1 — Add database migration for X").
    - The exact scope: which tasks, files, or layers of change are included.
    - What is explicitly **excluded** (deferred to a later PR).
3. After the plan is written, present the PR breakdown to the user for
   confirmation. Do not proceed until the user agrees with the split.
4. Ask the user:
   *"Should the PRs be raised in parallel (stacked on each other) or
   sequentially (one at a time, waiting for each to be merged)?"*
5. Record the PR strategy and the number of PRs for use in the per-PR cycle.

**Override:** When the `writing-plans` skill offers an execution handoff
choice, skip it. This orchestrator manages execution directly.

---

## Per-PR Cycle

> **Repeat Phases 4–8 for each PR defined in the plan.**
>
> Work on exactly one PR at a time. Complete the full cycle (implement →
> verify → review → commit & PR) for PR 1 before starting PR 2, and so on.
> Do not begin implementing a later PR until the current PR has been
> committed and pushed.

---

### Phase 4 — Implement

> Scope guard: implement **only** the tasks assigned to the current PR in the
> plan. Do not implement tasks belonging to other PRs.

> **Worktree guard:** All file edits, tool invocations, and sub-agent prompts
> must target the worktree path, not the main repository checkout. When using
> the JetBrains MCP server, pass the worktree path as `projectPath`. When
> launching sub-agents, include the worktree path in the prompt and instruct
> them to use it as their working directory.

- Follow the plan's task breakdown for the current PR. Use the
  `subagent-driven-development` skill or implement directly as appropriate.
- Follow TDD where the plan specifies tests.
- Do **not** commit during implementation — changes are committed in Phase 7
  after review.

---

### Phase 5 — Verify

1. Analyse the changes made for the current PR and propose a verification
   plan. Consider:
    - **Specific tests** — unit, controller, and integration tests directly
      related to the changes.
    - **Full test suite** — if changes are cross-cutting or affect shared
      infrastructure. The full suite takes up to 20 minutes — factor this
      into the decision. Prefer running only the relevant tests unless the
      changes are widespread or affect shared code paths.
    - **Local smoke test** — if changes affect navigation, journey logic, or
      UI/presentation: run the application locally, then use the Playwright
      CLI to manually smoke test the affected pages and journeys.
    - **Figma comparison** — if changes affect UI or content: compare the
      implemented pages against the Figma designs to catch missed or
      incorrect content changes.
2. Present the verification plan to the user for confirmation.
3. Execute the approved plan. See **Running Tests** below for execution
   guidance.
4. If verification fails, return to Phase 4 to fix issues, then re-verify.

#### Running Tests

**Available Gradle tasks:**
- `./gradlew test` — full suite (unit + integration + journey; ~20 minutes).
- `./gradlew testWithoutIntegration` — runs a reduced suite (primarily unit
  and controller tests; excludes tests under
  `uk/gov/communities/prsdb/webapp/integration/**`).
- `./gradlew test --tests "<fully.qualified.TestClass>"` — run a single test
  class.

**Streaming output:** Run tests using an async command so the output streams
in real time and use the equivalent of `read_powershell` to check progress
periodically. This allows the agent to monitor progress and detect hangs
rather than waiting silently for up to 20 minutes. Use `--console=plain` to
ensure Gradle does not use a rich console that suppresses intermediate output.

**Parallelising work:** When the full test suite is running, consider whether
any independent task can be done in parallel. For example, if Phase 6 (Code
Review) has not been done yet, launch the code review sub-agent while the
tests run. Other candidates include drafting PR descriptions or any other
non-conflicting work. Launch the test run first, then proceed with the
parallel task, checking back on the test output periodically.

---

### Phase 6 — Code Review

1. Launch a sub-agent to review the changes using the `reviewing-code` skill.
   This allows control over the model used and the reviewing priorities.
2. If the review identifies issues, return to Phase 4 to fix them, then
   re-verify (Phase 5) and re-review.
3. Once the agent review is clean, prompt the user:
   *"Changes are ready for your review. Please review them in IntelliJ.
   Let me know when you are satisfied or if you want any changes."*
4. Wait for user confirmation before proceeding.

---

### Phase 7 — Commit & PR

Once the user confirms satisfaction with the changes:

1. Use the `branch-and-commit-naming` skill to write the commit message.
2. Stage and commit all changes.
3. Push the branch to origin.
4. Use the `raising-pull-requests` skill to create the PR.
5. Report the PR URL to the user.
6. Clean up the worktree using the `using-git-worktrees` skill.

---

### Phase 8 — Next PR or Finish

If this was the last PR in the plan, report a summary of all PRs created and
stop.

If there are more PRs remaining:

**Stacked (parallel) PRs:**

- Use the `branch-and-commit-naming` skill to determine the branch name for
  the next PR.
- Create the new branch based on the previous PR's branch.
- Create a new worktree using the `using-git-worktrees` skill and launch
  IntelliJ.
- Return to Phase 4 with the next PR's tasks.
- When changes to an earlier branch are needed (e.g. from PR feedback),
  rebase downstream branches after pushing the fix.

**Sequential PRs:**

- Use the `branch-and-commit-naming` skill to determine the branch name for
  the next PR.
- Inform the user: *"The next PR is ready to start once the current PR is
  merged. Resume this workflow when ready."*
- Save the current state to the workflow state file under the user's home
  directory (`~/.copilot/workflow-state.json` on Unix/macOS or
  `%USERPROFILE%\.copilot\workflow-state.json` on Windows) so the session
  can be resumed. Minimum schema:
  ```json
  {
    "ticketId": "PDJB-123",
    "planPath": "docs/plans/2026-03-24-feature-name.md",
    "currentPr": 2,
    "totalPrs": 3,
    "strategy": "sequential",
    "completedPrs": ["https://github.com/.../pull/100"],
    "figmaLink": "https://figma.com/..."
  }
  ```
- When the user resumes, read the state file, create a new worktree from the
  latest main, and return to Phase 4 with the next PR's tasks.

---

## PR Feedback Loop

This section activates when the user signals that PR review comments have been
received. It operates outside the per-PR cycle numbering because it can happen
at any time after a PR is raised.

When entering the feedback loop, announce:

```
═══ PR Feedback Loop [PR M of T] ═══
```

1. Read the PR review comments using the GitHub MCP tools.
2. Use the `receiving-code-review` skill to evaluate each comment:
    - Assess whether each comment should be actioned, partially actioned, or
      pushed back on.
    - Consider the priorities defined in the `reviewing-code` skill. Not all
      comments warrant changes — it is acceptable to disagree with and push
      back on comments.
3. Present the evaluation to the user: for each comment, state whether you
   recommend actioning it and why.
4. Wait for user approval of the action plan.
5. Implement approved changes:
   a. Create a fresh worktree for the PR branch that received this feedback,
      ensuring that branch exists on `origin`, using the
      `using-git-worktrees` skill.
   b. Launch IntelliJ in the new worktree.
   c. Make the changes.
   d. Prompt the user to review in IntelliJ.
6. Re-run verification (as in Phase 5) to confirm the feedback changes have
   not introduced regressions. Present the verification plan for user
   approval before executing.
7. Once the user approves, commit and push.
8. Clean up the worktree using the `using-git-worktrees` skill.
9. Ask the user whether they would like drafted responses to the reviewer's
   comments. If yes, draft concise, professional responses for each comment
   and present them for approval before posting.

---

## State Tracking

Throughout the workflow, maintain awareness of the following state. When
transitioning between phases, verify that this state is still correct and
announce any changes.

- **Current phase** and **phase name**
- **Current PR number** (M of N)
- Ticket ID and branch name
- Worktree path
- Stacked vs sequential PR strategy
- Any Figma links provided

If at any point the current phase is unclear, re-read the plan and this skill
document to re-establish position. Do not guess or skip phases.

---

## Overrides to Default Instructions

When this workflow is active:

- **Commits**: The agent will commit changes, but **only** after explicit user
  approval in Phase 6/7. This overrides the default "do not commit"
  instruction.
- **Tests**: The agent will run tests as part of Phase 5, but **only** after
  presenting and receiving approval for the verification plan. This overrides
  the default "ask before running tests" instruction.
- **Linting**: The agent may run linters as part of verification in Phase 5,
  subject to the same user-approved verification plan. This clarifies the
  default "DO NOT try and run the linter after each change" instruction by
  allowing linting specifically in Phase 5 under an approved verification
  plan.
