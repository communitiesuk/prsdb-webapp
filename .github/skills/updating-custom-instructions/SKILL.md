---
name: updating-custom-instructions
description: Use when running a weekly merged-PR review for PRSDB Copilot instructions, auditing instruction drift, or applying an approved instruction-maintenance update.
---

# Updating Custom Instructions

Review changes introduced by merged PRs and update instructions only when they would otherwise mislead future work.
**No changes needed is a successful outcome.** Never manufacture additions to make a weekly run look productive.

## Scope and Authority

- The normal invocation reviews PRs merged into `main` since the last completed instruction review, not just the last seven days.
  Scheduling is external; invoking this skill does not create a schedule.
- Prepare justified local edits to `.github/instructions/*.instructions.md` and `.github/copilot-instructions.md`,
  including shared summaries and the directory table. An explicit audit-only request stops at the report without edits.
- Preserve user policies and unrelated work. Do not change application code, other documentation or this skill as part of a review.
- Local preparation is **not** permission to commit, push or publish. Require explicit approval of the exact diff and those actions.
  Approval of a previous run, a schedule or a subset of suggestions is not blanket publication approval.

## 1. Establish the Review Range

Use `gh` for GitHub operations. Record the repository, branch and dirty state; fetch the target branch and freeze its
commit as **H**. Read target-branch content at H, not unrelated local edits. Do not reset, stash or overwrite user work.

The durable checkpoint is `.github/instructions-review-state.json`, read **from the target branch at H**:

| Field | Value |
|-------|-------|
| `schemaVersion` | `1` |
| `baseBranch` | Reviewed target branch, normally `main` |
| `reviewedThrough` | Full commit SHA of the previously reviewed target snapshot, **B** |

Require the expected repository/branch, a valid checkpoint and B to be an ancestor of H. Review **(B, H]**.
An unmerged branch's checkpoint is never authoritative. H is not a wall-clock cutoff, the eventual maintenance-PR
merge commit, or whatever main points to when publication finishes.

**First run:** use a verified, explicitly recorded reviewed-through SHA from a merged full instruction refresh, or
obtain a user-confirmed initial baseline. Do not infer completeness from the newest instruction-file edit or invent a
seven-day baseline. If none can be established, report the blocker and stop. A full codebase audit is a separate,
explicitly requested bootstrap option.

Check for an existing open instruction-maintenance PR before preparing another patch. If one exists, report its URL
and pause; resume or amend it only with explicit permission. Do not create a competing draft.

## 2. Account for Every Merged PR

- Enumerate the target branch's first-parent commits in B..H and use GitHub's paginated commit-to-PR API
  (`repos/{owner}/{repo}/commits/{sha}/pulls`) to identify associated PRs. Deduplicate by PR number.
- Verify each PR is merged into the target and its merge commit belongs to the frozen range. Exclude open/draft PRs,
  other base branches and merges after H. Missed weekly runs must not leave gaps.
- Inspect each PR's description, changed files and relevant diff, then corroborate convention changes in the implementation
  and tests at H. PR prose alone is not evidence that a proposal became the current convention.
- Follow all pagination. Reconcile the PR set with the commit range; investigate direct pushes, missing associations and
  merge/rebase anomalies rather than silently treating them as covered.
- Failed fetches, truncated file lists/diffs or unavailable relevant evidence make the review incomplete. Recover the
  evidence or report a blocker; never claim a complete scan or advance the checkpoint over unknown work.

Inventory the instruction files and their `applyTo` patterns. Read global instructions, affected domain guides and
cross-cutting neighbours; inspect additional code only as needed to assess the PRs. Do not turn every run into an
unrelated whole-codebase rewrite. Use `making-code-edits` for source navigation. Empty results from an unavailable
or unverified index/glob do not prove that a symbol was removed.

## 3. Apply the Evidence and Anti-Bloat Gate

For each PR record a disposition: **no instruction impact**, **already covered**, **justified change**, or **unresolved**.
A candidate must answer all of these:

