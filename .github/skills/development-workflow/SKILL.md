---
name: development-workflow
description: Use when starting a new development task. Orchestrates the full lifecycle from setup through implementation, review, and PR creation.
---

# Development Workflow

Orchestrates the full development lifecycle for prsdb repositories. Follow each
phase in order, completing one before moving to the next.

---

## Phase 0 — Preflight

Invoke the `preflight-checks` skill to verify all required tools are available.

Treat preflight results as follows:

- **Critical** (block if missing): `gh` CLI, IntelliJ CLI, Docker, Playwright
  CLI.
- **Task-dependent** (block only for UI/content tasks): Figma MCP.

Do not proceed if any applicable critical tool is missing unless the user
explicitly confirms they want to continue.

---

## Phase 1 — Setup

1. Ask the user for the task description and Jira ticket ID.
   If there is no ticket, use `PDJB-NONE`.
2. Use the `branch-and-commit-naming` skill to determine the branch name.
3. Create and push the branch to origin.
4. Use the `using-git-worktrees` skill to create a worktree for the branch.
5. Launch IntelliJ in the worktree folder using the command recorded during
   preflight (e.g. `idea64 <worktree-path>`).
6. Change the working directory to the worktree.

---

## Phase 2 — Brainstorm

1. If the task involves UI, presentational, or content changes and no Figma
   link has been provided, ask the user for the relevant Figma file or
   fragment URL.
2. Invoke the `brainstorming` skill. Include any Figma link as context.

---

## Phase 3 — Plan

1. Invoke the `writing-plans` skill.
2. The plan **must** include a strategy to split the work into multiple small,
   easy-to-review PRs when the task is non-trivial. Define clear boundaries
   for each PR.
3. After the plan is written, ask the user:
   *"Should the PRs be raised in parallel (stacked on each other) or
   sequentially (one at a time, waiting for each to be merged)?"*
4. Record the answer for use in Phase 9.

**Override:** When the `writing-plans` skill offers an execution handoff
choice, skip it. This orchestrator manages execution directly.

---

## Phase 4 — Implement

Execute the plan for the current PR's worth of changes:

- Follow the plan's task breakdown. Use the `subagent-driven-development`
  skill or implement directly as appropriate.
- Follow TDD where the plan specifies tests.
- Do **not** commit during implementation — changes are committed in Phase 7
  after review.

---

## Phase 5 — Verify

1. Analyse the changes made and propose a verification plan. Consider:
    - **Specific tests** — unit, controller, and integration tests directly
      related to the changes.
    - **Full test suite** — if changes are cross-cutting or affect shared
      infrastructure.
    - **Local smoke test** — if changes affect navigation, journey logic, or
      UI/presentation: run the application locally, then use the Playwright
      CLI to manually smoke test the affected pages and journeys.
2. Present the verification plan to the user for confirmation.
3. Execute the approved plan and report results.
4. If verification fails, return to Phase 4 to fix issues, then re-verify.

---

## Phase 6 — Code Review

1. Launch a sub-agent to review the changes using the `reviewing-code` skill.
   This allows control over the model used and the reviewing priorities.
2. If the review identifies issues, return to Phase 4 to fix them, then
   re-verify (Phase 5) and re-review.
3. Once the agent review is clean, prompt the user:
   *"Changes are ready for your review. Please review them in IntelliJ.
   Let me know when you are satisfied or if you want any changes."*
4. Wait for user confirmation before proceeding.

---

## Phase 7 — Commit & PR

Once the user confirms satisfaction with the changes:

1. Use the `branch-and-commit-naming` skill to write the commit message.
2. Stage and commit all changes.
3. Push the branch to origin.
4. Use the `raising-pull-requests` skill to create the PR.
5. Report the PR URL to the user.
6. Clean up the worktree using the `using-git-worktrees` skill.

---

## Phase 8 — PR Feedback Loop

This phase activates when the user signals that PR review comments have been
received.

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
   a. Create a fresh worktree using the `using-git-worktrees` skill.
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

## Phase 9 — Next PR

If the plan from Phase 3 includes multiple PRs:

**Stacked (parallel) PRs:**

- Create a new branch based on the previous PR's branch.
- Create a new worktree and launch IntelliJ.
- Return to Phase 4 with the next PR's tasks.

**Sequential PRs:**

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

When all PRs are complete, report a summary of all PRs created.

---

## State Tracking

Throughout the workflow, maintain awareness of:

- Current phase
- Ticket ID and branch name
- Worktree path
- Which PR (of N) is being worked on
- Stacked vs sequential PR strategy
- Any Figma links provided

---

## Overrides to Default Instructions

When this workflow is active:

- **Commits**: The agent will commit changes, but **only** after explicit user
  approval in Phase 6/7. This overrides the default "do not commit"
  instruction.
- **Tests**: The agent will run tests as part of Phase 5, but **only** after
  presenting and receiving approval for the verification plan. This overrides
  the default "ask before running tests" instruction.
