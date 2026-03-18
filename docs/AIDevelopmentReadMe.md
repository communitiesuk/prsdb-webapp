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
frontend changes and debugging integration test failures. See the
[Playwright MCP setup guide](https://github.com/anthropics/anthropic-quickstarts/tree/main/mcp-server-playwright) for
full documentation.

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
Useful for implementing pages that match design specs. See the
[Figma MCP setup guide](https://github.com/nichochar/figma-developer-mcp) for full documentation.

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

### Initial setup

The instruction files are gitignored (they're developer-specific config), so you need to set them up when starting for the
first time. If you already have your own instruction files, see [Existing instruction files](#existing-instruction-files)
below.

1. **Copy the template** to create your main instructions file:

   ```powershell
   Copy-Item .github\copilot-instructions.template.md .github\copilot-instructions.md
   ```

2. **Generate the path-specific instruction files** by asking Copilot to run the skill:

   > "Generate custom instructions"

   This invokes the `generate-custom-instructions` skill, which parses the instruction table in
   `copilot-instructions.md`, explores the relevant source directories, and generates each instruction file in
   `.github/instructions/`.

After initial setup, the worktree scripts automatically copy instruction files into new worktrees, so you only need to do
this once.

### Existing instruction files

If you already have your own `.github/copilot-instructions.md` and/or files in `.github/instructions/`, you can either:

- **Skip setup entirely** — your existing files will continue to work as before. Consider running "Update the instructions"
  periodically to keep them in sync with the codebase.

- **Regenerate from the latest template** — if your instruction files are outdated or you want a fresh start:

  1. Back up your existing files if you've made personal customisations you want to keep.
  2. Copy the template over your main file:

     ```powershell
     Copy-Item .github\copilot-instructions.template.md .github\copilot-instructions.md -Force
     ```

  3. Ask Copilot to "Generate custom instructions". The skill will detect existing files in `.github/instructions/` and
     ask whether to overwrite or skip each one.

- **Fill in gaps** — if you have the main instructions file but are missing some path-specific files, run
  "Generate custom instructions". The skill will ask what to do about existing files and only generate the missing ones
  if you choose to skip existing.

### Main instructions file

The main instructions file at `.github/copilot-instructions.md` is the entry point for Copilot context. It provides an
overview of the architecture, build and test commands, Spring profiles, and key conventions. This file is automatically
loaded by Copilot when working in the repository.

Since this file is gitignored, it's also the place to add your own personal preferences and working style. Add a section
at the end of the file with any instructions you want Copilot to follow.

### Path-specific instructions

We have 17 instruction files in `.github/instructions/` that are automatically applied by Copilot based on the files being
edited. Each file documents the patterns, conventions, and best practices for a specific area of the codebase.

For example, `controllers.instructions.md` is applied when editing files in `controllers/` and covers endpoint patterns,
permission handling, and response conventions. `integration-tests.instructions.md` is applied when editing integration tests
and covers test base classes, page object patterns, and Navigator usage.

The full list of instruction files and their scopes is documented in the main instructions file.

### Adding or updating instructions

Instructions should reflect the current patterns in the codebase. When conventions change, ask Copilot to
"update the instructions" — this invokes the `updating-custom-instructions` skill, which reviews all instruction files
against the codebase and proposes updates. New instruction files can be added for new packages by creating a markdown file
in `.github/instructions/` with the appropriate `applyTo` frontmatter and adding a row to the table in the main
instructions file.

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

### Updating custom instructions

`updating-custom-instructions/SKILL.md` reviews all instruction files against the current codebase and proposes updates.
It explores each domain's source directories, identifies new, changed, or removed patterns, and presents a summary for
approval before applying changes. Use it periodically or after significant refactors.

> "Update the instructions"

### Generating custom instructions

`generate-custom-instructions/SKILL.md` creates instruction files from scratch for initial setup. It parses the table in
the main instructions file to know which files to create, then explores the codebase and generates each one sequentially.
See [Initial setup](#initial-setup) above.

> "Generate custom instructions"

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

## Prompt Structure

When working on a feature with Copilot, follow an **explore → plan → implement → test** cycle. Each phase uses a
different prompting style to get the best results.

### 1. Explore

Start by asking Copilot to find and summarise the files relevant to what you're building. This gives both you and Copilot
the context needed before making changes.

> "Explore the codebase for files related to the join property journey. Summarise each file and why it's relevant."

> "Find all the controllers, services, and templates involved in landlord registration."

This phase is about understanding — don't ask Copilot to make changes yet.

### 2. Plan

Switch to **plan mode** (Shift+Tab to toggle) and ask Copilot to create a step-by-step implementation plan. Plan mode
saves the plan to a file so you can review and edit it before any code is written.

> "Create a step-by-step plan to implement the gas certificate expired page with occupied and unoccupied variants."

> "Plan the changes needed to add conditional routing to the select-property step."

Review the plan and make any adjustments before moving on. You can edit the plan file directly or ask Copilot to revise
specific steps.

### 3. Implement

Switch out of plan mode (Shift+Tab) and ask Copilot to execute the plan.

> "Implement the plan."

Copilot will work through the steps sequentially, creating and modifying files. You can also implement specific steps:

> "Implement steps 1-3 from the plan."

### 4. Test

Ask Copilot to run the relevant tests and verify the implementation.

> "Run the unit tests for the gas certificate controller and service."

> "Run the integration tests related to landlord registration."

If you have Figma designs for the feature, you can also ask Copilot to compare the implementation against the design:

> "Use Figma and Playwright to check that the gas certificate expired page matches the design."

### Tips

- **Don't skip the explore phase** — Copilot produces better plans and implementations when it has seen the relevant code
  first.
- **Keep prompts focused** — one feature or change per cycle. For large features, break them into smaller pieces and run
  separate explore → plan → implement → test cycles for each.
- **Iterate within phases** — if the plan doesn't look right, refine it before implementing. If tests fail, ask Copilot
  to debug and fix rather than starting over.
