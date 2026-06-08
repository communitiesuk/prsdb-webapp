---
name: smoke-testing
description: Use when navigating the running application to verify pages and journeys work correctly. Mandates Playwright CLI usage and prohibits MCP server tools for app navigation.
allowed-tools: 'shell(playwright-cli:*)'
---

# Smoke Testing

Instructions for navigating the running application to verify pages and journeys work
correctly after changes.

## CRITICAL: Use the Playwright CLI

**MANDATORY:** All smoke testing MUST use the **Playwright CLI** via the
`playwright-cli` skill (located at `.claude/skills/playwright-cli/`). Invoke that
skill for browser interactions — it provides navigation, clicking, form filling,
screenshots, and data extraction.

**PROHIBITED:** Do NOT use the Playwright MCP server tools (`playwright-playwright_navigate`,
`playwright-playwright_click`, `playwright-playwright_fill`, etc.) for smoke testing
the application. The MCP server tools share a single global browser state that
conflicts with parallel development.

## Session Naming

Use the worktree name as the session name to avoid conflicts:
```
playwright-cli -s=pdjb-819 open http://localhost:8080/
```

This allows multiple developers (or worktrees) to smoke test simultaneously without
interfering with each other.

## Before You Start

1. Ensure the application is running (use the `running-locally` skill)
2. Read the `SERVER_PORT` from the worktree's `.env` file
3. Construct the base URL: `http://localhost:<SERVER_PORT>`

## Common Smoke Test Flows

### Verify a Dashboard Page

```bash
# Navigate to the landlord dashboard
playwright-cli -s=pdjb-819 open http://localhost:8080/landlord/dashboard

# Expected: Page renders without errors, shows navigation and content sections
```

### Verify a Registration Journey

```bash
# Start the landlord registration journey
playwright-cli -s=pdjb-819 open http://localhost:8080/landlord/register-as-a-landlord

# Navigate through each step manually in the browser
# Verify: each page loads, form fields appear, validation messages show on empty submit
```

### Verify a Search Page

```bash
# Navigate to the property search page
playwright-cli -s=pdjb-819 open http://localhost:8080/local-council/search

# Enter search criteria and verify results appear
```

### Verify Form Validation

```bash
# Navigate to a form page
playwright-cli -s=pdjb-819 open http://localhost:8080/landlord/register-as-a-landlord/name

# Submit the form empty to trigger validation errors
# Expected: error summary appears at top, individual field errors appear inline
```

### Verify Conditional Page Visibility

```bash
# Navigate to a page that should only appear under certain conditions
# Expected: the page either renders correctly or redirects as expected
```

### Verify a Local Council Page

```bash
# Navigate to a local council management page
playwright-cli -s=pdjb-819 open http://localhost:8080/local-council/dashboard

# Expected: LA-specific navigation and content renders
```

## After Smoke Testing

Close the Playwright session when finished:
```bash
playwright-cli -s=pdjb-819 close
```

## What to Check

During a smoke test, verify:
1. **Pages load** — no 500 errors, no blank pages, no Thymeleaf rendering errors
2. **Navigation works** — links between pages function correctly
3. **Form fields appear** — all expected inputs are rendered
4. **Validation messages** — submitting empty forms shows GOV.UK error summary + inline errors
5. **Conditional content** — elements that depend on state show/hide correctly
6. **Data displays** — if the page shows data from the database, verify it renders
7. **Role-based access** — pages that require specific roles are accessible (mock auth provides all roles)

## Limitations

- Smoke testing verifies pages load and render correctly — it is not a substitute for
  integration tests which assert specific business logic and data flows
- The `local-no-auth` profile auto-authenticates with all roles, so role-based access
  issues will not surface during local smoke testing
