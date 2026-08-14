#!/bin/bash

set -euo pipefail

START_MARKER='######## DATABASE-SCHEMA-HOOK START ########'
END_MARKER='####### DATABASE-SCHEMA-HOOK END #######'
HOOK_CONTENT="${START_MARKER}

if ! git diff --cached --quiet --diff-filter=ACMRD -- src/main/resources/db/migrations; then
    echo \"Staged database migrations changed; regenerating the database schema diagram\"
    node scripts/generate_database_schema.js --staged-migrations || exit \$?
    git add docs/database-schema.mmd
else
    echo \"No staged migration files; no need to regenerate schema diagram\"
fi

${END_MARKER}"

HOOK_PATH="$(git rev-parse --git-path hooks/pre-commit)"
mkdir -p "$(dirname "$HOOK_PATH")"

if [ -f "$HOOK_PATH" ]; then
    EXISTING_CONTENT="$(cat "$HOOK_PATH")"
else
    EXISTING_CONTENT=""
fi

# Replace existing marked block if present.
if [[ "$EXISTING_CONTENT" == *"$START_MARKER"* ]]; then
    TMP_FILE="${HOOK_PATH}.tmp"
    awk -v start="$START_MARKER" -v end="$END_MARKER" -v replacement="$HOOK_CONTENT" '
        BEGIN { inblock = 0; replaced = 0 }
        {
            if ($0 == start) {
                if (replaced == 0) {
                    print replacement
                    replaced = 1
                }
                inblock = 1
                next
            }
            if (inblock == 1 && $0 == end) {
                inblock = 0
                next
            }
            if (inblock == 0) {
                print
            }
        }
        END {
            if (replaced == 0) {
                if (NR > 0) print ""
                print replacement
            }
        }
    ' "$HOOK_PATH" > "$TMP_FILE"
    mv "$TMP_FILE" "$HOOK_PATH"
else
    if [ -f "$HOOK_PATH" ] && grep -q '^#!' "$HOOK_PATH"; then
        TMP_FILE="${HOOK_PATH}.tmp"
        awk -v hook="$HOOK_CONTENT" '
            NR == 1 {
                print
                print ""
                print hook
                print ""
                next
            }
            { print }
        ' "$HOOK_PATH" > "$TMP_FILE"
        mv "$TMP_FILE" "$HOOK_PATH"
    elif [ -f "$HOOK_PATH" ]; then
        printf '%s\n\n%s' "$HOOK_CONTENT" "$EXISTING_CONTENT" > "$HOOK_PATH"
    else
        printf '#!/bin/sh\n\n%s\n' "$HOOK_CONTENT" > "$HOOK_PATH"
    fi
fi

chmod +x "$HOOK_PATH"
echo "Installed database schema pre-commit hook at $HOOK_PATH"