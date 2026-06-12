# Phase 0 — Preflight

Invoke the `preflight-checks` skill to verify all required tools are available.

## Criticality

- **Critical** (block if missing): gh CLI, IntelliJ CLI, JetBrains MCP, Docker
- **Task-dependent** (block only for UI/content tasks): Figma MCP, Playwright CLI

Do not proceed if any critical tool is missing unless the user explicitly confirms.
