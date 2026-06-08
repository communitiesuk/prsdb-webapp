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

            $destDir = Split-Path $dest -Parent
            if (-not (Test-Path $destDir)) {
                New-Item -ItemType Directory -Path $destDir -Force | Out-Null
            }
            Copy-Item $source $dest
            Write-Host "  Copied $file" -ForegroundColor Gray
            $copiedCount++
        }
        Write-Host "Copied $copiedCount file(s)." -ForegroundColor Green
    } else {
        Write-Host "  No gitignored files found to copy." -ForegroundColor Yellow
    }
} finally {
    Pop-Location
}

# Assign unique ports for parallel worktree execution.
# Derive the offset from the highest SERVER_PORT found in sibling worktrees' .env files,
# rather than worktree count, to avoid port collisions after worktree removal.
$maxOffset = 0
$siblingEnvFiles = Get-ChildItem -Path $workTreeBase -Filter ".env" -Recurse -Depth 1 -ErrorAction SilentlyContinue
foreach ($siblingEnv in $siblingEnvFiles) {
    if ($siblingEnv.FullName -eq (Join-Path $DestinationPath ".env")) { continue }
    $portLine = Get-Content $siblingEnv.FullName | Where-Object { $_ -match '^SERVER_PORT=' } | Select-Object -First 1
    if ($portLine -match '(\d+)') {
        $siblingPort = [int]$Matches[1]
        $offset = $siblingPort - 8080
        if ($offset -gt $maxOffset) { $maxOffset = $offset }
    }
}
$portOffset = $maxOffset + 1

$newServerPort = 8080 + $portOffset
$newPostgresPort = 5433 + $portOffset
$newRedisPort = 6379 + $portOffset

$envFilePath = Join-Path $DestinationPath ".env"
if (Test-Path $envFilePath) {
    Write-Host "`nAssigning unique ports for parallel execution (offset: $portOffset)..." -ForegroundColor Cyan
    $content = Get-Content $envFilePath -Raw
    $content = $content -replace 'SERVER_PORT="8080"', "SERVER_PORT=`"$newServerPort`""
    $content = $content -replace 'POSTGRES_PORT="5433"', "POSTGRES_PORT=`"$newPostgresPort`""
    $content = $content -replace 'REDIS_PORT="6379"', "REDIS_PORT=`"$newRedisPort`""
    $content = $content -replace 'RDS_URL="jdbc:postgresql://localhost:5433/prsdblocal"', "RDS_URL=`"jdbc:postgresql://localhost:${newPostgresPort}/prsdblocal`""
    $content = $content -replace 'ELASTICACHE_PORT="6379"', "ELASTICACHE_PORT=`"$newRedisPort`""
    $content = $content -replace 'LANDLORD_BASE_URL="http://localhost:8080/landlord"', "LANDLORD_BASE_URL=`"http://localhost:${newServerPort}/landlord`""
    $content = $content -replace 'LOCAL_AUTHORITY_BASE_URL="http://localhost:8080/local-council"', "LOCAL_AUTHORITY_BASE_URL=`"http://localhost:${newServerPort}/local-council`""
    Set-Content -Path $envFilePath -Value $content -NoNewline
    Write-Host "  SERVER_PORT=$newServerPort" -ForegroundColor Gray
    Write-Host "  POSTGRES_PORT=$newPostgresPort" -ForegroundColor Gray
    Write-Host "  REDIS_PORT=$newRedisPort" -ForegroundColor Gray
}
