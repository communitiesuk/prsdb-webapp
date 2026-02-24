# Create Release PRs

Create release PRs for main -> test and main -> nft branches in **both repositories**:
- `communitiesuk/prsdb-infra`
- `communitiesuk/prsdb-webapp`

## Process

1. **Fetch latest changes** from origin for both repositories
2. **Check commits** between branches using `git log origin/{target}..origin/main`
3. **Check for existing draft PRs** - if a draft release PR already exists for the target branch, update it rather than creating a new one (drafts may contain special release instructions added earlier)
4. **Find previous release PRs** to determine the next release number and follow the existing format
5. **Create or update PRs** with release notes summarising the changes

## Release Notes Format

Based on previous releases in this repository:

- Group commits by ticket number (PRSD-*, PDJB-*)
- Order tickets by the position of their first commit in the git log (oldest first)
- Combine related commits under a single ticket entry
- Group PRSD-NONE/PDJB-NONE items together at the end
- Use format: `TICKET: Brief description`

Example:
```markdown
## Release notes

PDJB-119: Send joint landlord invitations
PDJB-273: Join a registered property as a joint landlord
PDJB-467: Gas safety task, EICR task, EPC task
PRSD-1021: NGD Address Update Task Runner
PRSD-NONE: Fixes bug on windows, Updates test seed data
```

## PR Title Format

- main -> test: `Release main to test #N` (increment from last release)
- main -> nft: `Release main to nft #N`

## Commands

```bash
# Check commits to release
git fetch origin
git log origin/test..origin/main --oneline
git log origin/nft..origin/main --oneline

# Check for existing draft PRs
gh pr list --repo communitiesuk/prsdb-webapp --state open --draft --search "Release main to test"
gh pr list --repo communitiesuk/prsdb-webapp --state open --draft --search "Release main to nft"

# Find previous release PR numbers
gh pr list --repo communitiesuk/prsdb-webapp --state all --search "Release main to test" --limit 5

# Update existing draft PR
gh pr edit {PR_NUMBER} --body "## Release notes

..."

# Or create new PR if no draft exists
gh pr create --base test --head main --title "Release main to test #N" --body "## Release notes

..."
```

## Notes

- If no commits exist between branches, no PR is needed
- Include any special release instructions if commits require manual steps (e.g., database migrations, secret population)
- When updating a draft PR, preserve any existing special release instructions that were added manually
- Repeat the process for both `prsdb-infra` and `prsdb-webapp` repositories
