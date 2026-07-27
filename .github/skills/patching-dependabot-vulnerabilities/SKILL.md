---
name: patching-dependabot-vulnerabilities
description: Use when asked to patch, triage, or clear Dependabot / security vulnerability alerts for a prsdb repository, or to prepare a security dependency-bump PR.
allowed-tools: 'shell(git status) shell(git fetch) shell(git checkout) shell(git rev-parse) shell(git add) shell(git commit) shell(git push) shell(git diff) shell(git log) shell(git ls-files) shell(gh api) shell(gh pr create) shell(gh pr ready) shell(gh pr checks) shell(gh pr view) shell(gh run view) shell(npm audit) shell(npm audit fix) shell(.\gradlew:*) shell(./gradlew:*) grep glob view edit'
---

# Patching Dependabot Vulnerabilities

Prepare and raise a PR that clears the open Dependabot alerts on a prsdb repository,
following the codebase's established dependency-pinning pattern, then let the draft PR's
pipeline verify the change before marking it ready for review.

**Core principle:** patch to the *minimum* version the advisory says is fixed, prefer
letting the framework (Spring Boot's dependency management) supply versions, and only
add a manual pin when the framework still resolves a vulnerable version. Remove pins the
moment the framework catches up.

## When to Use

- "Patch the Dependabot alerts", "clear the security vulnerabilities", "bump the
  vulnerable dependencies", or similar.
- Related skills apply: `branch-and-commit-naming` (branch/commit/PR names) and
  `raising-pull-requests` (PR body/template).

## Authority — approval waiver for this change type

The repository's standing agent rules ("do not commit changes", "do not run the linter",
"ask before running tests") are **set aside while this skill is active**, exactly as they
are suspended for the `development-workflow` skill. This skill has its own verification
gate — CI on a draft PR — so the agent may **commit, push, open the draft PR, and let the
pipeline run the tests** without pausing for per-step approval.

This waiver is scoped. The agent must still:

- keep the change limited to dependency / security version bumps (no unrelated edits);
- leave the PR as a **draft** until CI passes, then mark it ready — never merge it;
- never force-push over, or rebase, a shared branch;
- **iterate until CI is green** — if the build/test run fails, diagnose and fix the
  issues (including the small source adaptations a new version may require), push, and
  wait for a fresh run; mark the PR ready **only** once a run has fully passed;
- stop and ask only if a green build would need a substantial or behavioural source
  change, or if a failure is clearly unrelated to the bump (e.g. a pre-existing flake).

These changes always use the ticket `PDJB-NONE` — do not ask for a ticket ID; use it for
the branch name, commit message, and PR title.

## Step 0 — Start clean and on a branch

1. **Sync to the latest default branch first.** Local `main` is often stale even when
   `git status` says "up to date" — the remote-tracking ref can lag. Run
   `git fetch origin` and compare `git rev-parse HEAD` against
   `gh api repos/<org>/<repo>/commits/main --jq .sha`. Fast-forward if behind.
2. **Never work on `main`.** Create the branch (always `PDJB-NONE` for this work):
   `git checkout -b chore/PDJB-NONE-patch-dependabot-vulnerabilities`.

## Step 1 — Enumerate the alerts (source of truth)

Alerts are queried live from GitHub and are independent of your local checkout:

```pwsh
gh api "repos/<org>/<repo>/dependabot/alerts?state=open&per_page=100" --paginate --jq '.[] | {num:.number, sev:.security_advisory.severity, pkg:.security_vulnerability.package.name, eco:.security_vulnerability.package.ecosystem, vuln_range:.security_vulnerability.vulnerable_version_range, patched:.security_vulnerability.first_patched_version.identifier, manifest:.dependency.manifest_path, scope:.dependency.scope, summary:.security_advisory.summary}'
```

Group the rows by **ecosystem** (`maven`, `npm`) and by **package**, and record the single
`first_patched_version` you must reach for each package. Many alerts collapse into one
upgrade (e.g. every `io.netty:*` alert is fixed by one `netty.version` bump). Grab
`ghsa_id` / `cve_id` too — they go in the code comments.

## Step 2 — Understand how the project pins dependencies

- **Gradle** (`build.gradle.kts`): Spring Boot + `io.spring.dependency-management` supply
  managed versions via a BOM. Override a managed version by setting a Gradle property:
  `extra["<artifact>.version"] = "x.y.z"` (e.g. `netty.version`, `postgresql.version`,
  `jackson-bom.version`, `logback.version`, `commons-lang3.version`). One property drives
  every artifact in that BOM (`netty.version` covers all `netty-*`; `jackson-bom.version`
  aligns all jackson artifacts). Libraries Spring Boot does **not** manage (e.g.
  `commons-compress`) are handled with a `constraints { }` block instead.
