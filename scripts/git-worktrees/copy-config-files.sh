#!/bin/bash
# Copy gitignored configuration files from one worktree to another.
#
# Usage:
#   ./copy-config-files.sh <source-path> [destination-path]
#
# Arguments:
#   source-path       Path or name of the source worktree to copy files from.
#                     If a name is given (not a full path), assumes it is a
#                     sibling of the repo directory.
#   destination-path  Path or name of the destination worktree. Defaults to the
#                     current working directory. If a name is given, assumes it
#                     is a sibling of the repo directory.
#
# Examples:
#   ./copy-config-files.sh prsdb-webapp prsdb-webapp-2
#   ./copy-config-files.sh prsdb-webapp   # copies into current directory

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 <source-path> [destination-path]"
    exit 1
fi

SOURCE_INPUT="$1"
DEST_INPUT="${2:-$(pwd)}"

# Resolve paths relative to the script location
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAIN_REPO_PATH="$(cd "$SCRIPT_DIR/../.." && pwd)"
WORKTREE_BASE="$(dirname "$MAIN_REPO_PATH")"

# If not an absolute path, treat as a sibling name
if [[ "$SOURCE_INPUT" != /* ]]; then
    SOURCE_PATH="$WORKTREE_BASE/$SOURCE_INPUT"
else
    SOURCE_PATH="$SOURCE_INPUT"
fi

if [[ "$DEST_INPUT" != /* ]]; then
    DEST_PATH="$WORKTREE_BASE/$DEST_INPUT"
else
    DEST_PATH="$DEST_INPUT"
fi

# Validate paths exist
if [ ! -d "$SOURCE_PATH" ]; then
    echo "ERROR: Source path not found: $SOURCE_PATH"
    exit 1
fi
if [ ! -d "$DEST_PATH" ]; then
    echo "ERROR: Destination path not found: $DEST_PATH"
    exit 1
fi

echo "Copying gitignored config files..."
echo "  From: $SOURCE_PATH"
echo "  To:   $DEST_PATH"

# Directories to exclude from copying (build artifacts, IDE settings, etc.)
EXCLUDE_DIRS=(
    'node_modules' 'build' '.gradle' 'out' 'dist' 'bin'
    'nbproject' 'nbbuild' 'nbdist' '.nb-gradle'
    '.idea' '.vscode' '.kotlin' '.apt_generated'
    '.classpath' '.factorypath' '.project' '.settings' '.springBeans' '.sts4-cache'
    'scripts/plausible/outputs' 'scripts/plausible/saved'
    'scripts/plausible/processed_journey_data' 'scripts/plausible/userExperienceMetrics'
    'scripts/output' '.local-uploads'
)

cd "$SOURCE_PATH"
COPIED_COUNT=0

while IFS= read -r file; do
    [ -z "$file" ] && continue

    # Skip files in excluded directories
    SKIP=false
    for dir in "${EXCLUDE_DIRS[@]}"; do
        if [[ "$file" == "$dir/"* ]] || [[ "$file" == "$dir" ]]; then
            SKIP=true
            break
        fi
    done
    if $SKIP; then continue; fi

    DEST="$DEST_PATH/$file"

    DEST_DIR="$(dirname "$DEST")"
    mkdir -p "$DEST_DIR"
    cp "$SOURCE_PATH/$file" "$DEST"
    echo "  Copied $file"
    COPIED_COUNT=$((COPIED_COUNT + 1))
done < <(git ls-files --others --ignored --exclude-standard)

if [ "$COPIED_COUNT" -gt 0 ]; then
    echo "Copied $COPIED_COUNT file(s)."
else
    echo "  No gitignored files found to copy."
fi