1. What would a future developer or agent get wrong without this correction?
2. Which merged PR and current source path/lines establish the fact or reusable convention?
3. Is this a shared contract or repeatable pattern, rather than a one-off choice, legacy exception or incomplete migration?
4. Is an existing instruction wrong or materially incomplete, rather than already sufficient?

A framework/API change can qualify without appearing in several PRs. Conversely, dependency/version bumps without
workflow consequences, routine bug fixes, business-specific details and stylistic preferences normally need no addition.
Do not append class inventories, changelogs, fixed component counts or repeated dependency versions.

Existing code is evidence of practice, not permission to weaken deliberate policy. Explain legitimate exceptions;
flag unresolved policy questions instead of normalising violations. Prefer replacing/removing stale text or linking
to an existing guide over adding another section. New instruction files need a distinct, durable coverage gap.

Keep the compact PR/disposition/evidence record in the session workspace, not as weekly repository documentation.
If no change passes this gate, report the reviewed range, coverage and reasons, then stop: **no edits, approval prompt,
commit, PR or checkpoint-only update**. Rechecking that range next time is preferable to administrative churn.

## 4. Prepare and Present the Minimal Patch

Prepare changes in a safe workspace based on H, using `branch-and-commit-naming` for a new maintenance branch and
`using-git-worktrees` if another worktree is needed. If existing edits overlap or safe isolation is unavailable, stop
and report the conflict; do not absorb user changes into the proposal.

Preserve unaffected wording and style. Keep `applyTo` YAML frontmatter, use real examples and avoid duplicating global
rules in domain guides. Reconcile shared summaries and scope-table entries so instructions do not contradict each other.

Check example signatures against source, local links, YAML frontmatter, intended and unintended glob matches,
directory-table completeness, cross-file consistency, UTF-8/LF and the diff's file scope. Documentation-only changes
do not require application builds, tests or linting; do not run them just to fill a PR checklist.

If substantive instruction changes are prepared and **all** work through H is assessed and resolved, include a proposed
checkpoint with `reviewedThrough = H` in that patch. Otherwise leave the authoritative checkpoint unchanged. Never
create a checkpoint-only PR. A candidate checkpoint on a local branch/draft has no effect on later runs until merged.

Present:
- Target, B..H, PR coverage and any blockers or existing maintenance PR.
- Justified changes with source PRs, evidence, affected instruction files and the concrete reason each rule is needed.
- PRs retained as no-impact/already-covered, grouped concisely by reason; unresolved items must remain visible.
- The actual inspectable diff, documentation checks, proposed checkpoint and intended commit/push/**draft PR** action.

Ask for approval of that exact patch and publication. For partial approval, retain deferred items in the report and
exclude this run's unapproved hunks and proposed checkpoint from the publishable patch, without reverting prior user
work. **Do not advance the checkpoint.** Present the reduced diff. Approval to edit only is not approval to publish;
a checkpoint change also requires inclusion in the approved diff. If that diff changes materially, present it again
rather than extending the original permission.

## 5. Publish Only After Explicit Approval

Use `development-workflow` at its Commit & PR phase with this approved documentation scope and review result, not
an unrelated feature-development cycle. Use **`branch-and-commit-naming`** and **`raising-pull-requests`** for the
commit/title and repository PR template.

1. Recheck branch, diff and approval. New target commits after H belong to the next review; do not silently move the checkpoint.
2. Stage only the approved instruction files and eligible checkpoint, inspecting the staged diff for unrelated changes.
   Never interpret the general workflow's "all changes" as every dirty file in the checkout. Commit without attribution trailers.
3. Push the maintenance branch and create a PR into the reviewed target with `gh pr create --draft`. Include the reviewed
   range, motivating PRs and why the changes matter. Remove inapplicable template items; do not claim unrun tests passed.
4. If publication fails or times out, reconcile the branch/PR state before retrying; a timeout does not prove no PR was created.
   Reuse the existing draft rather than creating duplicates.
5. Report the draft URL and any unresolved work. Do not merge, mark ready, post comments or discard the workspace automatically.

The next run reads the checkpoint from the target branch again. A rejected, closed-unmerged or merely published draft
must never move the review baseline. Only the merged, approved checkpoint establishes completed coverage.
