# AI-Assisted Development

We use GitHub Copilot to assist with development. The AI workflow is built around three things: context instructions that
teach Copilot our codebase conventions, skills that guide repeatable workflows, and worktree scripts that support parallel
development with AI context replicated across workspaces.

## Getting Started

### Prerequisites

- An **active GitHub Copilot subscription**.
- **Windows:** PowerShell 6 or higher (run `$PSVersionTable.PSVersion` to check)
- **macOS:** No additional prerequisites

### Install the Copilot CLI

**Windows:**

```powershell
winget install GitHub.Copilot
```

**macOS:**

```bash
brew install copilot-cli
```

### First launch

Start a session from any worktree or the main repo:

```
copilot
```

On first launch you'll be prompted to log in to your GitHub account via the `/login` command. The CLI automatically
picks up the Copilot instructions and skills from `.github/` when launched from the repo root.

### GitHub CLI (`gh`)

The Copilot CLI can use the [GitHub CLI](https://cli.github.com/) for operations like creating PRs, managing issues, and
interacting with GitHub Actions. Install it and authenticate:

**Windows:**

```powershell
winget install GitHub.cli
gh auth login
```

**macOS:**

```bash
brew install gh
gh auth login
```

### MCP Servers

The Copilot CLI ships with the **GitHub MCP server** built in — no setup needed for GitHub API access (PRs, issues,
branches, actions, code search). The following additional MCP servers need to be configured manually.

#### Playwright MCP Server

Provides browser automation — navigating pages, taking screenshots, inspecting DOM snapshots. Useful for verifying
frontend changes and debugging integration test failures.

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-server-playwright"]
    }
  }
}
```

#### Figma MCP Server

Provides access to Figma designs — fetching design context, screenshots, and variable definitions from Figma files.
Useful for implementing pages that match design specs.

```json
{
  "mcpServers": {
    "figma": {
      "command": "npx",
      "args": ["-y", "figma-developer-mcp", "--stdio"]
    }
  }
}
```

This requires the Figma desktop app to be running with the Dev Mode MCP enabled.

### Configuration File Location

MCP server configuration goes in `~/.copilot/settings.json` (global) or `.copilot/settings.json` (per-project). You can
combine all three servers in a single config file. See the
[MCP documentation](https://modelcontextprotocol.io/quickstart) for more details.

## Copilot Instructions

### Main instructions file

The main instructions file at `.github/copilot-instructions.md` is the entry point for Copilot context. It provides an
overview of the architecture, build and test commands, Spring profiles, and key conventions. This file is automatically
loaded by Copilot when working in the repository.

### Path-specific instructions

We have 17 instruction files in `.github/instructions/` that are automatically applied by Copilot based on the files being
edited. Each file documents the patterns, conventions, and best practices for a specific area of the codebase.

For example, `controllers.instructions.md` is applied when editing files in `controllers/` and covers endpoint patterns,
permission handling, and response conventions. `integration-tests.instructions.md` is applied when editing integration tests
and covers test base classes, page object patterns, and Navigator usage.

The full list of instruction files and their scopes is documented in the main instructions file.

### Adding or updating instructions

Instructions should reflect the current patterns in the codebase. When conventions change, the relevant instruction file
should be updated to match. New instruction files can be added for new packages by creating a markdown file in
`.github/instructions/` with the appropriate `applyTo` frontmatter.

Note: The main instructions file (`.github/copilot-instructions.md`) and the instruction files in `.github/instructions/`
are gitignored. This is because they are developer-specific configuration that may vary between machines. The worktree
scripts handle copying these files into new worktrees automatically.

## Skills

Skills are structured guides in `.github/skills/` that teach Copilot how to perform specific workflows consistently.

### Branch and commit naming

`branch-and-commit-naming/SKILL.md` enforces our naming conventions:
- **Branches:** `<type>/<TICKET-ID>-<description>` (e.g. `feat/PDJB-632-gas-cert-expired-page`)
- **Commits:** `TICKET-ID: Description` (e.g. `PDJB-632: Create gas cert expired page`)
- **Types:** `feat`, `fix`, `chore`, `docs`
- **Ticket IDs:** `PDJB-###` or `PRSD-####`, or `PDJB-NONE`/`PRSD-NONE` for unticketed work

### Raising pull requests

`raising-pull-requests/SKILL.md` guides PR creation using our PR template. It covers the expected description format,
checklist items (tests, screenshots, seed data, QA instructions), and the emphasis on describing functional changes rather
than implementation details.

You can invoke this skill by asking Copilot to create a PR for you. For example:

> "Create a pull request for this branch"

Copilot will use the PR template, fill in the ticket number from the branch name, describe the functional changes based on
the diff, and populate the checklist.

### Creating release PRs

`creating-release-prs/SKILL.md` automates the release PR process. It covers checking commits between branches, determining
release numbers, generating grouped release notes, and creating or updating draft PRs for both the webapp and infra
repositories.

You can invoke this skill by asking Copilot to prepare a release. For example:

> "Create a release PR to test"

Copilot will check the commits between `main` and `test`, group them by ticket number, determine the next release number,
and create (or update) a draft PR with the generated release notes.

### Using skills

Skills are invoked automatically by Copilot when performing relevant tasks. You can also trigger them explicitly by
referencing the workflow in your prompt. Some examples:

- "Create a branch for ticket PDJB-789 to add landlord notifications" — uses the naming skill to produce
  `feat/PDJB-789-landlord-notifications`
- "Commit these changes for PDJB-456" — uses the naming skill to produce `PDJB-456: Add validation for postcode field`
- "Raise a PR" — uses the PR skill to generate a description from the diff and fill in the template
- "Create release PRs to nft" — uses the release skill to generate release notes and create PRs in both repositories

## Worktree Scripts

We use git worktrees for parallel development, managed by scripts in `scripts/git-worktrees/`. Each script is available in
both PowerShell and Bash.

### Creating a worktree

```powershell
.\scripts\git-worktrees\new-worktree.ps1 -WorktreeName "pdjb-123" -BranchName "feat/PDJB-123-my-feature"
```

This creates a new worktree as a sibling directory of the main repo, creates the branch from `main` (or a specified base
branch), and sets up the workspace. Crucially, it **automatically copies gitignored configuration files** from the main repo
into the new worktree — this includes `.env`, copilot instruction files, and key files. It also runs `npm install` to set up
frontend dependencies.

The script discovers files to copy dynamically using `git ls-files --others --ignored --exclude-standard`, filtering out
build artifacts. This means no script changes are needed when new gitignored config files are added.

### Switching branches in a worktree

```powershell
.\scripts\git-worktrees\switch-worktree.ps1 -BranchName "feat/PDJB-456-other-work"
```

Switches the current worktree to a different branch with safety checks for uncommitted changes.

### Removing a worktree

```powershell
.\scripts\git-worktrees\remove-worktree.ps1 -WorktreePath "pdjb-123"
```

Removes the worktree directory, prunes stale references, and optionally deletes the local branch. It handles
Windows-specific issues with deeply nested paths (e.g. `node_modules`) by cleaning those directories before removal.

## Typical Workflow

1. Pick up a ticket and create a worktree: `new-worktree.ps1 -WorktreeName "pdjb-123" -BranchName "feat/PDJB-123-my-feature"`
2. Work on the feature with Copilot — instructions and skills are automatically available in the new worktree
3. Use the naming skill for consistent branch names and commit messages
4. When ready, use the PR skill to create a pull request with the correct template and description
5. After merging, clean up: `remove-worktree.ps1 -WorktreePath "pdjb-123"`
6. For releases, use the release skill to generate release PRs with grouped notes
