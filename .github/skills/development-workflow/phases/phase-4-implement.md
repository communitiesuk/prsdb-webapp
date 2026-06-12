# Phase 4 — Implement

> **Scope guard:** Implement ONLY the tasks assigned to the current PR in the plan.

> **Worktree guard:** All file edits, tool invocations, and sub-agent prompts must
> target the working directory (worktree path or current workspace). Pass it as
> `projectPath` to JetBrains MCP.

## Bug Verification

If the ticket is a bug fix and the plan includes reproducing the bug locally, do so
before writing any code. Use the `running-locally` skill to start the application and
the `smoke-testing` skill to verify the bug exists. If local reproduction is not
possible, ask the user to confirm the bug exists.

## Implementation

Follow the plan's task breakdown for the current PR. Use the
`subagent-driven-development` skill or implement directly as appropriate.

Follow TDD where the plan's verification strategy specifies it.

Use the `making-code-edits` skill for all code searching, reading, and editing.

Do **not** commit during implementation — changes are committed in Phase 7 after
review.

## Processing Sub-Agent Results

After receiving each sub-agent result, extract and record only:
- **Status:** DONE / DONE_WITH_CONCERNS / BLOCKED / NEEDS_CONTEXT
- **Files changed:** list of file paths
- **Concerns:** any issues flagged (empty if none)

Write a one-line summary to the checkpoint file, e.g.:
`Task 3 — DONE — FooService.kt, FooServiceTests.kt`

Do not carry forward the sub-agent's full implementation narrative, self-review text,
or test output. If details are needed later, they can be recovered from the code.

## Mid-Phase Checkpoints

After each sub-agent task completes, update the checkpoint with:
- Current sub-step (e.g. "Phase 4, Task 3 of 7 — DONE")
- Cumulative list of completed tasks and files changed
- Remaining tasks
- Key decisions made during this task
