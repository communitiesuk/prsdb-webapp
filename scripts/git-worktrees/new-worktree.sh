#!/bin/bash
# Create a new worktree with all necessary configuration files.
#
# Usage:
#   ./new-worktree.sh <worktree-name> <branch-name> [base-branch]
#
# Arguments:
#   worktree-name   Name for the new worktree folder (created as a sibling of the repo)
#   branch-name     Name of the new branch to create (e.g. "feat/PDJB-123/description")
#   base-branch     Branch to base from (default: main)
#
# Examples:
#   ./new-worktree.sh my-feature feat/PDJB-123/new-feature
#   ./new-worktree.sh bugfix fix/PDJB-456/bug-fix test

set -e

if [ $# -lt 2 ]; then
    echo "Usage: $0 <worktree-name> <branch-name> [base-branch]"
    exit 1
fi

WORKTREE_NAME="$1"
BRANCH_NAME="$2"
BASE_BRANCH="${3:-main}"

# Configuration - resolved relative to the script location
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAIN_REPO_PATH="$(cd "$SCRIPT_DIR/../.." && pwd)"
WORKTREE_BASE="$(dirname "$MAIN_REPO_PATH")"
NEW_WORKTREE_PATH="$WORKTREE_BASE/$WORKTREE_NAME"

# Validate main repo exists
if [ ! -d "$MAIN_REPO_PATH" ]; then
    echo "ERROR: Main repository not found at: $MAIN_REPO_PATH"
    exit 1
fi

# Validate worktree doesn't already exist
if [ -e "$NEW_WORKTREE_PATH" ]; then
    echo "ERROR: Path already exists: $NEW_WORKTREE_PATH"
    exit 1
fi

echo "Creating new worktree: $NEW_WORKTREE_PATH"
echo "Branch: $BRANCH_NAME (from $BASE_BRANCH)"

cd "$MAIN_REPO_PATH"

# Fetch latest from origin
echo ""
echo "Fetching latest from origin..."
if ! git fetch origin; then
    echo "ERROR: Failed to fetch from origin."
    exit 1
fi

# Check if branch already exists
LOCAL_EXISTS=$(git branch --list "$BRANCH_NAME")
REMOTE_EXISTS=$(git branch -r --list "origin/$BRANCH_NAME")

if [ -n "$LOCAL_EXISTS" ] || [ -n "$REMOTE_EXISTS" ]; then
    echo ""
    echo "Branch '$BRANCH_NAME' already exists."
    read -p "Create worktree using existing branch? (y/N) " CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        echo "Aborted."
        exit 1
    fi

    echo ""
    echo "Creating worktree with existing branch..."
    if [ -n "$LOCAL_EXISTS" ]; then
        if ! git worktree add "$NEW_WORKTREE_PATH" "$BRANCH_NAME"; then
            echo "ERROR: Failed to create worktree."
            exit 1
        fi
    else
        # Branch exists only on remote — create local tracking branch
        if ! git worktree add -b "$BRANCH_NAME" "$NEW_WORKTREE_PATH" "origin/$BRANCH_NAME"; then
            echo "ERROR: Failed to create worktree."
            exit 1
        fi
    fi
else
    # Create worktree with new branch
    echo ""
    echo "Creating worktree with new branch from origin/$BASE_BRANCH..."
    if ! git worktree add -b "$BRANCH_NAME" "$NEW_WORKTREE_PATH" "origin/$BASE_BRANCH"; then
        echo "ERROR: Failed to create worktree."
        exit 1
    fi
fi

echo "Worktree created successfully."

# Copy gitignored configuration files dynamically
echo ""
echo "Copying gitignored configuration files..."

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

cd "$MAIN_REPO_PATH"
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

    DEST="$NEW_WORKTREE_PATH/$file"
    DEST_DIR="$(dirname "$DEST")"
    mkdir -p "$DEST_DIR"
    cp "$MAIN_REPO_PATH/$file" "$DEST"
    echo "  Copied $file"
    COPIED_COUNT=$((COPIED_COUNT + 1))
done < <(git ls-files --others --ignored --exclude-standard)

if [ "$COPIED_COUNT" -gt 0 ]; then
    echo "Copied $COPIED_COUNT gitignored file(s)."
else
    echo "  No gitignored files found to copy."
fi

# Install npm dependencies
echo ""
echo "Installing npm dependencies..."
cd "$NEW_WORKTREE_PATH"
if npm install; then
    echo "npm install completed."
else
    echo "WARNING: npm install failed."
fi

# Success message
echo ""
echo "========================================"
echo "Worktree created successfully!"
echo "========================================"
echo "Path: $NEW_WORKTREE_PATH"
echo "Branch: $BRANCH_NAME"
echo ""
echo "Next steps:"
echo "  cd $NEW_WORKTREE_PATH"
echo "  Open in your IDE and start coding!"
