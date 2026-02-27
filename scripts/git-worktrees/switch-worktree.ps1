<#
.SYNOPSIS
    Switch the active branch on the current worktree.

.DESCRIPTION
    Switches the current worktree to an existing branch or creates a new one.
    Refuses to switch if there are uncommitted changes in the working tree.

.PARAMETER BranchName
    The name of the branch to switch to or create.

.PARAMETER New
    Create a new branch instead of checking out an existing one.

.PARAMETER BaseBranch
    The base branch to create from when using -New (default: main).

.PARAMETER Fetch
    Fetch from origin before switching branches.

.EXAMPLE
    .\switch-worktree.ps1 -BranchName "feat/PDJB-123/my-feature"

.EXAMPLE
    .\switch-worktree.ps1 -BranchName "feat/PDJB-456/new-work" -New

.EXAMPLE
    .\switch-worktree.ps1 -BranchName "fix/PDJB-789/hotfix" -New -BaseBranch "test" -Fetch

.EXAMPLE
    .\switch-worktree.ps1 -BranchName "feat/PDJB-123/my-feature" -Fetch
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$BranchName,

    [switch]$New,

    [string]$BaseBranch = "main",

    [switch]$Fetch
)

$ErrorActionPreference = "Stop"

# Check we're inside a git repository
$gitTopLevel = git rev-parse --show-toplevel 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Not inside a git repository. Run this script from within a worktree."
    exit 1
}

# Check for uncommitted changes
$status = git status --porcelain
if ($status) {
    Write-Host "Uncommitted changes detected — refusing to switch:" -ForegroundColor Red
    git status --short
    Write-Host "`nPlease commit, stash, or discard your changes before switching." -ForegroundColor Yellow
    exit 1
}

# Optional fetch
if ($Fetch) {
    Write-Host "Fetching latest from origin..." -ForegroundColor Cyan
    git fetch origin
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to fetch from origin."
        exit 1
    }
}

if ($New) {
    # Create a new branch
    if ($Fetch) {
        $startPoint = "origin/$BaseBranch"
    } else {
        $startPoint = $BaseBranch
    }

    Write-Host "Creating new branch '$BranchName' from $startPoint..." -ForegroundColor Cyan
    git checkout -b $BranchName $startPoint
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to create branch '$BranchName' from $startPoint."
        exit 1
    }
} else {
    # Switch to an existing branch
    $localExists = git branch --list $BranchName
    $remoteExists = git branch -r --list "origin/$BranchName"

    if (-not $localExists -and -not $remoteExists) {
        Write-Error "Branch '$BranchName' does not exist locally or on origin. Use -New to create it."
        exit 1
    }

    Write-Host "Switching to branch '$BranchName'..." -ForegroundColor Cyan
    git checkout $BranchName
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to checkout branch '$BranchName'."
        exit 1
    }

    # If branch was remote-only, set upstream tracking
    if (-not $localExists -and $remoteExists) {
        git branch --set-upstream-to=origin/$BranchName $BranchName
        Write-Host "Set upstream tracking to origin/$BranchName." -ForegroundColor Gray
    }
}

# Summary
Write-Host "`nSwitched successfully." -ForegroundColor Green
Write-Host "Branch: $(git rev-parse --abbrev-ref HEAD)" -ForegroundColor Cyan
git log --oneline -1
