---
name: using-git-worktrees
description: Use when creating or removing git worktrees for prsdb repositories. Ensures the project scripts are used so that gitignored files are copied and unique local ports are assigned.
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
