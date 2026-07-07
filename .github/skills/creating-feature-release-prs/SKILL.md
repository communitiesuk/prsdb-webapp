---
name: creating-feature-release-prs
description: Use when asked to create a feature release, release a feature flag to an environment, or perform a config-only release for prsdb-webapp.
allowed-tools: 'shell(git status) shell(git diff) shell(git log) shell(git show) shell(git branch) shell(git switch) shell(git fetch) shell(git cherry-pick) shell(git rev-parse) shell(git push) shell(gh pr list) shell(gh pr view) shell(gh pr create) shell(gh pr edit)'
---

# Creating Feature Release PRs

A **feature release** is a config-only release that changes feature-flag values for a single
environment, **without** shipping any code that has accumulated on `main` since the last code release.

Unlike code releases, a feature release applies to the **`communitiesuk/prsdb-webapp`** repository
only — feature flags are not configured in `prsdb-infra`, so no infra release is required.

Feature-flag values are stored per environment in the webapp's `application-<profile>.yml` files, on
the same branches that deploy to each environment. A normal `main -> {env}` merge would therefore drag
in all of main's unreleased code. A feature release avoids this by branching from the **target
environment branch** and cherry-picking only the flag change.

## Environment mapping

| Environment | Flag file to edit                             | Deploy branch |
|-------------|-----------------------------------------------|---------------|
| test        | `src/main/resources/application-test.yml`     | `test`        |
| nft         | `src/main/resources/application-nft.yml`      | `nft`         |
| production  | `src/main/resources/application.yml` (base)   | `production`  |

Every flag is present in each environment override file, so the `application.yml` base values only
affect production.

## Process

1. **Confirm the scope with the requester.** Before building anything, establish exactly:
   - which feature flag(s) and/or release group(s) are changing (a `release` in `application.yml`
     toggles all flags assigned to it at once);
   - the new value for each (enabled/disabled); and
   - which environment(s) the change is being made live in (test, nft and/or production).

   Do not assume — a feature release to each environment is a separate PR, so this determines how many
   PRs are needed and which files/branches they target.
2. **Author the change on `main` first.** Raise a normal PR that edits **only** the target
   environment's flag file, as a standalone commit with no code changes, and merge it to `main`. This
   keeps `main` the single source of truth and makes the next code release consistent. The change is
   inert for test/nft/production until released (it takes effect immediately only where `main` deploys,
   e.g. integration). Use one commit per environment so each cherry-pick stays environment-specific.
3. **Fetch latest changes** from origin.
4. **Find the config commit** on `main` and note its SHA.
5. **Check for an existing draft PR** for this feature release — update it rather than creating a new one.
6. **Find previous feature release PRs** to determine the next release number.
7. **Create the release branch from the target environment branch** (not `main`) and **cherry-pick
   only** the flag commit(s). Because the branch is based on the environment branch, the diff contains
   only the flag change.
8. **Create or update the PR** into the environment branch with release notes.
9. **Merge with a normal (not squash) merge**, consistent with other merges into environment branches.
   No merge back into `main` is needed — the change already originated there.

Repeat for each environment the change is being made live in; environments are released independently and
there is no required ordering (a flag can be feature-released straight to production).

## Branch and PR naming

- Branch: `release/feature-{env}-{N}` (e.g. `release/feature-test-3`) — no feature descriptor in the name (a release may cover several flags); identify the flag(s)/release(s) in the PR description
- PR title: `Feature release to {env} #N` (increment from the last feature release to that environment)

## Release Notes Format

- State which feature flag(s) and/or release group(s) change and their new value(s)
- Link the `main` PR that introduced the change
- Note any coordination required (e.g. product approval for production — see below)

## Commands

```bash
git fetch origin

# Find the config-only commit on main (example: test)
git log origin/main --oneline -- src/main/resources/application-test.yml

# Check for an existing draft PR
gh pr list --repo communitiesuk/prsdb-webapp --state open --draft --search "Feature release to test"

# Find previous feature release PR numbers
gh pr list --repo communitiesuk/prsdb-webapp --state all --search "Feature release to test" --limit 5

# Branch from the TARGET env branch and cherry-pick only the flag commit
git switch -c release/feature-test-{N} origin/test
git cherry-pick {sha}
git push -u origin release/feature-test-{N}

# Create the PR into the env branch (or gh pr edit an existing draft)
gh pr create --base test --head release/feature-test-{N} \
  --title "Feature release to test #{N}" \
  --body "## Release notes

Enables {flag-name} in test. See {link-to-main-PR}."
```

## Notes

- The feature-release PR must contain **only** the flag-config change. There is no automated guard, so
  confirm the diff before requesting review.
- Releasing to **production** has extra approval requirements — check the feature flag's epic tickets
  are approved ('Done') before releasing, and follow the "Releasing to Prod" guidance in `ReadMe.md`.
- If no config commit exists on `main` yet, do step 1 first.
- If the feature should go live as part of a release group that does not yet exist, create the release group and
  associate the flag on `main` first (release groups cannot be empty and their names need a constant — see
  `docs/FeatureFlagsReadMe.md`).
- If no feature-flag change is pending for the environment, no feature release is needed.
