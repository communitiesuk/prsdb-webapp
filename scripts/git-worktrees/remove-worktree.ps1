<#
.SYNOPSIS
    Remove a worktree and optionally delete the associated branch.

.DESCRIPTION
    This script automates the removal of a git worktree for the repository.
    It removes the worktree, prunes stale references, and optionally deletes the local branch.
    Paths are resolved relative to the repo root, so this works for any developer.

.PARAMETER WorktreePath
    The path or name of the worktree to remove. If just a name is provided,
    assumes it's a sibling of the repo directory.

.PARAMETER Force
    Skip confirmation prompts and force removal even with uncommitted changes.

.EXAMPLE
    .\remove-worktree.ps1 -WorktreePath "my-feature"

.EXAMPLE
    .\remove-worktree.ps1 -WorktreePath "C:\Users\dev\Work\my-feature" -Force
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$WorktreePath,
    
    [switch]$Force
)

$ErrorActionPreference = "Stop"

# Configuration - resolved relative to the script location
$mainRepoPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$workTreeBase = Split-Path $mainRepoPath -Parent

# Resolve worktree path
if (-not [System.IO.Path]::IsPathRooted($WorktreePath)) {
    $WorktreePath = Join-Path $workTreeBase $WorktreePath
}

# Validate main repo exists
if (-not (Test-Path $mainRepoPath)) {
    Write-Error "Main repository not found at: $mainRepoPath"
    exit 1
}

# Validate worktree exists
if (-not (Test-Path $WorktreePath)) {
    Write-Error "Worktree path does not exist: $WorktreePath"
    exit 1
}

# Check it's not the main repo
if ((Resolve-Path $WorktreePath).Path -eq (Resolve-Path $mainRepoPath).Path) {
    Write-Error "Cannot remove the main repository!"
    exit 1
}

Write-Host "Removing worktree: $WorktreePath" -ForegroundColor Cyan

# Get the branch name from the worktree
$branchName = $null
Push-Location $WorktreePath
try {
    $branchName = git rev-parse --abbrev-ref HEAD 2>$null
    if ($branchName -eq "HEAD") {
        $branchName = $null
        Write-Host "Worktree is in detached HEAD state." -ForegroundColor Yellow
    } else {
        Write-Host "Branch: $branchName" -ForegroundColor Cyan
    }
    
    # Check for uncommitted changes
    $status = git status --porcelain
    if ($status) {
        Write-Host "`nUncommitted changes detected:" -ForegroundColor Yellow
        git status --short
        
        if (-not $Force) {
            $confirm = Read-Host "`nProceed anyway? Changes will be lost! (y/N)"
            if ($confirm -ne 'y' -and $confirm -ne 'Y') {
                Write-Host "Aborted." -ForegroundColor Red
                exit 1
            }
        }
    }
} finally {
    Pop-Location
}

# Confirm removal
if (-not $Force) {
    $confirm = Read-Host "`nRemove worktree at '$WorktreePath'? (y/N)"
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "Aborted." -ForegroundColor Red
        exit 1
    }
}

# Remove the worktree
Write-Host "`nRemoving worktree..." -ForegroundColor Cyan
Push-Location $mainRepoPath
try {
    if ($Force) {
        git worktree remove $WorktreePath --force
    } else {
        git worktree remove $WorktreePath
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to remove worktree."
        exit 1
    }
    Write-Host "Worktree removed." -ForegroundColor Green
    
    # Prune stale worktree references
    Write-Host "Pruning stale worktree references..." -ForegroundColor Cyan
    git worktree prune
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Warning: Failed to prune worktree references." -ForegroundColor Yellow
    } else {
        Write-Host "Prune completed." -ForegroundColor Green
    }
    
    # Ask about branch deletion
    if ($branchName -and $branchName -ne "main" -and $branchName -ne "test") {
        $deleteBranch = Read-Host "`nDelete local branch '$branchName'? (y/N)"
        if ($deleteBranch -eq 'y' -or $deleteBranch -eq 'Y') {
            git branch -D $branchName
            if ($LASTEXITCODE -ne 0) {
                Write-Error "Failed to delete branch '$branchName'."
                exit 1
            }
            Write-Host "Branch '$branchName' deleted." -ForegroundColor Green
        } else {
            Write-Host "Branch '$branchName' kept." -ForegroundColor Gray
        }
    }
    
} finally {
    Pop-Location
}

# Success message
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Worktree removed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
