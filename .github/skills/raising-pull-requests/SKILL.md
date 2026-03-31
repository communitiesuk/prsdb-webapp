---
name: raising-pull-requests
description: Use when creating pull requests, writing PR descriptions, or filling in PR templates for prsdb repositories.
---

# Raising Pull Requests

## PR Title

Format: `TICKET-ID: Description` (e.g. `PDJB-632: Create gas cert expired page`)

See the `branch-and-commit-naming` skill for full naming conventions.

## PR Description

Use the repository PR template at `.github/pull_request_template.md`. Fill in each section as follows:

### Ticket number

The JIRA ticket ID (e.g. `PDJB-632` or `PRSD-1021`). GitHub auto-links to JIRA when formatted correctly.

### Goal of change

A single sentence summarising the problem being solved. This is typically the ticket summary in your own words.

### Description of main change(s)

Focus on **what changed functionally**, not implementation details. The code diff shows the implementation — the description should explain the intent.

Preferred:
> Adds expired gas certificate page with variants for occupied and unoccupied properties

Avoid:
> Adds new `GasCertExpiredController` that injects `PropertyService` and calls `getOccupancyStatus()`

Use bullet points for multiple changes. Match the level of detail to the complexity of the change:
- **Simple changes** (deletions, renames): 1–2 bullets
- **Standard features**: 3–5 bullets covering functional changes
- **Complex fixes**: detailed explanation including what was wrong, why, and cross-cutting impact

### Anything you'd like to highlight to the reviewer?

Use this section when there is genuine uncertainty or something unusual. Examples:
- Debate over implementation approach where specific feedback is wanted
- Concerns about correctness in a particular area
- Cross-cutting changes that affect other journeys
- Leave empty if there is nothing to highlight

### Checklist

- **Check** `[x]` items that have been completed
- **Delete** items that do not apply to this PR
- Do not leave non-applicable items unchecked — remove them entirely

The full checklist covers:
- Screenshots of UI changes
- Unit tests for new logic
- Controller tests for new endpoints (including permissions)
- Single page integration tests for unhappy-flow UI (e.g. validation errors)
- Journey integration test steps for new journey steps
- New journey integration tests for new journeys
- Email templates added to `/src/main/resources/emails/emailTemplates.json`
- Full test suite run locally and passing
- Branch rebased onto main and run locally
- TODO comments for this ticket searched for and removed
- Seed data updated
- `NftDataSeeder` updated for schema changes
- Special release instructions added as checklist items to a draft PR
- QA instructions added to the ticket
