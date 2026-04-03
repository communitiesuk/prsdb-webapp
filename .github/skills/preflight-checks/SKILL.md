---
name: preflight-checks
description: Use at the start of any development workflow to verify that all required tools and services are available. Caches results to avoid repeated checks.
---

# Preflight Checks

Verify that all tools required for the development workflow are available before
starting work. Cache results to avoid repeating checks in subsequent sessions.

## Cache

**Location:**
- Unix/macOS: `~/.copilot/preflight-status.json`
- Windows: `%USERPROFILE%\.copilot\preflight-status.json`

**Strategy:** If the file exists and `lastChecked` matches today's date, report
the cached results and ask the user whether to re-run or proceed. Otherwise run
all checks fresh. Before writing the cache file, ensure the `.copilot` directory
exists under the user's home directory; create it if necessary.

**Format:**

```json
{
  "lastChecked": "<today-iso-date>",
  "results": {
    "gh": { "available": true },
    "intellij": { "available": true, "command": "idea64" },
    "figmaMcp": { "available": true },
    "jetbrainsMcp": { "available": true },
    "docker": { "available": true },
    "playwrightCli": { "available": true },
    "superpowersPlugin": { "available": true }
  }
}
```

## Checks

Run each check below. Record pass/fail and any metadata noted.

### 1. gh CLI

Run `gh --version` and `gh auth status`.

- Pass: both commands succeed.
- Fail guidance: install from https://cli.github.com/ and run `gh auth login`.

### 2. IntelliJ CLI

Run `idea64 --version`. If that fails, try `idea --version`.

- Pass: either command returns version output.
- Record which command succeeded (`idea64` or `idea`) in the cache under
  `results.intellij.command` — this value is used later to launch IntelliJ.
- Fail guidance:
  - Ensure the IntelliJ `bin` directory (or launcher script) is on PATH.
  - Windows: typically `C:\Program Files\JetBrains\IntelliJ IDEA <version>\bin`.
  - macOS: use **Tools → Create Command-line Launcher…** in IntelliJ, or add
    `/Applications/IntelliJ IDEA.app/Contents/MacOS` to PATH.
  - Linux: add the IntelliJ `bin` directory from the installation (e.g.
    `/opt/idea-IU-<version>/bin`) to PATH.

### 3. Figma MCP Server

Check whether Figma MCP tools are available in the current tool list (look for
tools with a `figma` prefix).

- Pass: at least one Figma tool is listed.
- Fail guidance: configure the Figma MCP server in the Copilot agent settings.
  The user may need to restart the Copilot agent for MCP server changes to take
  effect.

### 4. JetBrains MCP Server

Check whether JetBrains MCP tools are available in the current tool list (look
for tools with a `jetbrains` prefix).

- Pass: at least one JetBrains tool is listed.
- Fail guidance: ensure the JetBrains MCP server plugin is installed in
  IntelliJ and configured in the Copilot agent settings. The user may need to
  restart the Copilot agent for MCP server changes to take effect.

### 5. Docker

Run `docker --version`, then `docker info` (suppress verbose output, e.g.
redirect to `/dev/null` on Unix/macOS, pipe to `Out-Null` on PowerShell, or
simply check the exit code).

- Pass: both commands succeed (Docker is installed and the daemon is running).
- Fail guidance: install Docker Desktop and ensure the daemon is started.

### 6. Playwright CLI

Run `playwright-cli --version` (or check the PATH for `playwright-cli`).

- Pass: the command returns version output.
- Fail guidance: install via `npm install -g @playwright/cli@latest` and ensure
  `playwright-cli` is on PATH. See https://github.com/microsoft/playwright-cli
  for details. The standalone CLI is required (rather than the Gradle Playwright
  task or MCP server) to support parallel smoke testing across multiple
  worktrees using named sessions (`playwright-cli -s=<name>`).

### 7. Superpowers Plugin

Run `/skills list` (or equivalent) and check whether skills provided by the
superpowers plugin are available (e.g. skills with a `plugin` location such as
`brainstorming`, `writing-plans`, `subagent-driven-development`).

- Pass: at least one superpowers-provided skill is listed.
- Fail guidance: install the superpowers plugin by running
  `/plugin install superpowers-marketplace/superpowers`. The user may need to
  restart the Copilot CLI for plugin changes to take effect.

## Reporting

After all checks complete:

1. Print a summary table:
   ```
   Tool                Status
   ──────────────────  ──────
   gh CLI              ✓
   IntelliJ CLI        ✓ (idea64)
   Figma MCP           ✗ — not connected
   JetBrains MCP       ✓
   Docker              ✓
   Playwright CLI      ✓
   Superpowers Plugin  ✓
   ```
2. If any check failed, explain what is missing and provide the guidance above.
3. Ask the user whether to proceed despite missing tools or fix the issues first.
4. Save results to the cache file.