- **Build classpath**: the `buildscript { }` block pins its own copies (e.g. the Flyway
  plugin's `org.postgresql:postgresql`). GitHub's dependency submission reports these too,
  so they need bumping independently of the runtime version.
- **npm** (`package-lock.json`): transitive dev/prod dependencies; fixed via `npm audit fix`.

## Step 3 — Check for a Spring Boot upgrade first (before adding pins)

A framework bump is cleaner than a manual pin and lets you *delete* obsolete pins.

1. Read the current version from the `org.springframework.boot` plugin in `build.gradle.kts`.
2. Confirm the latest patch in the current minor line via
   `https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/maven-metadata.xml`
   (probe specific POMs with a HEAD request to defeat cached metadata).
3. For each candidate, read what it *manages* from its BOM POM
   (`spring-boot-dependencies-<v>.pom`) — grep for `<netty.version>`, `<postgresql.version>`,
   `<jackson-bom.version>`, `<logback.version>`, etc.
4. **Decide:**
   - If a newer **patch or minor** in the same major line manages safe versions, bump the
     plugin version and **remove every `extra[...]` pin / constraint it makes redundant**
     (a managed version `>=` the required safe version means the pin is obsolete). Update
     the header comment's version number.
   - A new **major** (e.g. Spring Boot 4.x) is out of scope for a security patch — flag it
     as a follow-up, do not adopt it here.
   - If no eligible upgrade exists, keep the pinning pattern and continue.

## Step 4 — Apply the fixes

**Gradle** — for each Maven alert whose fixed version is newer than what Spring Boot
manages, add/adjust the override next to the existing ones, with a comment citing the
CVE/GHSA and fixed version (match the existing style):

```kotlin
// CVE-XXXX / GHSA-xxxx: <summary> (fixed in <version>).
extra["<artifact>.version"] = "<version>"
```

- Verify each target artifact/version exists on Maven Central (HEAD the POM) before writing it.
- Bump any matching `buildscript { }` classpath pins to the same fixed version.
- Keep comment lines within `.editorconfig`'s `max_line_length` (140 here); wrap long CVE
  lists across two comment lines.
- If a required pin is **already present** (fixed in an earlier PR), the alert is stale
  against a pre-fix dependency graph and will auto-close on the next submission — leave the
  pin, note it, do not duplicate it.

**npm** — `npm audit fix` (without `--force`). Confirm the target package reached the
patched version and that `git diff package-lock.json` is limited to the intended change;
`package.json` should be untouched. A large "added/removed/changed N packages" line usually
just means `node_modules` on disk was stale — the committed lockfile diff is what matters.

**Line endings** — the `edit`/`create` tools write CRLF on Windows, and CI has a
line-ending check that fails on CRLF. Convert every touched text file back to LF before
committing:
`(Get-Content .\file -Raw) -replace "\`r\`n","\`n" | Set-Content -NoNewline .\file`, then
confirm with `git ls-files --eol <file>` (want `i/lf`).

## Step 5 — Verify the patch actually takes effect (locally, before pushing)

A pin is only useful if resolution honours it.

- **Gradle** (read-only report, not the test suite):
  `.\gradlew -q dependencies --configuration runtimeClasspath` and grep for each affected
  artifact. Confirm each shows `-> <fixed version>` (or the fixed version outright). Use the
  relevant configuration for test-only dependencies.
- **npm**: `npm audit` reports `found 0 vulnerabilities`.
- Cross-check every alert number from Step 1 is covered by a resolved safe version.

## Step 6 — Raise a draft PR, let CI verify, then mark ready

Per the approval waiver above, do not stop for per-step approval here.

1. **Commit** the changes on the branch with a `PDJB-NONE:`-prefixed, sentence-case message and
   **no attribution line** (see `branch-and-commit-naming`), e.g.
   `PDJB-NONE: Bump vulnerable dependencies to clear Dependabot alerts`.
2. **Push** the branch and open a **draft** PR using the template
   (`raising-pull-requests`): `gh pr create --draft --title "<same as commit>" --body-file <file>`.
   Do **not** run the full gradle/integration suite locally — the PR pipeline runs it.
3. **Wait for CI.** The `Build and Test` workflow (`.github/workflows/build-and-test.yml`)
   runs on `pull_request` and covers the line-ending check, `npm ci`/`build`/`test`, and
   `./gradlew clean check` (unit **and** integration tests). It typically takes ~15–25 min.
   Block on it with `gh pr checks <number> --watch`, or poll every few minutes.
4. **If the run fails, fix it — never mark the PR ready with a red build.** Read the
   failing logs (`gh run view <run-id> --log-failed`), find the root cause, and fix it:
   - a line-ending failure means a file is still CRLF — renormalise to LF
     (`git add --renormalize .`) and recommit;
   - a compile or test failure caused by the new versions (a renamed API, a changed
     default, a tightened check) — make the small source adaptation the upgrade requires;
   - then push and **return to step 3**. Repeat the fix-and-wait loop until a run passes
     cleanly. Escalate to the user only if a green build would need a substantial or
     behavioural source change, or the failure is clearly unrelated to the bump.
5. **Only after a full CI run has passed**, mark the PR ready for review:
   `gh pr ready <number>`. Never run `gh pr ready` while checks are failing or still running.
6. **Report** the PR URL and a short passing-check summary. Note any alerts that were
   already fixed (and will auto-close) and any deferred major-version upgrade.

## Gotchas checklist

- [ ] Fetched and confirmed HEAD == remote `main` before starting.
- [ ] Working on a branch, not `main`.
- [ ] Trusted `git diff` / `grep` over `view` when they disagree (view can be stale).
- [ ] One BOM property (netty.version, jackson-bom.version) covers a whole family.
- [ ] Buildscript-classpath pins bumped alongside runtime pins.
- [ ] Files converted to LF and confirmed with `git ls-files --eol`.
- [ ] Comment lines within max_line_length.
- [ ] Resolution verified locally (`gradlew dependencies`, `npm audit`), not assumed.
- [ ] No obsolete pins left behind after any framework bump.
- [ ] PR opened as a **draft**; marked ready only after CI passed.
