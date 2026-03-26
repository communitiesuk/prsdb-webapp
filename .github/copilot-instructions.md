# Copilot Instructions for PRSDB Webapp

## Path-Specific Instructions

Context-aware instructions are available for different parts of the codebase. These are automatically applied based on the files you're working with:

| Instruction File | Applies To |
|------------------|------------|
| [journeys.instructions.md](instructions/journeys.instructions.md) | `journeys/`, `forms/` |
| [controllers.instructions.md](instructions/controllers.instructions.md) | `controllers/` |
| [services.instructions.md](instructions/services.instructions.md) | `services/` |
| [database.instructions.md](instructions/database.instructions.md) | `database/`, `db/migrations/` |
| [integration-tests.instructions.md](instructions/integration-tests.instructions.md) | `integration/` tests |
| [unit-tests.instructions.md](instructions/unit-tests.instructions.md) | Unit tests (`src/test/`) |
| [frontend.instructions.md](instructions/frontend.instructions.md) | `templates/`, `css/`, `js/` |
| [feature-flags.instructions.md](instructions/feature-flags.instructions.md) | `featureFlags/`, flag annotations |
| [validation.instructions.md](instructions/validation.instructions.md) | `validation/` |
| [models.instructions.md](instructions/models.instructions.md) | `models/` (dataModels, requestModels, viewModels) |
| [messages.instructions.md](instructions/messages.instructions.md) | `messages/` (i18n YAML files) |
| [exceptions.instructions.md](instructions/exceptions.instructions.md) | `exceptions/` |
| [helpers.instructions.md](instructions/helpers.instructions.md) | `helpers/`, `extensions/`, `converters/` |
| [clients.instructions.md](instructions/clients.instructions.md) | `clients/` (external API clients) |
| [constants.instructions.md](instructions/constants.instructions.md) | `constants/`, `enums/` |
| [config.instructions.md](instructions/config.instructions.md) | `config/`, `security/`, `filters/`, `interceptors/` |
| [scheduled-tasks.instructions.md](instructions/scheduled-tasks.instructions.md) | `application/` (scheduled task runners) |

## Important: Follow Existing Patterns

Before implementing new functionality, search the codebase for similar examples and follow the established patterns. This codebase has well-defined conventions for:
- Controllers and their corresponding tests
- Services and repository interactions
- Multi-step form journeys (see `forms/` and `journeys/` packages)
- Integration tests with page objects
- Feature flags and conditional behaviour
- Local API stubs for third-party services

When in doubt, find an existing implementation that does something similar and use it as a template.

## Worktree Management

This repo uses git worktrees for parallel development. Scripts are in `scripts/git-worktrees/` (PowerShell and Bash).

```powershell
# Create a new worktree with a branch
.\scripts\git-worktrees\new-worktree.ps1 -WorktreeName "pdjb-123" -BranchName "feat/PDJB-123-my-feature"

# Switch branch in current worktree
.\scripts\git-worktrees\switch-worktree.ps1 -BranchName "feat/PDJB-456-other-work"

# Remove a worktree
.\scripts\git-worktrees\remove-worktree.ps1 -WorktreePath "pdjb-123"
```

The `new-worktree` script automatically copies gitignored config files (`.env`, copilot instructions, etc.) into the new
worktree and runs `npm install`.

## Build, Test, and Lint Commands

```powershell
# Build (includes compiling frontend assets via npm)
.\gradlew build

# Run all tests
.\gradlew test

# Run unit tests only (excludes integration tests - faster, no Docker needed)
.\gradlew testWithoutIntegration

# Run a single test class
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.LandlordControllerTests"

# Run a single test method
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.LandlordControllerTests.someTestMethod"

# Lint with Ktlint
.\gradlew ktlintCheck

# Auto-format with Ktlint
.\gradlew ktlintFormat

# Run frontend JS tests
npm test

# Build frontend assets only
npm run build
```

Integration tests require Docker running (uses testcontainers for PostgreSQL).

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
- `clients/` - External API clients (EPC Register, OS Downloads)
- `config/` - Spring configuration, security, filters, interceptors
- `constants/` - Constants and domain enums
- `controllers/` - HTTP endpoints
- `database/entity/` and `database/repository/` - JPA entities and repositories
- `exceptions/` - Custom exception classes
- `forms/` - Multi-step form page classes (`AbstractPage` hierarchy)
- `helpers/` - Converters, extension functions, utility helpers
- `journeys/` - Journey state management with step-by-step flow
- `local/api/` - Local development stubs for third-party APIs (annotated with `@Profile("local")`)
- `models/` - Data models, request/form models, view models
- `services/` - Business logic
- `validation/` - Custom validation framework (`@ValidatedBy` + `PropertyConstraintValidator`)

### Custom Annotations
The project uses custom Spring annotations instead of plain Spring annotations:
- `@PrsdbController` / `@PrsdbRestController` — for controllers
- `@PrsdbWebService` / `@PrsdbTaskService` — for services
- `@PrsdbWebComponent` — for journey factories and other components
- `@PrsdbWebConfiguration` / `@PrsdbTaskConfiguration` — for config beans
- `@PrsdbControllerAdvice` — for exception handlers

### Multi-Step Form Framework ("Journeys")
Complex forms use a journey framework with step-by-step flows. See `journeys.instructions.md` for full details.

Journey hierarchy: `Journey` → `JourneyWithTaskList` → `UpdateJourney` → `GroupedUpdateJourney`

Page hierarchy: `AbstractPage` → `Page`, `PageWithContentProvider`, `FileUploadPage`, `CheckAnswersPage`

Journeys are instantiated via factory classes annotated with `@PrsdbWebComponent`.

### Feature Flags (FF4J)
Feature flags are configured in `application.yml` under `features`. See `feature-flags.instructions.md` for full details.
- `@AvailableWhenFeatureEnabled` / `@AvailableWhenFeatureDisabled` on endpoints
- `@PrsdbFlip` for service method switching
- Flag names in `FeatureFlagNames.kt`, release names in `FeatureFlagReleaseNames.kt`
- Strict startup validation ensures YAML config and code constants stay in sync

### Authentication
Uses GOV.UK One Login OAuth2. For local development, the `local-no-auth` profile provides a mock that auto-authenticates with all roles.

## Conventions

### Database Migrations
Flyway migrations go in `src/main/resources/db/migrations/` with naming: `V<major>_<minor>_<fix>__<name>.sql`. See `database.instructions.md` for entity and repository patterns.

### Frontend Assets
Uses GOV.UK Frontend 5.11.0 + Ministry of Justice Frontend 3.3.1. See `frontend.instructions.md` for templates, SCSS, JS, and fragment conventions.

### Integration Tests
Tests use Playwright (`@UsePlaywright`) with page object pattern and 35+ reusable components. See `integration-tests.instructions.md` for base classes, Navigator, and naming conventions.

### Email Templates
Email templates are markdown files in `src/main/resources/emails/`. Template IDs for Notify service are configured in `emailTemplates.json`.

### Spring Profiles
- `local` - Local development with Docker Compose dependencies
- `local-no-auth` - Mock One Login authentication
- `local-auth` - Real One Login integration environment
- `use-notify` - Enable real email sending via Notify
- `web-server-deactivated` + `scheduled-task` - Run scheduled tasks locally

### Pull requests
When creating a pull request, use the template defined in [pull_request_template.md](pull_request_template.md).

