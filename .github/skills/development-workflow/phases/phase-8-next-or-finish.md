# Phase 8 — Next PR or Finish

If this was the last PR: report a summary of all PRs created and stop.
Delete the checkpoint files.

If more PRs remain:

## Stacked (Parallel) PRs

- Use `branch-and-commit-naming` skill for the next branch name.
- Create the new branch based on the previous PR's branch.
- Create a new worktree using `using-git-worktrees` and launch IntelliJ.
- Return to Phase 4.
- When changes to an earlier branch are needed, rebase downstream branches.

## Sequential PRs

- Use `branch-and-commit-naming` skill for the next branch name.
- Inform the user: *"Next PR ready to start once current PR is merged."*
- Save state to `~/.copilot/workflow-checkpoints/<ticket-id>.json`:
  ```json
  {
    "ticketId": "PDJB-123",
    "planPath": "~/.copilot/session-state/<id>/files/plan.md",
    "currentPr": 2,
    "totalPrs": 3,
    "strategy": "sequential",
    "completedPrs": ["https://github.com/.../pull/100"],
    "figmaLink": null
  }
  ```
- When resumed: read state, create new worktree from latest main, return to Phase 4.
