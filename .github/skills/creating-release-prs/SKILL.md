---
name: creating-release-prs
description: Use when asked to create release PRs, prepare releases, or deploy to test or nft environments for prsdb repositories.
allowed-tools: 'shell(git status) shell(git diff) shell(git log) shell(git show) shell(git branch) shell(git fetch) shell(git rev-parse) shell(git merge-tree) shell(git switch) shell(git merge) shell(git add) shell(git commit) shell(git push) shell(gh pr list) shell(gh pr view) shell(gh pr create) shell(gh pr edit) shell(gh pr close)'
---

# Creating Release PRs

Create release PRs for main -> test and main -> nft branches in **both repositories**:
- `communitiesuk/prsdb-infra`
- `communitiesuk/prsdb-webapp`

## Process

1. **Fetch latest changes** from origin for both repositories.
2. **Check commits** between branches using `git log origin/{target}..origin/main --oneline`.
3. **Find existing and previous release PRs.** Reuse an existing release's number and notes; otherwise determine
   the next release number and follow the existing format.
4. **Check each proposed merge for conflicts** before choosing a PR head, using
   `git merge-tree --write-tree origin/{target} origin/main` in the appropriate repository.
   Inspect its exit status before running another command (`$LASTEXITCODE` in PowerShell or `$?` in Bash):
   - `0`: create or update the usual direct `main -> {target}` PR. No temporary release branch is needed.
   - `1`: use the conflict-resolution procedure below.
   - Any other status: stop and investigate the failed check; do not treat it as a clean merge.
5. **Create or update the appropriate PR** with release notes summarising the changes. Recheck if either branch
   advances during preparation; an unknown GitHub mergeability result is not confirmation that the PR is conflict-free.
6. **Keep normal merges into environment branches.** Do not squash a code release or change the merge queue/squash
   policy for development and feature-config PRs into `main`.

## When the code release conflicts

Follow [Merge conflicts](../../../ReadMe.md#merge-conflicts):

- Create a temporary branch from the **destination**, such as `release/main-to-test-{N}` from `origin/test`,
  or reuse the existing resolution branch for this release.
- Merge the intended source revision and any newer destination changes into that branch, resolve conflicts and
  complete the normal merge commits.
- Review flag conflicts by flag/release name rather than blindly accepting a whole config file: the destination may
  contain a newer feature release, while the source may add or retire flags with its code changes.
- Recheck using the temporary branch as the source, for example
  `git merge-tree --write-tree origin/test release/main-to-test-{N}`, and review its diff against the destination.
  Do not repeatedly check the original conflicting source/destination pair after resolving on the temporary branch.
- Raise the PR into the **destination**, preserving the release number, notes and any special instructions.
  If replacing a direct PR, link its replacement, update the release tracking ticket and close the superseded PR.
- Do not resolve conflicts on `main` or merge the resolution branch back into the source. Normal-merging the
  release branch into the destination preserves ancestry for the next promotion.

Use this fallback only when the merge check reports conflicts, not for every code release. For `test -> production`,
the same procedure uses `production` as the destination and `test` as the source.

If the destination advances before the release merges, merge its latest revision into the temporary branch and
repeat the conflict check and diff review. If more source changes are included, refresh the release notes and checks.

## Release Notes Format

- Group commits by ticket number (PRSD-\*, PDJB-\*)
- Order tickets by the position of their first commit in the git log (oldest first)
- Combine related commits under a single ticket entry
- Group PRSD-NONE/PDJB-NONE items together at the end
- Use format: `TICKET: Brief description`

Example:
```
## Release notes

PDJB-119: Send joint landlord invitations
PDJB-273: Join a registered property as a joint landlord
PDJB-467: Gas safety task, EICR task, EPC task
PRSD-1021: NGD Address Update Task Runner
PRSD-NONE: Fixes bug on windows, Updates test seed data
```

## PR Title Format

- main -> test: `Release main to test #N` (increment from last release)
- main -> nft: `Release main to nft #N` (increment from last release)

## Commands

```bash
# Check commits to release (fetch and log in same command)
git fetch origin && git log origin/test..origin/main --oneline
git fetch origin && git log origin/nft..origin/main --oneline

# Check mergeability before choosing a PR head; inspect the exit status immediately
git merge-tree --write-tree origin/test origin/main

# Find previous release PR numbers
gh pr list --repo communitiesuk/prsdb-webapp --state all --search "Release main to test" --limit 5
gh pr list --repo communitiesuk/prsdb-webapp --state all --search "Release main to nft" --limit 5

# Create direct PRs only when each target's merge check is clean and no draft exists
gh pr create --base test --head main --title "Release main to test #N" --body "## Release notes

..."
gh pr create --base nft --head main --title "Release main to nft #N" --body "## Release notes

..."
```

## Notes

- If no commits exist between branches, no PR is needed.
- Stop if fetching fails. Run the merge check separately for each repository and target; a clean test merge does not
  establish that NFT is conflict-free.
- Include any special release instructions if commits require manual steps (e.g., database migrations, secret population).
- When updating a draft PR, preserve any existing special release instructions that were added manually.
- Repeat the process for both `prsdb-infra` and `prsdb-webapp` repositories.
- All release PRs made at the same time should use the same release number for consistency. This may lead to infra skipping a release. This is fine.
