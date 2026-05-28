---
name: writing-plans
description: Use when you have a spec or requirements for a multi-step task, before touching code. Produces implementation plans tailored to the prsdb-webapp codebase.
---

# Writing Plans

Write comprehensive implementation plans assuming the implementing agent has zero
context for this codebase. Document everything needed: which files to touch, code
examples, testing approach, and verification commands.

**Save plans to:** `~/.copilot/session-state/<session-id>/files/plan.md`
Plans are session artifacts and must NOT be committed to the repository.

## Scope Check

If the spec covers multiple independent subsystems, suggest breaking into separate
plans — one per subsystem. Each plan should produce working, testable software on
its own.

## File Structure

Before defining tasks, map out which files will be created or modified:

- Follow existing package structure conventions (see below)
- Each file should have one clear responsibility
- Files that change together should live together
- Follow established patterns — do not restructure without agreement

### Package Layout Reference

```
src/main/kotlin/uk/gov/communities/prsdb/webapp/
├── annotations/        Custom Spring annotations
├── application/        Scheduled task runners
├── clients/            External API clients
├── config/             Spring configuration, security, filters
├── constants/          Constants and domain enums
├── controllers/        HTTP endpoints (@PrsdbController)
├── database/entity/    JPA entities
├── database/repository/ JPA repositories
├── exceptions/         Custom exception classes
├── forms/              Multi-step form page classes
├── helpers/            Converters, extensions, utilities
├── journeys/           Journey state management
├── local/api/          Local dev stubs (@Profile("local"))
├── models/             Data, request, and view models
├── services/           Business logic (@PrsdbWebService)
└── validation/         Custom validation framework
```

## Plan Document Header

Every plan MUST start with:

```markdown
# [Feature Name] Implementation Plan

**Goal:** [One sentence]

**Architecture:** [2-3 sentences about approach]

**Tech Stack:** Kotlin, Spring Boot, Thymeleaf, GOV.UK Frontend, PostgreSQL
```

## TODO Table

After the header, include a table mapping Jira acceptance criteria and codebase
TODO comments to plan tasks:

```markdown
## Jira ToDos

| ToDo | Addressed In |
|------|-------------|
| [Acceptance criterion from ticket] | PR N: Task M |
| `// TODO PDJB-XXX: [comment text]` in `SomeFile.kt:42` | PR N: Task M |
| [Item intentionally not addressed] | Out of scope — [reason] |
```

## Task Structure

Each task must include exact file paths, code, and verification commands:

````markdown
### Task N: [Component Name]

**Files:**
- Create: `src/main/kotlin/.../NewFile.kt`
- Modify: `src/main/kotlin/.../ExistingFile.kt`
- Test: `src/test/kotlin/.../NewFileTests.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `descriptive test name in backticks`() {
    // arrange, act, assert
}
```

- [ ] **Step 2: Run test to verify it fails**

PowerShell:
```powershell
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.TestClass.descriptive test name in backticks" --console=plain
```
Bash:
```bash
./gradlew test --tests "uk.gov.communities.prsdb.webapp.TestClass.descriptive test name in backticks" --console=plain
```

- [ ] **Step 3: Write minimal implementation**

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**
````

## Project-Specific Conventions

Plans must account for these patterns:

### Custom Annotations
- Controllers: `@PrsdbController` (not `@Controller`)
- Services: `@PrsdbWebService` (not `@Service`)
- Components: `@PrsdbWebComponent` (not `@Component`)
- Config: `@PrsdbWebConfiguration` (not `@Configuration`)

### Entities
- Extend `AuditableEntity` or `ModifiableAuditableEntity`
- Flyway migrations in `src/main/resources/db/migrations/`
- Naming: `V<major>_<minor>_<fix>__<description>.sql`

### Journey Framework
- StepId enums implement `StepId` or `GroupedStepId<T>`
- Steps extend `RequestableStep`, annotated `@JourneyFrameworkComponent`
- Journey factories annotated `@PrsdbWebService`

### Validation
- Form models annotated `@IsValidPrioritised`
- Properties annotated `@ValidatedBy` with `ConstraintDescriptor` entries
- Do NOT use standard Bean Validation (`@NotBlank`, `@Size`, etc.)

### Testing
- Unit tests: JUnit 5 + Mockito Kotlin, backtick method names
- Controller tests: `@WebMvcTest` extending `ControllerTest`
- Integration tests: `@UsePlaywright` extending `IntegrationTest*` base classes
- Journey step tests: test step configuration compliance

### Feature Flags
- Non-bug-fix user-visible changes need feature flags
- Flag names in `FeatureFlagNames.kt`, releases in `FeatureFlagReleaseNames.kt`
- Endpoint: `@AvailableWhenFeatureEnabled("FLAG_NAME")`

## Verification Strategy

Every plan must include a verification strategy covering:

1. **TDD suitability** — which tests to write first
2. **Test types** — unit, controller, journey step config, integration, frontend JS
3. **Full suite** — whether needed (~20 min; prefer targeted)
4. **Smoke test** — unless no observable runtime effect
5. **Figma comparison** — if UI/content changes

## No Placeholders

Every step must contain actual content. These are plan failures:
- "TBD", "TODO", "implement later"
- "Add appropriate error handling"
- "Write tests for the above" (without actual test code)
- "Similar to Task N" (repeat the content)
- Steps that describe what to do without showing how

## Self-Review

After writing the complete plan:
1. **Spec coverage:** Can every requirement point to a task?
2. **Placeholder scan:** Any vague steps remaining?
3. **Type consistency:** Do names match across tasks?

Fix inline. Do not re-review.
