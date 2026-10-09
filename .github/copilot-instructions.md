# Copilot Instructions for PRSDB Webapp

> **Tip:** For feature work, consider using the `development-workflow` skill which orchestrates the full lifecycle from
> setup through implementation, review, and PR creation. Invoke it by describing a task (e.g. "I need to implement
> PDJB-789") or type `/development-workflow` to trigger it explicitly.

## Path-Specific Instructions

Context-aware instructions are available for different parts of the codebase. These are automatically applied based on the files you're working with:

| Instruction File | Applies To |
|------------------|------------|
| [journeys.instructions.md](instructions/journeys.instructions.md) | Kotlin `journeys/`, journey component annotation |
| [controllers.instructions.md](instructions/controllers.instructions.md) | `controllers/` |
| [services.instructions.md](instructions/services.instructions.md) | `services/` |
| [database.instructions.md](instructions/database.instructions.md) | `database/`, `db/migrations/` |
| [integration-tests.instructions.md](instructions/integration-tests.instructions.md) | Browser tests, shared integration fixtures, session builders and test resources |
| [unit-tests.instructions.md](instructions/unit-tests.instructions.md) | Kotlin tests, with guidance by test layer |
| [frontend.instructions.md](instructions/frontend.instructions.md) | `templates/`, `css/`, `js/`, assets and root frontend build files |
| [feature-flags.instructions.md](instructions/feature-flags.instructions.md) | Runtime flag configuration, annotations, constants, overrides and flag tests |
| [validation.instructions.md](instructions/validation.instructions.md) | Validators, request models and composed validation annotations |
| [models.instructions.md](instructions/models.instructions.md) | `models/` (dataModels, requestModels, viewModels) |
| [messages.instructions.md](instructions/messages.instructions.md) | `messages/` (i18n YAML files) |
| [exceptions.instructions.md](instructions/exceptions.instructions.md) | Exceptions, journey exceptions and error-handling guidance |
| [helpers.instructions.md](instructions/helpers.instructions.md) | `helpers/`, `extensions/`, `converters/` |
| [clients.instructions.md](instructions/clients.instructions.md) | `clients/` (external API clients) |
| [constants.instructions.md](instructions/constants.instructions.md) | `constants/`, `enums/` |
| [config.instructions.md](instructions/config.instructions.md) | Runtime configuration, security, filters, interceptors, bootstrap and application YAML |
| [scheduled-tasks.instructions.md](instructions/scheduled-tasks.instructions.md) | `application/` runners and task annotations/conditions |
| [emails.instructions.md](instructions/emails.instructions.md) | Email Markdown/metadata, personalization models and Notify contract tests |
| [build-ci.instructions.md](instructions/build-ci.instructions.md) | Gradle, root frontend build configuration and GitHub workflows |
| [reviewing-code.instructions.md](instructions/reviewing-code.instructions.md) | Cross-cutting review priorities |

## Important: Follow Existing Patterns

Before implementing new functionality, search the codebase for similar examples and follow the established patterns. This codebase has well-defined conventions for:
- Controllers and their corresponding tests
- Services and repository interactions
- Multi-step form journeys (see `journeys/`, request form models and shared templates)
- Integration tests with page objects
- Feature flags and conditional behaviour
- Local API stubs for third-party services

When in doubt, find an existing implementation that does something similar and use it as a template.

## Worktree Management

This repo uses git worktrees for parallel development. Scripts are in `scripts/git-worktrees/` (PowerShell and Bash).

**PowerShell:**
```powershell
# Create a new worktree with a branch
.\scripts\git-worktrees\new-worktree.ps1 -WorktreeName "pdjb-123" -BranchName "feat/PDJB-123-my-feature"

# Switch branch in current worktree
.\scripts\git-worktrees\switch-worktree.ps1 -BranchName "feat/PDJB-456-other-work"

# Remove a worktree
.\scripts\git-worktrees\remove-worktree.ps1 -WorktreePath "pdjb-123"
```

**Bash:**
```bash
# Create a new worktree with a branch
./scripts/git-worktrees/new-worktree.sh pdjb-123 feat/PDJB-123-my-feature

# Switch branch in current worktree
./scripts/git-worktrees/switch-worktree.sh feat/PDJB-456-other-work

# Remove a worktree
./scripts/git-worktrees/remove-worktree.sh pdjb-123
```

The `new-worktree` script automatically copies gitignored config files (`.env`, copilot instructions, etc.) into the new
worktree and runs `npm install`.

## Build, Test, and Lint Commands

**PowerShell** (use `.\gradlew`):
```powershell
.\gradlew build                  # Full build/check lifecycle, including frontend assets
.\gradlew test                   # JVM tests, including integration tests
.\gradlew testWithoutIntegration # Excludes the integration package; some tests still need Docker
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.LandlordControllerTests"          # Single test class
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.LandlordControllerTests.index returns a redirect for unauthenticated user"
.\gradlew ktlintCheck            # Lint with Ktlint
.\gradlew ktlintFormat           # Auto-format with Ktlint
npm test                         # Run frontend JS tests
npm run build                    # Build frontend assets only
```

**Bash** (use `./gradlew`):
```bash
./gradlew build                  # Full build/check lifecycle, including frontend assets
./gradlew test                   # JVM tests, including integration tests
./gradlew testWithoutIntegration # Excludes the integration package; some tests still need Docker
./gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.LandlordControllerTests"          # Single test class
./gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.LandlordControllerTests.index returns a redirect for unauthenticated user"
./gradlew ktlintCheck            # Lint with Ktlint
./gradlew ktlintFormat           # Auto-format with Ktlint
npm test                         # Run frontend JS tests
npm run build                    # Build frontend assets only
```

Container-backed tests require Docker for shared PostgreSQL and Redis containers, including some context tests outside
the `integration` package. JVM compilation/testing also builds and copies frontend assets, so Node/npm and frontend
dependencies are prerequisites. Node tests run separately. See [build-ci.instructions.md](instructions/build-ci.instructions.md).

## Line Endings

This repository uses **LF** line endings (enforced by `.editorconfig`). On Windows, the `create` and `edit` tools write
files with CRLF line endings by default. After creating or editing files, convert them to LF before committing:

```powershell
# Convert a single file to LF
(Get-Content .\path\to\file.kt -Raw) -replace "`r`n", "`n" | Set-Content -NoNewline .\path\to\file.kt
```

## Temporary Files

Never create temporary files (diffs, scripts, command output, snapshots) in the repository working directory. Always use
the session workspace at `~/.copilot/session-state/<session-id>/files/` for any temporary artifacts. This keeps the repo
clean and avoids untracked files appearing in `git status`.

## Architecture

### Spring Boot + Kotlin + Thymeleaf Stack
The webapp is a Kotlin Spring Boot application using Thymeleaf templates with the GOV.UK Design System. Frontend assets (JS/SCSS) are bundled via Rollup.

### Package Structure
- `annotations/` - Custom Spring annotations (`@PrsdbController`, `@PrsdbWebService`, `@PrsdbRestController`, `@PrsdbTaskService`, `@PrsdbWebComponent`, etc.)
- `application/` - Scheduled task runners
- `clients/` - External HTTP/SDK adapters and profile-selected stubs
- `config/` - Spring configuration, security, filters, interceptors
- `constants/` - Constants and domain enums
- `controllers/` - HTTP endpoints
- `database/entity/`, `database/repository/`, `database/dao/` - JPA mappings, repositories and bulk-loading data access
- `exceptions/` - Custom exception classes
- `helpers/` - Converters, extension functions, utility helpers
- `journeys/` - Graph-based journey DSL, steps/configurations, reusable tasks and typed state
- `local/api/` - Local development stubs for third-party APIs (annotated with `@Profile("local")`)
- `models/` - Data models, request/form models, view models
- `services/` - Business logic
- `validation/` - Custom validation framework (`@ValidatedBy` + `PropertyConstraintValidator`)

### Custom Annotations
Choose annotations by purpose and lifecycle:
- `@PrsdbController` / `@PrsdbRestController` — for controllers
- `@PrsdbWebService` / `@PrsdbTaskService` — for services
- `@PrsdbWebComponent` — for web-only components
- `@JourneyFrameworkComponent` — for prototype-scoped journey components
- `@PrsdbWebConfiguration` / `@PrsdbTaskConfiguration` — for config beans
- `@PrsdbControllerAdvice` — for exception handlers

Web wrappers exclude beans when `web-server-deactivated` is active; task wrappers require that profile.
Intentionally shared beans, such as Notify, auditing and feature configuration, retain plain Spring stereotypes.
Runner annotations (`@PrsdbTask` / `@PrsdbScheduledTask`) are distinct from task service/configuration wrappers.

### Multi-Step Form Framework ("Journeys")
Complex forms use `JourneyBuilder.journey(state)` to assemble requestable/internal steps and reusable task subjourneys,
optionally grouped into sections. Factories provide routing maps to `JourneyStepDispatcher`; navigation destinations
and parentage/reachability are separate concerns. Requestable configurations bind form models and render templates.
See [journeys.instructions.md](instructions/journeys.instructions.md) for state, prototype scope and current examples.

### Feature Flags (FF4J)
Feature flags are configured in `application.yml` under `features`. See `feature-flags.instructions.md` for full details.
- `@AvailableWhenFeatureEnabled` / `@AvailableWhenFeatureDisabled` on endpoints
- `FeatureFlagManager.checkFeature` for runtime decisions; `@PrsdbFlip` also supports service switching
- Flag names in `FeatureFlagNames.kt`, release names in `FeatureFlagReleaseNames.kt`
- Startup validation compares bound flag/release configuration with the registered name lists
- Session overrides are development-only by policy and disabled in base configuration; their guard is property-based

### Authentication
Uses GOV.UK One Login and, for local councils, Internal Access OAuth2. `local-no-auth` uses local mock providers;
roles still depend on the selected user's database records and the relevant security-chain role mapper. The default
seeded One Login user supports all four roles, but alternative identities, seed data and callback routes can differ.

## Conventions

### Database Migrations
Flyway migrations go in `src/main/resources/db/migrations/` with naming: `V<major>_<minor>_<fix>__<name>.sql`. See `database.instructions.md` for entity and repository patterns.

### Frontend Assets
Uses GOV.UK Frontend and Ministry of Justice Frontend, with versions in `package.json` / `package-lock.json`.
See [frontend.instructions.md](instructions/frontend.instructions.md) for templates, SCSS, JS and fragments.

### Integration Tests
Browser tests use Playwright (`@UsePlaywright`), shared page objects/components and accessibility assertions.
See [integration-tests.instructions.md](instructions/integration-tests.instructions.md) for fixtures and Navigator patterns.

### Email Templates
Email templates are markdown files in `src/main/resources/emails/`. Template IDs for Notify service are configured in `emailTemplates.json`.
See [emails.instructions.md](instructions/emails.instructions.md) for personalization, remote template versioning and contract checks.

### Spring Profiles
- `local` - Local development with Docker Compose dependencies
- `local-no-auth` - Local mock OAuth2 providers
- `local-org-landlord` - Alternative organisational-landlord mock identity
- `local-auth` - Real One Login integration environment
- `use-notify` - Enable real email sending via Notify
- `web-server-deactivated` + `scheduled-task` + the exact task-name profile - Select a scheduled runner

### Pull requests
When creating a pull request, use the template defined in [pull_request_template.md](pull_request_template.md).

# Agent mode

- DO NOT try and commit changes you make - I will do that manually when I'm satisfied with the results.
- DO NOT try and run the linter after each change - It will run when I commit, and I'll ask you to fix any errors that occur, or I'll fix them myself.
- ALWAYS ask before running the tests - I will check your changes are what I wanted before spending time running and debugging tests.

These rules are suspended when the `development-workflow` skill is active, as that skill has its own user-approval gates for commits, tests, and linting.
