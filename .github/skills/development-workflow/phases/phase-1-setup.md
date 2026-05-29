# Phase 1 — Setup

1. Ask the user for the task description and Jira ticket ID.
   If there is no ticket, use `PDJB-NONE`.
2. Use the `branch-and-commit-naming` skill to determine the branch name for
   the first PR.
3. Use the `using-git-worktrees` skill to create a worktree for the branch.
4. Launch IntelliJ in the worktree folder using the command recorded during
   preflight (e.g. `idea.cmd <worktree-path>`).
5. Change the working directory to the worktree.
