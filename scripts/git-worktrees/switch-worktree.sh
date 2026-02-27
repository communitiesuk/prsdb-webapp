#!/bin/bash
# Switch the active branch on the current worktree.
#
# Usage:
#   ./switch-worktree.sh <branch-name> [--new] [--base-branch <branch>] [--fetch]
#
# Arguments:
#   branch-name         Branch to switch to or create
#   --new               Create a new branch instead of checking out an existing one
#   --base-branch <b>   Base branch when creating a new branch (default: main)
#   --fetch             Fetch from origin before switching
#
# Examples:
#   ./switch-worktree.sh feat/PDJB-123/my-feature
#   ./switch-worktree.sh feat/PDJB-456/new-work --new
#   ./switch-worktree.sh fix/PDJB-789/hotfix --new --base-branch test --fetch
#   ./switch-worktree.sh feat/PDJB-123/my-feature --fetch

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 <branch-name> [--new] [--base-branch <branch>] [--fetch]"
    exit 1
fi

BRANCH_NAME="$1"
shift

CREATE_NEW=false
BASE_BRANCH="main"
DO_FETCH=false

while [ $# -gt 0 ]; do
    case "$1" in
        --new)
            CREATE_NEW=true
            shift
            ;;
        --base-branch)
            BASE_BRANCH="$2"
            shift 2
            ;;
        --fetch)
            DO_FETCH=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Check we're inside a git repository
if ! git rev-parse --show-toplevel > /dev/null 2>&1; then
    echo "ERROR: Not inside a git repository. Run this script from within a worktree."
    exit 1
fi

# Check for uncommitted changes
STATUS=$(git status --porcelain)
if [ -n "$STATUS" ]; then
    echo "Uncommitted changes detected — refusing to switch:"
    git status --short
    echo ""
    echo "Please commit, stash, or discard your changes before switching."
    exit 1
fi

# Optional fetch
if $DO_FETCH; then
    echo "Fetching latest from origin..."
    if ! git fetch origin; then
        echo "ERROR: Failed to fetch from origin."
        exit 1
    fi
fi

if $CREATE_NEW; then
    # Create a new branch
    if $DO_FETCH; then
        START_POINT="origin/$BASE_BRANCH"
    else
        START_POINT="$BASE_BRANCH"
    fi

    echo "Creating new branch '$BRANCH_NAME' from $START_POINT..."
    if ! git checkout -b "$BRANCH_NAME" "$START_POINT"; then
        echo "ERROR: Failed to create branch '$BRANCH_NAME' from $START_POINT."
        exit 1
    fi
else
    # Switch to an existing branch
    LOCAL_EXISTS=$(git branch --list "$BRANCH_NAME")
    REMOTE_EXISTS=$(git branch -r --list "origin/$BRANCH_NAME")

    if [ -z "$LOCAL_EXISTS" ] && [ -z "$REMOTE_EXISTS" ]; then
        echo "ERROR: Branch '$BRANCH_NAME' does not exist locally or on origin. Use --new to create it."
        exit 1
    fi

    echo "Switching to branch '$BRANCH_NAME'..."
    if ! git checkout "$BRANCH_NAME"; then
        echo "ERROR: Failed to checkout branch '$BRANCH_NAME'."
        exit 1
    fi

    # If branch was remote-only, set upstream tracking
    if [ -z "$LOCAL_EXISTS" ] && [ -n "$REMOTE_EXISTS" ]; then
        git branch --set-upstream-to="origin/$BRANCH_NAME" "$BRANCH_NAME"
        echo "Set upstream tracking to origin/$BRANCH_NAME."
    fi
fi

# Summary
echo ""
echo "Switched successfully."
echo "Branch: $(git rev-parse --abbrev-ref HEAD)"
git log --oneline -1
