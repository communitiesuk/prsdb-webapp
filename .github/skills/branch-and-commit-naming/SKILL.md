---
name: branch-and-commit-naming
description: Use when creating branches, writing commit messages, or naming PRs in prsdb repositories. Ensures consistent naming conventions.
---

# Branch and Commit Naming Conventions

## Branch Names

Format: `<type>/<TICKET-ID>-<description>`

- **type**: `feat`, `fix`, `chore`, or `docs` (always lowercase)
- **TICKET-ID**: `PDJB-###` or `PRSD-####` (always uppercase), or `PDJB-NONE`/`PRSD-NONE` for work without a ticket
- **description**: lowercase kebab-case summary

Examples:
```
feat/PDJB-632-gas-cert-expired-page
fix/PDJB-274-duplicate-textboxes-join-property
chore/PDJB-686-decommission-property-registration
docs/PRSD-NONE-update-readme
```

Special branches:
- Hotfix: `hotfix/PRSD-<ticket>-<description>` (e.g. `hotfix/PRSD-1234-fix-critical-bug`)
- Release: `release/main-to-<target>-<N>` (e.g. `release/main-to-test-11`)

## Commit Messages

Format: `TICKET-ID: Description`

- **TICKET-ID**: `PDJB-###`, `PRSD-####`, `PDJB-NONE`, or `PRSD-NONE` (always uppercase)
- **Description**: sentence case, concise summary of the change

Examples:
```
PDJB-632: Create gas cert expired page with occupied/unoccupied variants
PDJB-274: Fix duplicate textboxes on Join Property Find a Property page
PDJB-NONE: Fix worktree removal script for long paths
PRSD-1021: NGD Address Update Task Runner
```

## PR Titles

Follow the same format as commit messages: `TICKET-ID: Description`

On squash merge, GitHub uses the PR title as the merge commit message, so consistent PR titles produce consistent commit history.

## Determining the Ticket ID

When a ticket ID is needed (for branch names, commit messages, or PR titles):
1. **Check the current branch name** — it usually contains the ticket ID (e.g. `feat/PDJB-632-gas-cert-expired-page` → `PDJB-632`)
2. **If the branch name does not contain a ticket ID**, ask the user for it

## Attribution

Do not include attribution lines such as `Co-authored-by` in commit messages.
