---
name: preflight-checks
description: Use at the start of any development workflow to verify that all required tools and services are available. Caches results to avoid repeated checks.
allowed-tools: 'write shell(gh --version) shell(gh auth status) shell(docker info) shell(idea.cmd --version) shell(idea --version) shell(idea64 --version) shell(playwright-cli --version) shell(Set-Content -Path ~/.copilot/preflight-status.json:*) shell(tee ~/.copilot/preflight-status.json:*)'
---

# Preflight Checks

Verify that all tools required for the development workflow are available. Checks are
divided into two categories with different caching strategies.

## Check Categories

### Installation Checks (cached 30 days)

These verify that required software is installed. They change rarely and are expensive
to re-run unnecessarily.

| Check | Command | Pass Criteria |
|-------|---------|---------------|
| gh CLI | `gh --version` && `gh auth status` | Both succeed |
| IntelliJ CLI | `idea64 --version` or `idea --version` or `idea.cmd --version` | Any returns output or is found on PATH |
| Playwright CLI | `playwright-cli --version` | Returns version output |

### Activation Checks (run every time)

These verify that services are currently running and connected. They must pass at the
start of every workflow session regardless of cache.

| Check | Method | Pass Criteria |
|-------|--------|---------------|
| JetBrains MCP | Look for `jetbrains-*` tools in available tool list | At least one tool present |
| Docker daemon | `docker info` (suppress output, check exit code) | Exit code 0 |
| Figma MCP | Look for `figma*` tools in available tool list | At least one tool present |

## Cache

**Location:**
- Windows: `%USERPROFILE%\.copilot\preflight-status.json`
- Unix/macOS: `~/.copilot/preflight-status.json`

**Strategy:**
- If `lastInstallChecked` is within 30 days of today: skip installation checks,
  report cached results
- Activation checks always run fresh regardless of cache
- Before writing the cache file, ensure the `.copilot` directory exists
- Write the cache with a command that matches this skill's pre-approved permission, so it
  runs without a prompt. Use the `~/.copilot/preflight-status.json` path (forward slashes, no
  drive-letter colon, portable across users) — PowerShell:
  `Set-Content -Path ~/.copilot/preflight-status.json -Value $cacheJson`; Bash:
  `tee ~/.copilot/preflight-status.json <<'EOF' … EOF`

**Format:**

```json
{
  "lastInstallChecked": "<iso-date>",
  "installResults": {
    "gh": { "available": true },
    "intellij": { "available": true, "command": "idea.cmd" },
    "playwrightCli": { "available": true, "version": "1.59.0" }
  },
  "activationResults": {
    "jetbrainsMcp": { "available": true },
    "docker": { "available": true },
    "figmaMcp": { "available": false }
  }
}
```

## Execution Order

1. Check cache age for installation checks
2. Run installation checks if cache is stale (>30 days) or missing
3. Always run activation checks
4. Report results
5. Save updated cache

## Installation Check Details

### gh CLI

Run `gh --version` and `gh auth status`.

- Pass: both commands succeed.
- Fail guidance: install from https://cli.github.com/ and run `gh auth login`.

### IntelliJ CLI

Run `idea64 --version`. If that fails, try `idea --version`, then `idea.cmd --version`.

- Pass: any command returns version output.
- Record which command succeeded in the cache under `installResults.intellij.command`.
- Fail guidance: add the IntelliJ `bin` directory to PATH. Windows: typically via
  JetBrains Toolbox scripts. macOS: use **Tools → Create Command-line Launcher…**.

### Playwright CLI

Run `playwright-cli --version`.

- Pass: the command returns version output.
- Fail guidance: install via `npm install -g @anthropic-ai/playwright-cli@latest`.

## Reporting

Print a summary table:
```
Tool                 Category      Status
───────────────────  ────────────  ──────
gh CLI               Install       ✓ (cached)
IntelliJ CLI         Install       ✓ (idea.cmd, cached)
Playwright CLI       Install       ✓ (cached)
JetBrains MCP        Activation    ✓
Docker               Activation    ✓
Figma MCP            Activation    ✗ — not connected
```

## Criticality

- **Critical** (block workflow): gh CLI, IntelliJ CLI, JetBrains MCP, Docker
- **Task-dependent** (block only for UI/content tasks): Figma MCP, Playwright CLI

If any critical tool is missing, do not proceed unless the user explicitly confirms.
