---
name: development-workflow
description: Use when starting a new development task. Orchestrates the full lifecycle from setup through implementation, review, and PR creation.
---

# Development Workflow

Orchestrates the full development lifecycle for prsdb repositories.

## Phases

| # | Name | Summary |
|---|------|---------|
| 0 | Preflight | Verify tools via `preflight-checks` skill |
| 1 | Setup | Create branch, worktree, launch IDE |
| 2 | Brainstorm | Clarify requirements, explore approaches |
| 3 | Plan | Write implementation plan with PR breakdown |
| 4 | Implement | Execute plan tasks for current PR |
| 5 | Verify | Run tests and smoke tests |
| 6 | Review | Code review (agent + user) |
| 7 | Commit & PR | Stage, commit, push, raise draft PR |
| 8 | Next or Finish | Advance to next PR or complete |

**Structure:** Phases 0-3 run once. Phases 4-8 repeat per PR.

**Phase loading:** When entering a phase, read the corresponding file from
`.github/skills/development-workflow/phases/phase-N-<name>.md`. The PR feedback
loop is in `pr-feedback-loop.md`.

---

## Phase Transition Protocol

At every phase transition, execute this mandatory sequence:

1. Update the JSON state file with the new phase and current state
2. Update the markdown checkpoint file
3. Re-read the phase file for the phase being entered
4. Re-read the plan file to refresh the current PR's scope
5. Re-read the JSON state file to confirm position
6. Announce the new phase using the format below

### Phase Announcement Format

**When PR plan is known (Phases 4-8):**
```
═══ Phase N — <Phase Name> [PR M of T: <short PR title>] ═══
Ticket: <ID> | Branch: <branch> | Worktree: <path>
```

**Before PR plan is confirmed (Phases 0-2, early Phase 3):**
```
═══ Phase N — <Phase Name> [PR TBD] ═══
```

---

## State Tracking

### Structured JSON State File (primary)

Maintain at `~/.copilot/workflow-checkpoints/<ticket-id>.json` (Windows:
`%USERPROFILE%\.copilot\workflow-checkpoints\<ticket-id>.json`).

Updated at every phase transition AND mid-phase checkpoints.

Schema:
```json
{
  "ticketId": "PDJB-123",
  "currentPhase": 4,
  "phaseName": "Implement",
  "currentPr": { "number": 1, "of": 3, "title": "Add database migration" },
  "branch": "feat/PDJB-123-add-migration",
  "worktreePath": "C:\\Work\\Projects\\MHCLG\\pdjb-123",
  "strategy": "stacked",
  "planPath": "~/.copilot/session-state/<id>/files/plan.md",
  "figmaLink": null,
  "tasksCompleted": [],
  "tasksRemaining": [],
  "currentTask": null,
  "keyDecisions": [],
  "verificationStatus": {
    "unitTests": "not_started",
    "controllerTests": "not_started",
    "integrationTests": "not_started",
    "smokeTest": "not_started",
    "linting": "not_started"
  }
}
```

### Markdown Checkpoint (human-readable backup)

Maintain at `~/.copilot/workflow-checkpoints/<ticket-id>.md`.

Content:
```markdown
# Workflow Checkpoint — <TICKET-ID>
- Ticket: <ID>
- Phase: N — <Name>
- PR: M of T
- Branch: <branch>
- Worktree: <path>
- Strategy: stacked/sequential
- Plan: <plan-path>
- Figma: <link or "none">
- Current PR scope: <one-line summary>
- Verification strategy: <one-line summary>
- Decisions made: <key decisions>

## Recovery
If reading this to recover context:
1. Re-read the plan file
2. Re-read `.github/skills/development-workflow/SKILL.md`
3. Re-read the phase file for the current phase
4. Announce recovered state to user for confirmation
```

---

## Preventing Context Loss

### Intra-Phase Checkpoints

Checkpoints fire at phase transitions AND within phases:
- **Phase 4:** After each sub-agent task completes
- **Phase 5:** After each verification step completes
- **Phase 7:** After each significant action (commit, push, PR creation)
- **Any phase:** After user interaction that changes direction or scope

Each mid-phase checkpoint updates both the JSON state and markdown checkpoint.

### Context-Compact Summaries

After receiving each sub-agent result, extract and record ONLY:
- Status: DONE / DONE_WITH_CONCERNS / BLOCKED / NEEDS_CONTEXT
- Files changed: list of file paths
- Concerns: any issues flagged (empty if none)

Write a one-line summary to the checkpoint. Do NOT carry forward the sub-agent's
full narrative, self-review, or test output.

### Turn-Count Heuristic

If you have made 15+ tool calls within a single phase without a checkpoint:
1. Write a mid-phase checkpoint (JSON + markdown)
2. Re-read the plan section for the current PR
3. Re-read the JSON state file
4. Continue from where you left off

This is a safety net — it catches cases where event-driven checkpoints were missed.

### Recovery Procedure

If the current phase or PR is unclear:
1. Read the JSON state file (fastest, least ambiguous)
2. Read the markdown checkpoint
3. Re-read the plan file
4. Re-read this skill document and the current phase file
5. Announce recovered state to user for confirmation

---

## Overrides to Default Instructions

When this workflow is active:
- **Commits:** Allowed after explicit user approval in Phase 7
- **Tests:** Allowed in Phase 5 after presenting and receiving approval for the
  verification plan
- **Linting:** Allowed in Phase 5 under an approved verification plan

---

## Sub-Agent Delegation

### Phase 5 — Delegated Verification

Dispatch a single sub-agent (general-purpose) with:
- The verification strategy from the plan
- The worktree path as `projectPath`
- Instructions to run each verification step and return a structured report

The orchestrator receives only the structured report. If there are failures, dispatch
fix sub-agents as needed.

### Phase 7 — Delegated PR Creation

Consider delegating the PR template checklist and `gh pr create` to a sub-agent
that returns the PR URL. This keeps the PR creation output out of the orchestrator's
context.