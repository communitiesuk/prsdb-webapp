# PR Feedback Loop

Activates when PR review comments are received.

```
═══ PR Feedback Loop [PR M of T] ═══
```

1. Read PR review comments using `gh` CLI.
2. Use `receiving-code-review` skill to evaluate each comment:
    - Assess: action, partially action, or push back
    - Consider `reviewing-code` skill priorities
3. Present evaluation to user: for each comment, state recommendation and why.
4. Wait for user approval.
5. Implement approved changes:
   a. Create fresh worktree for the PR branch (ensure branch exists on origin)
   b. Launch IntelliJ
   c. Make changes
   d. Prompt user to review in IntelliJ
6. Re-run verification (present plan for approval first).
7. Commit and push after user approval.
8. Clean up worktree.
9. Offer to draft responses to reviewer comments.
