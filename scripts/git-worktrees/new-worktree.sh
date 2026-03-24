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

# Assign unique ports for parallel worktree execution.
# Derive the offset from the highest SERVER_PORT found in sibling worktrees' .env files,
# rather than worktree count, to avoid port collisions after worktree removal.
MAX_OFFSET=0
for SIBLING_ENV in "$WORKTREE_BASE"/*/.env; do
    [ -f "$SIBLING_ENV" ] || continue
    [ "$SIBLING_ENV" = "$NEW_WORKTREE_PATH/.env" ] && continue
    SIBLING_PORT=$(grep -E '^SERVER_PORT=' "$SIBLING_ENV" 2>/dev/null | sed -E 's/^[^0-9]*([0-9]+).*/\1/' || true)
    if [ -n "$SIBLING_PORT" ] && [ "$SIBLING_PORT" -ge 8080 ] 2>/dev/null; then
        OFFSET=$((SIBLING_PORT - 8080))
        [ "$OFFSET" -gt "$MAX_OFFSET" ] && MAX_OFFSET=$OFFSET
    fi
done
PORT_OFFSET=$((MAX_OFFSET + 1))

NEW_SERVER_PORT=$((8080 + PORT_OFFSET))
NEW_POSTGRES_PORT=$((5433 + PORT_OFFSET))
NEW_REDIS_PORT=$((6379 + PORT_OFFSET))

ENV_FILE="$NEW_WORKTREE_PATH/.env"
if [ -f "$ENV_FILE" ]; then
    echo ""
    echo "Assigning unique ports for parallel execution (offset: $PORT_OFFSET)..."
    sed \
        -e "s/SERVER_PORT=\"8080\"/SERVER_PORT=\"$NEW_SERVER_PORT\"/" \
        -e "s/POSTGRES_PORT=\"5433\"/POSTGRES_PORT=\"$NEW_POSTGRES_PORT\"/" \
        -e "s/REDIS_PORT=\"6379\"/REDIS_PORT=\"$NEW_REDIS_PORT\"/" \
        -e "s|RDS_URL=\"jdbc:postgresql://localhost:5433/prsdblocal\"|RDS_URL=\"jdbc:postgresql://localhost:$NEW_POSTGRES_PORT/prsdblocal\"|" \
        -e "s/ELASTICACHE_PORT=\"6379\"/ELASTICACHE_PORT=\"$NEW_REDIS_PORT\"/" \
        -e "s|LANDLORD_BASE_URL=\"http://localhost:8080/landlord\"|LANDLORD_BASE_URL=\"http://localhost:$NEW_SERVER_PORT/landlord\"|" \
        -e "s|LOCAL_AUTHORITY_BASE_URL=\"http://localhost:8080/local-council\"|LOCAL_AUTHORITY_BASE_URL=\"http://localhost:$NEW_SERVER_PORT/local-council\"|" \
        "$ENV_FILE" > "$ENV_FILE.tmp" && mv "$ENV_FILE.tmp" "$ENV_FILE"
    echo "  SERVER_PORT=$NEW_SERVER_PORT"
    echo "  POSTGRES_PORT=$NEW_POSTGRES_PORT"
    echo "  REDIS_PORT=$NEW_REDIS_PORT"
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
