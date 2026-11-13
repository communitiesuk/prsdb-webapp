---
name: using-git-worktrees
description: Use when creating or removing git worktrees for prsdb repositories. Ensures the project scripts are used so that gitignored files are copied and unique local ports are assigned.
allowed-tools: 'shell(git status) shell(git diff) shell(git log) shell(git show) shell(git branch) shell(git fetch) shell(git rev-parse) shell(powershell -File scripts\git-worktrees\new-worktree.ps1:*) shell(powershell -File scripts\git-worktrees\remove-worktree.ps1:*) shell(./scripts/git-worktrees/new-worktree.sh:*) shell(./scripts/git-worktrees/remove-worktree.sh:*) shell(git worktree list) shell(node scripts/git-worktrees/assign-worktree-ports.mjs:*)'
---

# Git Worktrees

## CRITICAL: Always use the project scripts

Do NOT use `git worktree add` or `git worktree remove` directly. The project provides
scripts that handle essential setup that raw git commands do not:

- Copying gitignored configuration files (`.env`, PEM keys)
- Assigning unique local ports so multiple worktrees can run in parallel
- Installing npm dependencies
- Cleaning up long-path directories on removal

## Creating a worktree

The base branch must exist on origin (push it first if it is a local-only branch).

**PowerShell (Windows):**
```powershell
powershell -File scripts\git-worktrees\new-worktree.ps1 -WorktreeName "<name>" -BranchName "<branch>" -BaseBranch "<base>"
```

**Bash (Linux/macOS):**
```bash
./scripts/git-worktrees/new-worktree.sh <name> <branch> [base-branch]
```

- `name`: folder name for the worktree (created as a sibling of the repo root)
- `branch`: new branch name, following the project naming convention
- `base`: branch to base from (default: `main`)

The script automatically assigns unique `SERVER_PORT`, `POSTGRES_PORT`, and `REDIS_PORT`
values in the new worktree's `.env` file so it can run alongside other worktrees.

## Copilot app worktree sessions

If you are running in the GitHub Copilot app and the session's workspace type is
`worktree`, the app has already created the worktree. Do NOT create another one.
When the session is created, the app copies the config files listed in
`.worktreeinclude`, and `.github/github-app.yml` runs
`scripts/git-worktrees/copilot-app-worktree.mjs`, which assigns unique ports in `.env`
and runs `npm install`. When the session is archived, the script stops the worktree's
Docker containers. Do not remove the worktree yourself — the app removes it when the
session is archived.

### Verify the setup

The output of these setup scripts is not shown to you, and they don't run if the user
hasn't accepted the repository configuration in the app. Check the setup before relying
on it:

1. `.env` and the PEM keys in `src/main/resources/` exist in the worktree.
2. `.env` sets `SERVER_PORT` to something other than `8080`.
3. `node_modules/` exists in the worktree.

Fix anything that is missing, using the main checkout (the first path listed by
`git worktree list`) as the source:

- **Missing config files** — run the `copy-config-files` script, which also assigns
  ports:
  ```powershell
  powershell -File scripts\git-worktrees\copy-config-files.ps1 -SourcePath "<main-checkout-path>" -DestinationPath "<worktree-path>"
  ```
  ```bash
  ./scripts/git-worktrees/copy-config-files.sh <main-checkout-path> <worktree-path>
  ```
- **Default or missing ports** — `node scripts/git-worktrees/assign-worktree-ports.mjs <worktree-path>`
- **Missing `node_modules/`** — `npm install`

If the config files or ports were missing, suggest that the user accepts the repository
configuration in the app so future sessions are set up automatically.

## Removing a worktree

**PowerShell:**
```powershell
powershell -File scripts\git-worktrees\remove-worktree.ps1 -WorktreePath "<name>" [-Force]
```

**Bash:**
```bash
./scripts/git-worktrees/remove-worktree.sh <name> [--force]
```

The removal script handles long-path cleanup, worktree pruning, and optionally deletes
the local branch.

## Running a worktree locally

Each worktree has its own `.env` with unique ports. To start the infrastructure and app:

```shell
cd <worktree-path>
docker-compose -f docker-compose.local.yml up -d
./gradlew bootRun
```

The app will start on the port specified by `SERVER_PORT` in that worktree's `.env`.

## Parallel browser testing

Use the Playwright CLI with named sessions to interact with different worktrees
simultaneously:

```shell
playwright-cli -s=<worktree-name> open http://localhost:<port>/landlord/register-as-a-landlord
```

Each `-s=<name>` session runs an independent browser instance.
