#!/usr/bin/env pwsh

$ErrorActionPreference = 'Stop'

$startMarker = '######## DATABASE-SCHEMA-HOOK START ########'
$endMarker = '####### DATABASE-SCHEMA-HOOK END #######'
$hookContent = @"
$startMarker

if ! git diff --cached --quiet --diff-filter=ACMRD -- src/main/resources/db/migrations; then
    echo "Staged database migrations changed; regenerating the database schema diagram"
    node scripts/generate_database_schema.js --staged-migrations || exit `$?
    git add docs/database-schema.mmd
else
    echo "No staged migration files; no need to regenerate schema diagram"
fi

$endMarker
"@

$hookPath = (git rev-parse --git-path hooks/pre-commit).Trim()
$hooksDir = Split-Path -Path $hookPath -Parent
if (-not (Test-Path -Path $hooksDir)) {
    New-Item -ItemType Directory -Path $hooksDir -Force | Out-Null
}

$existingContent = if (Test-Path -Path $hookPath) {
    Get-Content -Path $hookPath -Raw
} else {
    ''
}

if ($existingContent.Contains($startMarker)) {
    $pattern = [regex]::Escape($startMarker) + '.*?' + [regex]::Escape($endMarker)
    $newContent = [regex]::Replace($existingContent, $pattern, $hookContent, [System.Text.RegularExpressions.RegexOptions]::Singleline)
} elseif ($existingContent -match '^#![^\n]*(\r?\n)') {
    $lineBreak = if ($existingContent.Contains("`r`n")) { "`r`n" } else { "`n" }
    $firstLineBreakIndex = $existingContent.IndexOf("`n")
    $prefix = $existingContent.Substring(0, $firstLineBreakIndex + 1)
    $rest = $existingContent.Substring($firstLineBreakIndex + 1)
    $newContent = $prefix + $lineBreak + $hookContent + $lineBreak + $lineBreak + $rest
} elseif ([string]::IsNullOrEmpty($existingContent)) {
    $newContent = "#!/bin/sh`n`n$hookContent`n"
} else {
    $newContent = "$hookContent`n`n$existingContent"
}

Set-Content -Path $hookPath -Value $newContent -NoNewline
Write-Host "Installed database schema pre-commit hook at $hookPath"
