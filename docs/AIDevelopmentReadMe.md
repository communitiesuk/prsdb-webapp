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
branches, actions, code search). It uses your GitHub CLI (`gh`) authentication, so it has access to the same repositories
you can reach via `gh`. The following additional MCP servers need to be configured manually.

#### Playwright MCP Server

Provides browser automation — navigating pages, taking screenshots, inspecting DOM snapshots. Useful for verifying
frontend changes and debugging integration test failures. See the
[Playwright MCP repo](https://github.com/microsoft/playwright-mcp) for full documentation.

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp"]
    }
  }
}
```

#### Figma MCP Server

Provides access to Figma designs — fetching design context, screenshots, and variable definitions from Figma files.
Useful for implementing pages that match design specs. See the
[Figma MCP guide](https://help.figma.com/hc/en-us/articles/32132100833559-Guide-to-the-Figma-MCP-server) for full
documentation.

To enable it, open the Figma Desktop app and go to **Preferences → Enable Dev Mode MCP Server**. Then add the following
to your MCP config:

```json
{
  "mcpServers": {
    "figma": {
      "type": "http",
      "url": "http://127.0.0.1:3845/mcp"
    }
  }
}
```

This requires the Figma desktop app to be running with Dev Mode MCP enabled.

### Configuration File Location

MCP server configuration goes in `~/.copilot/settings.json` (global) or `.copilot/settings.json` (per-project). You can
combine all three servers in a single config file. See the
[MCP documentation](https://modelcontextprotocol.io/quickstart) for more details.

### Superpowers Plugin

We recommend installing the [Superpowers](https://github.com/obra/superpowers) plugin — it enforces structured workflows
for brainstorming, test-driven development, systematic debugging, and implementation planning. It makes a noticeable
difference to output quality by automatically guiding Copilot through a brainstorm → plan → implement cycle.

**Install via the Copilot CLI:**

```
/plugin marketplace add obra/superpowers-marketplace
/plugin install superpowers@superpowers-marketplace
```

**Update to the latest version:**

```
/plugin update superpowers
```

Start a new session after installing. Skills activate automatically when relevant — for example, asking Copilot to plan a
feature will invoke the brainstorming and planning skills.

## Copilot Instructions

### Main instructions file

The main instructions file at `.github/copilot-instructions.md` provides an overview of the architecture, build and test
commands, Spring profiles, and key conventions. This file is automatically loaded by Copilot when working in the
repository. Since it is committed to the repository, changes are shared across the team.

### Path-specific instructions

We have 18 instruction files in `.github/instructions/` that are automatically applied by Copilot based on the files being
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

`generate-custom-instructions/SKILL.md` creates or regenerates instruction files. It parses the table in
the main instructions file to know which files to create, then explores the codebase and generates each one sequentially.
Useful when adding a new instruction file or regenerating all files after a significant refactor.

> "Generate custom instructions"

### Development workflow

`development-workflow/SKILL.md` orchestrates the full lifecycle of a development task, from setup through to PR creation.
It chains together other skills automatically across 10 phases:

0. **Preflight** — invokes the `preflight-checks` skill to verify tooling (gh CLI, IntelliJ, Docker, Playwright; Figma MCP for UI tasks)
1. **Setup** — asks for the task and ticket ID, uses `branch-and-commit-naming` to name the branch, creates a worktree via `using-git-worktrees`, and launches IntelliJ
2. **Brainstorm** — invokes the `brainstorming` skill, incorporating any Figma links the user provides for UI tasks
3. **Plan** — invokes the `writing-plans` skill, requires a multi-PR splitting strategy for non-trivial tasks, and asks the user whether PRs should be stacked (parallel) or sequential
4. **Implement** — executes the plan using `subagent-driven-development` or directly, follows TDD where specified, does not commit
5. **Verify** — proposes a verification plan covering unit/controller/integration tests, local smoke tests via Playwright, and Figma comparison for UI changes; executes after user approval
6. **Code review** — launches a sub-agent review using the `reviewing-code` skill, loops back to fix if issues found, then prompts the user to review in IntelliJ before proceeding
7. **Commit & PR** — uses `branch-and-commit-naming` for the commit message, pushes, creates the PR via `raising-pull-requests`, and cleans up the worktree
8. **PR feedback** — reads review comments via GitHub MCP, uses `receiving-code-review` to evaluate each (action, partially action, or push back), implements approved changes in a fresh worktree, re-verifies, and optionally drafts responses to the reviewer
9. **Next PR** — for stacked PRs, branches off the previous PR's branch and returns to Phase 4; for sequential PRs, saves state to `~/.copilot/workflow-state.json` and waits for the current PR to merge

Invoke it by describing a task, or type `/development-workflow` to trigger it explicitly:

> "I need to implement PDJB-789 — add landlord notifications for expired gas certificates"

The skill saves state to `~/.copilot/workflow-state.json` so sessions can be resumed.

### Preflight checks

`preflight-checks/SKILL.md` verifies that required tools are available before starting work: gh CLI, IntelliJ,
Docker, Playwright, and Figma MCP. Results are cached daily to `~/.copilot/preflight-status.json` to avoid
repeated checks. This skill is invoked automatically by the development workflow but can also be run standalone.

> "Run preflight checks"

### Reviewing code

`reviewing-code/SKILL.md` performs project-specific code review covering: custom annotation usage, dependency
injection patterns, controller security (`@PreAuthorize`), journey framework conventions, validation patterns,
entity conventions, and testing standards. It only flags issues that break project conventions — not style
preferences or patterns already established in the codebase. Used automatically by the development workflow
in Phase 6, but can also be invoked directly.

> "Review the changes on this branch"

### Using skills

Skills are invoked automatically by Copilot when performing relevant tasks. You can also trigger them explicitly by
referencing the workflow in your prompt, or by typing `/<skill-name>` (e.g. `/raising-pull-requests`) to force a specific
skill. Some examples:

- "Create a branch for ticket PDJB-789 to add landlord notifications" — uses the naming skill to produce
  `feat/PDJB-789-landlord-notifications`
- "Commit these changes for PDJB-456" — uses the naming skill to produce `PDJB-456: Add validation for postcode field`
- "Raise a PR" — uses the PR skill to generate a description from the diff and fill in the template
- "Create release PRs to nft" — uses the release skill to generate release notes and create PRs in both repositories
- "I need to implement PDJB-789" — uses the development workflow to orchestrate the full task lifecycle
- "Review the changes on this branch" — uses the reviewing code skill to check for convention violations
- "Run preflight checks" — verifies tooling before starting work

## Worktree Scripts

We use git worktrees for parallel development, managed by scripts in `scripts/git-worktrees/`. Each script is available in
both PowerShell and Bash.

### Using git worktrees skill

`using-git-worktrees/SKILL.md` ensures Copilot uses the project scripts (not raw `git worktree` commands) when creating
or removing worktrees. It covers the script parameters, port assignment, and cleanup. This skill is invoked automatically
when Copilot detects a worktree-related task.

### Creating a worktree

```powershell
.\scripts\git-worktrees\new-worktree.ps1 -WorktreeName "pdjb-123" -BranchName "feat/PDJB-123-my-feature"
```
```bash
./scripts/git-worktrees/new-worktree.sh pdjb-123 feat/PDJB-123-my-feature
```

This creates a new worktree as a sibling directory of the main repo, creates the branch from `main` (or a specified base
branch), and sets up the workspace. Crucially, it **automatically copies gitignored configuration files** from the main repo
into the new worktree — this includes `.env` and key files. It also runs `npm install` to set up
frontend dependencies.

The script discovers files to copy dynamically using `git ls-files --others --ignored --exclude-standard`, filtering out
build artifacts. This means no script changes are needed when new gitignored config files are added.

### Switching branches in a worktree

```powershell
.\scripts\git-worktrees\switch-worktree.ps1 -BranchName "feat/PDJB-456-other-work"
```
```bash
./scripts/git-worktrees/switch-worktree.sh feat/PDJB-456-other-work
```

Switches the current worktree to a different branch with safety checks for uncommitted changes.

### Removing a worktree

```powershell
.\scripts\git-worktrees\remove-worktree.ps1 -WorktreePath "pdjb-123"
```
```bash
./scripts/git-worktrees/remove-worktree.sh pdjb-123
```

Removes the worktree directory, prunes stale references, and optionally deletes the local branch. It handles
Windows-specific issues with deeply nested paths (e.g. `node_modules`) by cleaning those directories before removal.

## Working with Copilot

### Development workflow (recommended)

For feature work, describe the task and Copilot will use the `development-workflow` skill to orchestrate the full
lifecycle automatically — from creating a worktree through to raising a PR:

> "I need to implement PDJB-789 — add landlord notifications for expired gas certificates"

Copilot will run preflight checks, create a worktree and branch, brainstorm the approach, create a plan for your review,
implement the changes, run tests, perform a code review, and create a PR. Each phase requires your approval before
proceeding, so you stay in control throughout.

For multi-PR features, the workflow supports both stacked (parallel) and sequential PR strategies — it will ask which
approach you prefer during the planning phase.

### Manual workflow

For quick fixes, exploratory work, or when you want more direct control, you can drive each phase yourself using an
**explore → plan → implement → test** cycle.

1. **Explore** — ask Copilot to find and summarise relevant files before making changes:

   > "Explore the codebase for files related to the join property journey. Summarise each file and why it's relevant."

2. **Plan** — switch to plan mode (Shift+Tab) and ask for a step-by-step implementation plan:

   > "Create a step-by-step plan to implement the gas certificate expired page."

3. **Implement** — switch out of plan mode and ask Copilot to execute the plan:

   > "Implement the plan."

4. **Test** — ask Copilot to run tests and verify the implementation:

   > "Run the unit tests for the gas certificate controller and service."

### Tips

- **Don't skip exploration** — Copilot produces better plans and implementations when it has seen the relevant code first.
- **Keep prompts focused** — one feature or change per cycle. For large features, break them into smaller pieces.
- **Iterate within phases** — if the plan doesn't look right, refine it before implementing. If tests fail, ask Copilot
  to debug and fix rather than starting over.

### Command permissions

By default, Copilot asks for your approval before running shell commands. You'll see a prompt showing the command it wants
to execute and can approve or reject it. **Keep this default behaviour** — it lets you review what Copilot is doing before
it makes changes to your environment.

There is a "yolo" mode (`--allow-all-tools`) that auto-approves all commands without prompting. **Do not use this on its
own.** It removes the safety net of reviewing commands before execution, which can lead to unintended changes to your
local environment, git history, or running destructive operations without your knowledge.

However, you can create a PowerShell alias that allows all tools but explicitly denies dangerous commands. Add this to
your PowerShell profile (`$PROFILE`):

```powershell
function copilot {
  $exe = (Get-Command copilot -CommandType Application).Source

  & $exe `
    --allow-all-tools `
    --deny-tool='shell(git reset)' `
    --deny-tool='shell(git reset:*)' `
    --deny-tool='shell(git clean)' `
    --deny-tool='shell(git clean:*)' `
    --deny-tool='shell(curl)' `
    --deny-tool='shell(curl:*)' `
    --deny-tool='shell(wget)' `
    --deny-tool='shell(wget:*)' `
    --deny-tool='shell(Invoke-WebRequest)' `
    --deny-tool='shell(Invoke-WebRequest:*)' `
    --deny-tool='shell(Invoke-RestMethod)' `
    --deny-tool='shell(Invoke-RestMethod:*)' `
    --deny-tool='shell(Invoke-Expression)' `
    --deny-tool='shell(Invoke-Expression:*)' `
    --deny-tool='shell(iex:*)' `
    --deny-tool='shell(runas)' `
    --deny-tool='shell(runas:*)' `
    --deny-tool='shell(schtasks)' `
    --deny-tool='shell(schtasks:*)' `
    --deny-tool='shell(sc:*)' `
    --deny-tool='shell(reg:*)' `
    --deny-tool='shell(Set-ExecutionPolicy)' `
    --deny-tool='shell(Set-ExecutionPolicy:*)' `
    --deny-tool='shell(diskpart)' `
    --deny-tool='shell(diskpart:*)' `
    --deny-tool='shell(format:*)' `
    --deny-tool='shell(bcdedit)' `
    --deny-tool='shell(bcdedit:*)' `
    --deny-tool='shell(docker system prune)' `
    --deny-tool='shell(docker system prune:*)' `
    --deny-tool='shell(docker volume prune)' `
    --deny-tool='shell(docker volume prune:*)' `
    --deny-tool='shell(docker image prune)' `
    --deny-tool='shell(docker image prune:*)' `
    --deny-tool='shell(docker rm)' `
    --deny-tool='shell(docker rm:*)' `
    --deny-tool='shell(docker rmi)' `
    --deny-tool='shell(docker rmi:*)' `
    @args
}
```

This gives Copilot the speed of auto-approval while blocking destructive system, git, network, and Docker commands.
You can add or remove `--deny-tool` entries to suit your needs.

## Notes
- Playwright requires you to be on your main repo in order to run the server, so you cannot do this from a worktree.