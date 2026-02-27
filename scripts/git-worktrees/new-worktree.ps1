<#
.SYNOPSIS
    Create a new worktree with all necessary configuration files.

.DESCRIPTION
    This script automates the creation of a new git worktree for the repository.
    It creates the worktree, copies gitignored configuration files, and installs npm dependencies.
    Paths are resolved relative to the repo root, so this works for any developer.

.PARAMETER WorktreeName
    The name for the new worktree folder (created as a sibling of the repo).

.PARAMETER BranchName
    The name of the new branch to create (e.g., "feat/PDJB-123/description").

.PARAMETER BaseBranch
    The base branch to create from (default: main).

.EXAMPLE
    .\new-worktree.ps1 -WorktreeName "my-feature" -BranchName "feat/PDJB-123/new-feature"

.EXAMPLE
    .\new-worktree.ps1 -WorktreeName "bugfix" -BranchName "fix/PDJB-456/bug-fix" -BaseBranch "test"
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$WorktreeName,
    
    [Parameter(Mandatory=$true)]
    [string]$BranchName,
    
    [string]$BaseBranch = "main"
)

$ErrorActionPreference = "Stop"

# Configuration - resolved relative to the script location
$mainRepoPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$workTreeBase = Split-Path $mainRepoPath -Parent
$newWorktreePath = Join-Path $workTreeBase $WorktreeName

# Validate main repo exists
if (-not (Test-Path $mainRepoPath)) {
    Write-Error "Main repository not found at: $mainRepoPath"
    exit 1
}

# Validate worktree doesn't already exist
if (Test-Path $newWorktreePath) {
    Write-Error "Path already exists: $newWorktreePath"
    exit 1
}

Write-Host "Creating new worktree: $newWorktreePath" -ForegroundColor Cyan
Write-Host "Branch: $BranchName (from $BaseBranch)" -ForegroundColor Cyan

Push-Location $mainRepoPath
try {
    # Fetch latest from origin
    Write-Host "`nFetching latest from origin..." -ForegroundColor Cyan
    git fetch origin
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to fetch from origin."
        exit 1
    }
    
    # Check if branch already exists
    $branchExists = git branch --list $BranchName
    $remoteBranchExists = git branch -r --list "origin/$BranchName"
    
    if ($branchExists -or $remoteBranchExists) {
        Write-Host "`nBranch '$BranchName' already exists." -ForegroundColor Yellow
        $useExisting = Read-Host "Create worktree using existing branch? (y/N)"
        if ($useExisting -ne 'y' -and $useExisting -ne 'Y') {
            Write-Host "Aborted." -ForegroundColor Red
            exit 1
        }
        
        # Create worktree with existing branch
        Write-Host "`nCreating worktree with existing branch..." -ForegroundColor Cyan
        if ($branchExists) {
            git worktree add $newWorktreePath $BranchName
        } else {
            # Branch exists only on remote — create local tracking branch
            git worktree add -b $BranchName $newWorktreePath "origin/$BranchName"
        }
    } else {
        # Create worktree with new branch
        Write-Host "`nCreating worktree with new branch from origin/$BaseBranch..." -ForegroundColor Cyan
        git worktree add -b $BranchName $newWorktreePath "origin/$BaseBranch"
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to create worktree."
        exit 1
    }
    Write-Host "Worktree created successfully." -ForegroundColor Green
    
} finally {
    Pop-Location
}

# Copy gitignored configuration files dynamically
Write-Host "`nCopying gitignored configuration files..." -ForegroundColor Cyan

# Directories to exclude from copying (build artifacts, IDE settings, etc.)
$excludeDirs = @(
    'node_modules', 'build', '.gradle', 'out', 'dist', 'bin',
    'nbproject', 'nbbuild', 'nbdist', '.nb-gradle',
    '.idea', '.vscode', '.kotlin', '.apt_generated',
    '.classpath', '.factorypath', '.project', '.settings', '.springBeans', '.sts4-cache',
    'scripts/plausible/outputs', 'scripts/plausible/saved',
    'scripts/plausible/processed_journey_data', 'scripts/plausible/userExperienceMetrics',
    'scripts/output', '.local-uploads'
)

Push-Location $mainRepoPath
try {
    $ignoredFiles = git ls-files --others --ignored --exclude-standard
    if ($ignoredFiles) {
        $copiedCount = 0
        foreach ($file in $ignoredFiles) {
            # Skip files in excluded directories
            $skip = $false
            foreach ($dir in $excludeDirs) {
                if ($file -like "$dir/*" -or $file -eq $dir) {
                    $skip = $true
                    break
                }
            }
            if ($skip) { continue }

            $source = Join-Path $mainRepoPath $file
            $dest = Join-Path $newWorktreePath $file
            $destDir = Split-Path $dest -Parent
            if (-not (Test-Path $destDir)) {
                New-Item -ItemType Directory -Path $destDir -Force | Out-Null
            }
            Copy-Item $source $dest
            Write-Host "  Copied $file" -ForegroundColor Gray
            $copiedCount++
        }
        Write-Host "Copied $copiedCount gitignored file(s)." -ForegroundColor Green
    } else {
        Write-Host "  No gitignored files found to copy." -ForegroundColor Yellow
    }
} finally {
    Pop-Location
}

# Install npm dependencies
Write-Host "`nInstalling npm dependencies..." -ForegroundColor Cyan
Push-Location $newWorktreePath
try {
    npm install
    Write-Host "npm install completed." -ForegroundColor Green
} finally {
    Pop-Location
}

# Success message
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Worktree created successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Path: $newWorktreePath" -ForegroundColor Cyan
Write-Host "Branch: $BranchName" -ForegroundColor Cyan
Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "  cd $newWorktreePath" -ForegroundColor Gray
Write-Host "  Open in your IDE and start coding!" -ForegroundColor Gray
