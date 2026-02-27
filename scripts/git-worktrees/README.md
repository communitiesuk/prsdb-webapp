# Git Worktree Management Scripts

## What are git worktrees?

Git worktrees let you check out multiple branches of the same repository simultaneously, each in its own directory. Instead of stashing or committing work-in-progress to switch branches, you can have separate working copies side by side — for example, one for your current feature and another for a code review or hotfix. All worktrees share the same `.git` history, so commits, branches, and stashes are visible across them.

See the [official documentation](https://git-scm.com/docs/git-worktree) for more details.

## Overview

PowerShell scripts for managing git worktrees in this repository. Worktrees are created as siblings of the repo directory, and all paths are resolved dynamically so these scripts work for any developer.

## Prerequisites

- PowerShell 5.1+ (or PowerShell Core)
- Git

## Scripts

### `new-worktree.ps1`

Creates a new git worktree with all necessary configuration files and dependencies.

**What it does:**
1. Fetches latest from origin
2. Creates a new worktree with a new branch (or prompts to use an existing branch)
3. Copies gitignored configuration files from the main repo:
   - `.env`
   - `.github/copilot-instructions.md`
   - `.github/instructions/` directory
   - `src/main/resources/private_key.pem`
   - `src/main/resources/public_key.pem`
4. Runs `npm install` to set up frontend dependencies

**Parameters:**

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `-WorktreeName` | Yes | — | Name for the new worktree folder (created as a sibling of the repo) |
| `-BranchName` | Yes | — | Name of the branch to create (e.g. `feat/PDJB-123/description`) |
| `-BaseBranch` | No | `main` | Branch to base the new worktree on |

**Examples:**
```powershell
# Create a new feature worktree
.\new-worktree.ps1 -WorktreeName "my-feature" -BranchName "feat/PDJB-123/new-feature"

# Create from the test branch instead of main
.\new-worktree.ps1 -WorktreeName "bugfix" -BranchName "fix/PDJB-456/bug-fix" -BaseBranch "test"
```

---

### `remove-worktree.ps1`

Removes a git worktree and optionally deletes the associated local branch.

**What it does:**
1. Checks for uncommitted changes (warns before proceeding)
2. Confirms removal with the user (unless `-Force` is used)
3. Removes the worktree via `git worktree remove`
4. Prunes stale worktree references
5. Optionally deletes the local branch (never deletes `main` or `test`)

**Parameters:**

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `-WorktreePath` | Yes | — | Path or name of the worktree to remove. If a name is given, assumes it's a sibling of the repo directory |
| `-Force` | No | `false` | Skip all confirmation prompts and force removal |

**Examples:**
```powershell
# Remove a worktree by name
.\remove-worktree.ps1 -WorktreePath "my-feature"

# Remove with a full path
.\remove-worktree.ps1 -WorktreePath "C:\Users\dev\Work\my-feature"

# Force remove without any prompts
.\remove-worktree.ps1 -WorktreePath "my-feature" -Force
```

---

### `switch-worktree.ps1`

Switches the active branch on the current worktree, or creates a new branch.

**What it does:**
1. Checks for uncommitted changes — **refuses to switch if the working tree is dirty**
2. Optionally fetches from origin (with `-Fetch`)
3. Switches to an existing branch, or creates a new one (with `-New`)
4. Automatically sets upstream tracking when checking out a remote-only branch
5. Shows the current branch and latest commit after switching

**Parameters:**

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `-BranchName` | Yes | — | Branch to switch to or create |
| `-New` | No | `false` | Create a new branch instead of checking out an existing one |
| `-BaseBranch` | No | `main` | Base branch when creating a new branch (used with `-New`) |
| `-Fetch` | No | `false` | Fetch from origin before switching |

**Examples:**
```powershell
# Switch to an existing branch
.\switch-worktree.ps1 -BranchName "feat/PDJB-123/my-feature"

# Switch to an existing branch after fetching latest
.\switch-worktree.ps1 -BranchName "feat/PDJB-123/my-feature" -Fetch

# Create a new branch from main
.\switch-worktree.ps1 -BranchName "feat/PDJB-456/new-work" -New

# Create a new branch from test, fetching first
.\switch-worktree.ps1 -BranchName "fix/PDJB-789/hotfix" -New -BaseBranch "test" -Fetch
```

---

## Typical Workflow

```powershell
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

## How Paths Work

These scripts resolve all paths dynamically so they work on any machine:

- **Main repo path** — resolved from the script's location (`$PSScriptRoot\..\..\`)
- **Worktree base directory** — the parent folder of the repo (i.e. worktrees are created as siblings)

For example, if your repo is at `C:\Users\you\Work\MHCLG-PRSDB`, worktrees will be created in `C:\Users\you\Work\`.

## Notes

- IDE settings (`.idea/`, `.vscode/`) are **not** copied to new worktrees to avoid conflicts
- The `remove` script will never offer to delete the `main` or `test` branches
- The `switch` script must be run from inside a git worktree
