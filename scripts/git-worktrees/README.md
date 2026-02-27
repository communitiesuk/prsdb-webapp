# Git Worktree Management Scripts

## What are git worktrees?

Git worktrees let you check out multiple branches of the same repository simultaneously, each in its own directory. Instead of stashing or committing work-in-progress to switch branches, you can have separate working copies side by side — for example, one for your current feature and another for a code review or hotfix. All worktrees share the same `.git` history, so commits, branches, and stashes are visible across them.

See the [official documentation](https://git-scm.com/docs/git-worktree) for more details.

## Overview

Scripts for managing git worktrees in this repository. Both **PowerShell** (`.ps1`) and **Bash** (`.sh`) versions are provided. Worktrees are created as siblings of the repo directory, and all paths are resolved dynamically so these scripts work for any developer.

## Prerequisites

- **PowerShell** 5.1+ (or PowerShell Core) — for `.ps1` scripts
- **Bash** — for `.sh` scripts (Git Bash on Windows, or native on macOS/Linux)
- **Git**
- **Node.js / npm** — required by `new-worktree` to install frontend dependencies

## Scripts

### `new-worktree` (.ps1 / .sh)

Creates a new git worktree with all necessary configuration files and dependencies.

**What it does:**
1. Fetches latest from origin
2. Creates a new worktree with a new branch (or prompts to use an existing branch)
3. **Dynamically copies gitignored configuration files** from the main repo (see [Config file copying](#config-file-copying) below)
4. Runs `npm install` to set up frontend dependencies

**Parameters:**

| PowerShell | Bash | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `-WorktreeName` | `<worktree-name>` (positional) | Yes | — | Name for the new worktree folder (created as a sibling of the repo) |
| `-BranchName` | `<branch-name>` (positional) | Yes | — | Name of the branch to create (e.g. `feat/PDJB-123/description`) |
| `-BaseBranch` | `<base-branch>` (positional) | No | `main` | Branch to base the new worktree on |

**Examples:**
```powershell
# PowerShell
.\new-worktree.ps1 -WorktreeName "my-feature" -BranchName "feat/PDJB-123/new-feature"
.\new-worktree.ps1 -WorktreeName "bugfix" -BranchName "fix/PDJB-456/bug-fix" -BaseBranch "test"
```
```bash
# Bash
./new-worktree.sh my-feature feat/PDJB-123/new-feature
./new-worktree.sh bugfix fix/PDJB-456/bug-fix test
```

---

### `remove-worktree` (.ps1 / .sh)

Removes a git worktree and optionally deletes the associated local branch.

**What it does:**
1. Checks for uncommitted changes (warns before proceeding)
2. Confirms removal with the user (unless force is used)
3. Removes the worktree via `git worktree remove`
4. Prunes stale worktree references
5. Optionally deletes the local branch (never deletes `main` or `test`)

**Parameters:**

| PowerShell | Bash | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `-WorktreePath` | `<worktree-path>` (positional) | Yes | — | Path or name of the worktree to remove. If a name is given, assumes it's a sibling of the repo directory |
| `-Force` | `--force` | No | `false` | Skip all confirmation prompts and force removal |

**Examples:**
```powershell
# PowerShell
.\remove-worktree.ps1 -WorktreePath "my-feature"
.\remove-worktree.ps1 -WorktreePath "my-feature" -Force
```
```bash
# Bash
./remove-worktree.sh my-feature
./remove-worktree.sh my-feature --force
```

---

### `switch-worktree` (.ps1 / .sh)

Switches the active branch on the current worktree, or creates a new branch.

**What it does:**
1. Checks for uncommitted changes — **refuses to switch if the working tree is dirty**
2. Optionally fetches from origin
3. Switches to an existing branch, or creates a new one
4. Automatically sets upstream tracking when checking out a remote-only branch
5. Shows the current branch and latest commit after switching

**Parameters:**

| PowerShell | Bash | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `-BranchName` | `<branch-name>` (positional) | Yes | — | Branch to switch to or create |
| `-New` | `--new` | No | `false` | Create a new branch instead of checking out an existing one |
| `-BaseBranch` | `--base-branch <b>` | No | `main` | Base branch when creating a new branch |
| `-Fetch` | `--fetch` | No | `false` | Fetch from origin before switching |

**Examples:**
```powershell
# PowerShell
.\switch-worktree.ps1 -BranchName "feat/PDJB-123/my-feature"
.\switch-worktree.ps1 -BranchName "feat/PDJB-456/new-work" -New -Fetch
.\switch-worktree.ps1 -BranchName "fix/PDJB-789/hotfix" -New -BaseBranch "test" -Fetch
```
```bash
# Bash
./switch-worktree.sh feat/PDJB-123/my-feature
./switch-worktree.sh feat/PDJB-456/new-work --new --fetch
./switch-worktree.sh fix/PDJB-789/hotfix --new --base-branch test --fetch
```

---

## Typical Workflow

```powershell
# PowerShell

# 1. Create a new worktree for your ticket
.\scripts\git-worktrees\new-worktree.ps1 -WorktreeName "pdjb-123" -BranchName "feat/PDJB-123/my-feature"

# 2. Work on your feature...
cd ..\pdjb-123

# 3. Need to switch to a different branch in this worktree?
.\scripts\git-worktrees\switch-worktree.ps1 -BranchName "feat/PDJB-456/other-work" -Fetch

# 4. Or start fresh on a new branch
.\scripts\git-worktrees\switch-worktree.ps1 -BranchName "feat/PDJB-789/new-task" -New -Fetch

# 5. When done, remove the worktree
.\scripts\git-worktrees\remove-worktree.ps1 -WorktreePath "pdjb-123"
```

```bash
# Bash

# 1. Create a new worktree for your ticket
./scripts/git-worktrees/new-worktree.sh pdjb-123 feat/PDJB-123/my-feature

# 2. Work on your feature...
cd ../pdjb-123

# 3. Need to switch to a different branch in this worktree?
./scripts/git-worktrees/switch-worktree.sh feat/PDJB-456/other-work --fetch

# 4. Or start fresh on a new branch
./scripts/git-worktrees/switch-worktree.sh feat/PDJB-789/new-task --new --fetch

# 5. When done, remove the worktree
./scripts/git-worktrees/remove-worktree.sh pdjb-123
```

## How Paths Work

These scripts resolve all paths dynamically so they work on any machine:

- **Main repo path** — resolved from the script's location (`$PSScriptRoot\..\..\` / `dirname "$0"/../../`)
- **Worktree base directory** — the parent folder of the repo (i.e. worktrees are created as siblings)

For example, if your repo is at `C:\Users\you\Work\MHCLG-PRSDB`, worktrees will be created in `C:\Users\you\Work\`.

## Config File Copying

The `new-worktree` script dynamically discovers which gitignored files need copying to the new worktree. It uses `git ls-files --others --ignored --exclude-standard` to find all gitignored files that exist on disk, then filters out build artifacts and IDE settings (e.g. `node_modules/`, `build/`, `.gradle/`, `.idea/`, `.vscode/`).

This means that when new gitignored config files are added to the repo (e.g. a new `.env` file or key), they are automatically picked up — no script changes needed.

**Files typically copied:** `.env`, `.github/copilot-instructions.md`, `.github/instructions/`, `*.pem` keys

**Directories excluded from copying:** `node_modules`, `build`, `.gradle`, `out`, `dist`, `bin`, `.idea`, `.vscode`, `.kotlin`, `.local-uploads`, `scripts/plausible/outputs`, and other build/IDE artifacts

## Notes

- IDE settings (`.idea/`, `.vscode/`) are **not** copied to new worktrees to avoid conflicts
- The `remove` script will never offer to delete the `main` or `test` branches
- The `switch` script must be run from inside a git worktree
