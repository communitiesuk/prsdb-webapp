---
applyTo: "**"
---

# PRSDB Code Review Priorities

The overriding principle when reviewing code in this project is **consistency with the existing codebase**. Look for
places where new code diverges from established patterns rather than evaluating it in isolation. If existing code
follows the same approach as the code under review, it is not a review issue — even if a different approach might be
theoretically better.

If a wider refactor is justified, it may be mentioned but should not block the review.

## Spring Annotations

Choose stereotypes by the bean's purpose and lifecycle. The web wrappers use
`@Conditional(WebServerOnly::class)` so web-only components are not loaded in task mode:

- `@PrsdbController` instead of `@Controller`
- `@PrsdbWebService` instead of `@Service`
- `@PrsdbWebComponent` instead of `@Component`
- `@PrsdbWebConfiguration` instead of `@Configuration`

For task-only collaborators, use `@PrsdbTaskService` and `@PrsdbTaskConfiguration`. `@PrsdbTask` and
`@PrsdbScheduledTask` are runner annotations, not substitutes for every task-mode bean.

Shared web/task beans legitimately use plain Spring stereotypes: examples include `AuditingConfig`, `NotifyConfig`,
`FeatureFlagConfig`, `NotifyEmailNotificationService` and `AbsoluteUrlProvider`. Flag an inappropriate lifecycle,
not the presence of a bare annotation alone. Replacing a shared bean with a web-only wrapper can break tasks.

## Dependency Injection

Production services and controllers use constructor injection, not `@Autowired` fields or setters.
Constructor parameters should be `private val` unless there is a specific reason to expose them.
Spring test fixtures legitimately use injected fields, `@MockitoBean` and `@MockitoSpyBean`; do not apply the
production injection rule to those fixtures.

## Controllers

Controllers follow this structure:

- Annotated with `@PrsdbController` (not `@Controller`)
- Role-based access at class level for uniform permissions, or method level for mixed-role controllers
- `@RequestMapping` for the base URL path
- Dependencies injected via constructor

Review effective security-chain matching, class/method authorisation and service-owned resource access checks.
Public registration, invitation and local mock endpoints intentionally differ from protected pages; absence of a
`@PreAuthorize` token alone is not a finding. Controllers must invoke the appropriate service guard before
dispatching protected operations.

## Journey Framework

The journey framework assembles directed graphs using the journey-builder DSL. Journeys can contain steps directly
or reusable task subjourneys, with optional flat sections. Requestable steps use configurations, form models and
templates, not a `Page` subclass. See [journeys.instructions.md](journeys.instructions.md).

Key patterns to check:

- **Requestable steps** pair `AbstractRequestableStepConfig` with `JourneyStep.RequestableStep` and a companion
  `ROUTE_SEGMENT`; enums express modes/outcomes, not step identifiers
- **Internal steps** use `AbstractInternalStepConfig` and `JourneyStep.InternalStep`, with no request URL
- **Stateful framework components** use prototype-scoped `@JourneyFrameworkComponent`
- **Navigation and reachability** are configured separately through destinations and parentage
- **Journey factories** use the DSL and fresh state; `@PrsdbWebService` is common, but prototype factories also exist
- **Journey form models** implement `FormModel`; prioritised validation requires `@IsValidPrioritised`

Flag bypasses of the established builder/state lifecycle, not omission of an optional task or section.
Flag-off and restructured flows can coexist within this same framework.

## Validation

Prefer the custom prioritised validation framework for new form validation. The pattern is:

1. Annotate the form model class with `@IsValidPrioritised`
2. Use `@ValidatedBy` or an existing composed validation annotation on properties
3. Each `ConstraintDescriptor` references a `messageKey` (for i18n error messages) and a `validatorType`
   that is a subtype of `PrioritisedConstraintValidator`

Value validators implement `PropertyConstraintValidator`; delegated constraints use
`DelegatedPropertyConstraintValidator::class` and a no-argument Boolean `targetMethod`. Existing live forms also
contain Jakarta constraints, sometimes mixed with prioritised constraints; do not demand unrelated migrations.
Check new validation against the prioritised default and relevant form family.
Validation errors use YAML message keys, not hardcoded human-readable strings.

## Entities and Auditing

Ordinary audited domain entities extend one of:

