# Phase 1 — Setup

1. Ask the user for the task description and Jira ticket ID.
   If there is no ticket, use `PDJB-NONE`.
2. Use the `branch-and-commit-naming` skill to determine the branch name for
   the first PR.
3. Ask the user whether to create a new worktree or use the current workspace:
   - **New worktree** — use the `using-git-worktrees` skill to create a
     worktree for the branch. Launch IntelliJ in the worktree folder using
     the command recorded during preflight (e.g. `idea.cmd <worktree-path>`).
     Switch the working directory to the worktree using the `/cwd` command
     (e.g. `/cwd C:\Work\Projects\MHCLG\<worktree-name>`) so subsequent
     commands run from the worktree without needing `cd` or repeated path
     prefixes.
   - **Current workspace** — stay in the current working directory. Create or
     switch to the branch using `git checkout -b <branch>` (or
     `git checkout <branch>` if it already exists). If the worktree is
     not the default one, offer to copy gitignored config files from a
     sibling worktree using the `copy-config-files` script.
