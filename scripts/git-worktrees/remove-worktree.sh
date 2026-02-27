#!/bin/bash
# Remove a worktree and optionally delete the associated branch.
#
# Usage:
#   ./remove-worktree.sh <worktree-path> [--force]
#
# Arguments:
#   worktree-path   Path or name of the worktree to remove. If a name is given,
#                   assumes it's a sibling of the repo directory.
#   --force         Skip confirmation prompts and force removal.
#
# Examples:
#   ./remove-worktree.sh my-feature
#   ./remove-worktree.sh /home/dev/Work/my-feature --force

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 <worktree-path> [--force]"
    exit 1
fi

WORKTREE_PATH="$1"
FORCE=false

for arg in "$@"; do
    if [ "$arg" = "--force" ]; then
        FORCE=true
    fi
done

# Configuration - resolved relative to the script location
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAIN_REPO_PATH="$(cd "$SCRIPT_DIR/../.." && pwd)"
WORKTREE_BASE="$(dirname "$MAIN_REPO_PATH")"

# Resolve worktree path
if [[ "$WORKTREE_PATH" != /* ]]; then
    WORKTREE_PATH="$WORKTREE_BASE/$WORKTREE_PATH"
fi

# Validate main repo exists
if [ ! -d "$MAIN_REPO_PATH" ]; then
    echo "ERROR: Main repository not found at: $MAIN_REPO_PATH"
    exit 1
fi

# Validate worktree exists
if [ ! -d "$WORKTREE_PATH" ]; then
    echo "ERROR: Worktree path does not exist: $WORKTREE_PATH"
    exit 1
fi

# Check it's not the main repo
RESOLVED_WORKTREE="$(cd "$WORKTREE_PATH" && pwd)"
RESOLVED_MAIN="$(cd "$MAIN_REPO_PATH" && pwd)"
if [ "$RESOLVED_WORKTREE" = "$RESOLVED_MAIN" ]; then
    echo "ERROR: Cannot remove the main repository!"
    exit 1
fi

echo "Removing worktree: $WORKTREE_PATH"

# Get the branch name from the worktree
BRANCH_NAME=""
cd "$WORKTREE_PATH"
BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || true)
if [ "$BRANCH_NAME" = "HEAD" ]; then
    BRANCH_NAME=""
    echo "Worktree is in detached HEAD state."
else
    echo "Branch: $BRANCH_NAME"
fi

# Check for uncommitted changes
STATUS=$(git status --porcelain)
if [ -n "$STATUS" ]; then
    echo ""
    echo "Uncommitted changes detected:"
    git status --short

    if ! $FORCE; then
        read -p "Proceed anyway? Changes will be lost! (y/N) " CONFIRM
        if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
            echo "Aborted."
            exit 1
        fi
    fi
fi

# Confirm removal
if ! $FORCE; then
    read -p "Remove worktree at '$WORKTREE_PATH'? (y/N) " CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        echo "Aborted."
        exit 1
    fi
fi

# Remove the worktree
echo ""
echo "Removing worktree..."
cd "$MAIN_REPO_PATH"

if $FORCE; then
    if ! git worktree remove "$WORKTREE_PATH" --force; then
        echo "ERROR: Failed to remove worktree."
        exit 1
    fi
else
    if ! git worktree remove "$WORKTREE_PATH"; then
        echo "ERROR: Failed to remove worktree."
        exit 1
    fi
fi
echo "Worktree removed."

# Prune stale worktree references
echo "Pruning stale worktree references..."
if git worktree prune; then
    echo "Prune completed."
else
    echo "WARNING: Failed to prune worktree references."
fi

# Ask about branch deletion
if [ -n "$BRANCH_NAME" ] && [ "$BRANCH_NAME" != "main" ] && [ "$BRANCH_NAME" != "test" ]; then
    read -p "Delete local branch '$BRANCH_NAME'? (y/N) " DELETE_BRANCH
    if [ "$DELETE_BRANCH" = "y" ] || [ "$DELETE_BRANCH" = "Y" ]; then
        if git branch -D "$BRANCH_NAME"; then
            echo "Branch '$BRANCH_NAME' deleted."
        else
            echo "ERROR: Failed to delete branch '$BRANCH_NAME'."
            exit 1
        fi
    else
        echo "Branch '$BRANCH_NAME' kept."
    fi
fi

# Success message
echo ""
echo "========================================"
echo "Worktree removed successfully!"
echo "========================================"