- `AuditableEntity` — provides an auto-populated `createdDate`
- `ModifiableAuditableEntity` — extends the above with `lastModifiedDate`

Entities updated after creation generally use `ModifiableAuditableEntity`. Check the mapping's role before flagging
a missing superclass: composite-key/link and projection mappings such as `LandlordIncompleteProperties` and
`LocalCouncilUserOrInvitation` have different established contracts. Do not duplicate audit fields on ordinary entities.

Use restricted setters for properties that must not be set externally, and preserve encapsulated relationship
collections and their mutation methods.

## Services and Transactions

- Services use the lifecycle-appropriate stereotype described above
- Database mutations need appropriate transaction boundaries; the established annotation is `jakarta.transaction.Transactional`
- Managed-entity dirty checking is valid; an explicit repository `save()` is not required for every update
- Service methods should not catch and silently swallow exceptions from repository calls
- Preserve stale-update checks and use the existing after-commit helper for side effects that depend on a successful commit

## Feature Flags

Feature flags use FF4J. The patterns are:

- **Endpoint-level**: `@AvailableWhenFeatureEnabled(FLAG_NAME)` or `@AvailableWhenFeatureDisabled(FLAG_NAME)`
  on controller methods
- **Bean-level**: supported `@PrsdbFlip(name = FLAG_NAME, alterBean = "bean-name")` on interface methods, with separate
  flag-on and flag-off implementation beans
- **Programmatic**: `featureFlagManager.checkFeature(FLAG_NAME)` in service or controller logic

Flag names should be defined as constants, not inline strings. New feature-flagged code should follow whichever
of the above patterns is most appropriate for the scope of the flag.
Check registration lists and profile configuration together. Session overrides are development-only by policy and
property-gated; do not assume an environment-level production guard exists.

## Database Migrations

Flyway migrations follow the naming convention `V<MAJOR>_<MINOR>_<PATCH>__<description>.sql` and live in
`src/main/resources/db/migrations/`.

Review points:

- New audited domain tables should include `created_date TIMESTAMPTZ(6) DEFAULT current_timestamp NOT NULL` at minimum,
  and `last_modified_date TIMESTAMPTZ(6)` if the entity is modifiable
- Column types should be consistent with existing tables (e.g. `TIMESTAMPTZ(6)` for timestamps, `BIGINT` for IDs
  with `GENERATED BY DEFAULT AS IDENTITY`)
- Foreign key constraints should be explicit
- Migration version numbers should not conflict with existing migrations

## Testing

The project has several test layers:

- **Unit tests**: JUnit 5 with Mockito Kotlin. Use `@MockitoBean` for Spring-managed mocks.
- **Controller tests**: `@WebMvcTest` extending `ControllerTest` directly or through a shared intermediate base.
  Check inherited security/filter setup rather than requiring direct inheritance.
- **Browser integration tests**: Playwright-based, using shared Testcontainers for PostgreSQL and Redis. Base classes
  (`IntegrationTest`, `IntegrationTestWithMutableData`, `IntegrationTestWithImmutableData`) handle DB lifecycle.
  Page transitions should use the existing page-creation helpers, preserving URL and accessibility assertions.
- **Journey unit tests**: configurations, routing, tasks and state can be exercised without a browser or Spring context
- **Other tests**: context tests can live outside the integration package; frontend tests use Node rather than JUnit

Prefer backtick-quoted descriptive Kotlin test names (e.g. `` `submitting feedback escapes brackets and redirects` ``).
Existing conventional names such as `contextLoads`, `kotlin.test` assertions and suitable Jupiter parameter sources
are not reasons for unrelated rewrites.

## Security

Beyond `@PreAuthorize` on controllers, review for:

- CSRF tokens: forms submitted via POST must include CSRF protection (Spring handles this via Thymeleaf, but
  custom AJAX calls or REST endpoints need explicit handling)
- No secrets, API keys, or credentials in source code
- New routes: any new URL pattern must be covered by the appropriate security filter chain
  (`LandlordSecurityConfig`, `LocalCouncilSecurityConfig`, `LettingAgentSecurityConfig`, or `DefaultSecurityConfig`)

## What Not to Flag

- Style and formatting issues covered by ktlint (indentation, spacing, import ordering)
- Code that follows an established pattern in the codebase, even if the pattern is not ideal
- Minor naming preferences where the chosen name is clear and consistent with surrounding code
