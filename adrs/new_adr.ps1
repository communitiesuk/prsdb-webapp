# Define the ADR template and directory
$template = "0000-adr-template.md"
$adrDir = "."

# Check if the template file exists
if (-not (Test-Path "$adrDir\$template")) {
    Write-Host "Template file '$template' not found!"
    exit 1
}

# Check if the title is provided
if ($args.Count -eq 0) {
    Write-Host "Usage: ./new_adr_lowercase.ps1 'Title of the ADR'"
    exit 1
}

# Set the title from the first argument
$title = $args[0]

# Convert the title to a safe filename by replacing spaces with dashes and making it lowercase
$safeTitle = ($title -replace '\s+', '-').ToLower()

# Find the next available ADR number
$existingAdrs = Get-ChildItem "$adrDir\*.md" |
    ForEach-Object {
        # Extract the first 4 digits, and remove leading zeros by casting to [int]
        [int]($_.BaseName -replace '^(\d{4}).*', '$1')
    } |
    Sort-Object

if ($existingAdrs.Count -eq 0) {
    # If no ADRs exist, start at 1
    $nextNumber = 1
    Write-Host "No existing ADRs found. Starting at 1."
} else {
    # Otherwise, get the highest ADR number and increment it
    $nextNumber = $existingAdrs[-1] + 1
}

# Zero-pad the ADR number to four digits
$paddedNumber = $nextNumber.ToString("0000")

# Create the new ADR filename (in lowercase)
$adrFilename = "$adrDir\$($paddedNumber.ToLower())-$safeTitle.md"

# Copy the template to the new ADR file
Copy-Item "$adrDir\$template" "$adrFilename"

# Update the first line of the new ADR with the padded number and title
(Get-Content "$adrFilename") |
    ForEach-Object {
        if ($_ -match "^# ADR-0000: ADR Template") {
            "# ADR-${paddedNumber}: $title"
        } else {
            $_
        }
    } | Set-Content "$adrFilename"

# Confirmation message
Write-Host "ADR created successfully: $adrFilename"
