# Phase 7 — Commit & PR

Once the user confirms satisfaction:

1. Use the `branch-and-commit-naming` skill to write the commit message.
2. Stage and commit all changes.
3. Push the branch to origin.
4. **PR template checklist** — read the PR template and go through each item:
    - If the agent can perform the action, do so and confirm.
    - If the agent cannot but the item is relevant, ask the user to confirm.
    - If not relevant to this PR, delete it from the PR description.
5. Use the `raising-pull-requests` skill to create the PR as a **draft**
   (use `gh pr create --draft`).
6. Report the PR URL to the user.
7. Clean up the worktree using the `using-git-worktrees` skill.
