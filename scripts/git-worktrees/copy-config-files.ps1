<#
.SYNOPSIS
    Copy gitignored configuration files from one worktree to another.

.DESCRIPTION
    Copies gitignored configuration files (e.g. .env, .pem keys) from a source
    worktree to a destination worktree. Useful when you have manually created a
    worktree (e.g. via `git clone` or `git worktree add`) and need to replicate
    the configuration from an existing workspace.

    Build artifacts and IDE settings are excluded automatically.

.PARAMETER SourcePath
    Path to the source worktree to copy files from. If a name is given (not a
    full path), assumes it is a sibling of the repo directory.

.PARAMETER DestinationPath
    Path to the destination worktree to copy files to. Defaults to the current
    working directory. If a name is given (not a full path), assumes it is a
    sibling of the repo directory.

.EXAMPLE
    .\copy-config-files.ps1 -SourcePath "prsdb-webapp" -DestinationPath "prsdb-webapp-2"

.EXAMPLE
    # Copy from a sibling worktree into the current directory
    .\copy-config-files.ps1 -SourcePath "prsdb-webapp"
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$SourcePath,

    [string]$DestinationPath = (Get-Location).Path
)

$ErrorActionPreference = "Stop"

# Resolve paths — if not rooted, treat as sibling of the repo directory
$mainRepoPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$workTreeBase = Split-Path $mainRepoPath -Parent

if (-not [System.IO.Path]::IsPathRooted($SourcePath)) {
    $SourcePath = Join-Path $workTreeBase $SourcePath
}
if (-not [System.IO.Path]::IsPathRooted($DestinationPath)) {
    $DestinationPath = Join-Path $workTreeBase $DestinationPath
}

# Validate paths exist
if (-not (Test-Path $SourcePath)) {
    Write-Error "Source path not found: $SourcePath"
    exit 1
}
if (-not (Test-Path $DestinationPath)) {
    Write-Error "Destination path not found: $DestinationPath"
    exit 1
}

Write-Host "Copying gitignored config files..." -ForegroundColor Cyan
Write-Host "  From: $SourcePath" -ForegroundColor Gray
Write-Host "  To:   $DestinationPath" -ForegroundColor Gray

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

Push-Location $SourcePath
try {
    $ignoredFiles = git ls-files --others --ignored --exclude-standard
    if ($ignoredFiles) {
        $copiedCount = 0
        $skippedCount = 0
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

            $source = Join-Path $SourcePath $file
            $dest = Join-Path $DestinationPath $file

            # Skip if destination already has the file
            if (Test-Path $dest) {
                $skippedCount++
                continue
            }

            $destDir = Split-Path $dest -Parent
            if (-not (Test-Path $destDir)) {
                New-Item -ItemType Directory -Path $destDir -Force | Out-Null
            }
            Copy-Item $source $dest
            Write-Host "  Copied $file" -ForegroundColor Gray
            $copiedCount++
        }
        Write-Host "Copied $copiedCount file(s), skipped $skippedCount already-existing file(s)." -ForegroundColor Green
    } else {
        Write-Host "  No gitignored files found to copy." -ForegroundColor Yellow
    }
} finally {
    Pop-Location
}
