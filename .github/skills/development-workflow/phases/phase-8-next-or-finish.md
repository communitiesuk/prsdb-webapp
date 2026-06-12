# Phase 8 — Next PR or Finish

If this was the last PR: report a summary of all PRs created and stop.
Delete the checkpoint files.

If more PRs remain:

## Stacked (Parallel) PRs

- Use `branch-and-commit-naming` skill for the next branch name.
- Create the new branch based on the previous PR's branch.
- **If using worktrees**: create a new worktree using `using-git-worktrees`
  and launch IntelliJ.
- **If using current workspace**: switch to the new branch with
  `git checkout -b <branch>`.
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
- When resumed: read state, create new worktree from latest main (or switch
  branch if not using worktrees), return to Phase 4.
