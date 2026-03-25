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

The project defines custom stereotype annotations that wrap standard Spring annotations with a
`@Conditional(WebServerOnly::class)` guard. These exist so that web components are not loaded when the application
runs in task-runner mode. Code should use:

- `@PrsdbController` instead of `@Controller`
- `@PrsdbWebService` instead of `@Service`
- `@PrsdbWebComponent` instead of `@Component`
- `@PrsdbWebConfiguration` instead of `@Configuration`

For scheduled task components, the equivalents are `@PrsdbTask` and `@PrsdbScheduledTask`.

Any new controller, service, component, or configuration class that uses a bare Spring stereotype annotation instead
of the project wrapper is a review finding.

## Dependency Injection

All dependencies are injected via constructor parameters. There should be no use of `@Autowired` on fields or setters.
Constructor parameters should be `private val` unless there is a specific reason to expose them.

## Controllers

Controllers follow this structure:

- Annotated with `@PrsdbController` (not `@Controller`)
- Role-based access via `@PreAuthorize("hasAnyRole('LANDLORD')")` or equivalent at the class level
- `@RequestMapping` for the base URL path
- Dependencies injected via constructor

If a new controller or endpoint is added without a `@PreAuthorize` annotation, or uses a bare `@Controller`, flag it.

## Journey Framework

The journey framework is the project's custom multi-step form system. It has a specific component hierarchy:

```
Journey → Section → Task → Step → Page
```

Key patterns to check:

- **StepId enums** implement `StepId` or `GroupedStepId<T>` and define `urlPathSegment` values
- **Step configurations** extend `AbstractRequestableStepConfig` and are annotated with `@JourneyFrameworkComponent`
- **Step classes** extend `RequestableStep` and are annotated with `@JourneyFrameworkComponent`, with a companion
  object defining the `ROUTE_SEGMENT` constant
- **Journey factories** are annotated with `@PrsdbWebService` and use the journey builder DSL
- **Form models** implement `FormModel` and are annotated with `@IsValidPrioritised`

New journey code that does not follow this hierarchy, or that skips the builder DSL in favour of manual wiring,
is a review finding.

## Validation

The project uses a custom prioritised validation framework rather than standard Bean Validation annotations like
`@NotBlank` or `@Size`. The pattern is:

1. Annotate the form model class with `@IsValidPrioritised`
2. Annotate individual properties with `@ValidatedBy`, specifying one or more `ConstraintDescriptor` entries
3. Each `ConstraintDescriptor` references a `messageKey` (for i18n error messages) and a `validatorType`
   (a `PrioritisedConstraintValidator` implementation)

New form models that use standard Bean Validation annotations instead of `@ValidatedBy` / `@IsValidPrioritised` are
a review finding. Validation error messages should always use message keys from the messages properties file, not
hardcoded strings.

## Entities and Auditing

All JPA entities should extend one of:

- `AuditableEntity` — provides an auto-populated `createdDate`
- `ModifiableAuditableEntity` — extends the above with `lastModifiedDate`

Most entities that are updated after creation should extend `ModifiableAuditableEntity`. New entities that do not
extend either base class, or that define their own timestamp fields, are a review finding.

Entity properties that should not be set externally should use `private set`.

## Services and Transactions

- Services are annotated with `@PrsdbWebService` (not `@Service`)
- Methods that write to the database should be annotated with `@Transactional`
- Service methods should not catch and silently swallow exceptions from repository calls

## Feature Flags

Feature flags use FF4J. The patterns are:

- **Endpoint-level**: `@AvailableWhenFeatureEnabled("FLAG_NAME")` or `@AvailableWhenFeatureDisabled("FLAG_NAME")`
  on controller methods
- **Bean-level**: `@PrsdbFlip(name = "FLAG_NAME", alterBean = "bean-name")` on interface methods, with separate
  flag-on and flag-off implementation beans
- **Programmatic**: `featureFlagManager.checkFeature("FLAG_NAME")` in service or controller logic

Flag names should be defined as constants, not inline strings. New feature-flagged code should follow whichever
of the above patterns is most appropriate for the scope of the flag.

## Database Migrations

Flyway migrations follow the naming convention `V<MAJOR>_<MINOR>_<PATCH>__<description>.sql` and live in
`src/main/resources/db/migrations/`.

Review points:

- New tables should include `created_date TIMESTAMPTZ(6) DEFAULT current_timestamp NOT NULL` at minimum,
  and `last_modified_date TIMESTAMPTZ(6)` if the entity is modifiable
- Column types should be consistent with existing tables (e.g. `TIMESTAMPTZ(6)` for timestamps, `BIGINT` for IDs
  with `GENERATED BY DEFAULT AS IDENTITY`)
- Foreign key constraints should be explicit
- Migration version numbers should not conflict with existing migrations

## Testing

The project has several test layers:

- **Unit tests**: JUnit 5 with Mockito Kotlin. Use `@MockitoBean` for Spring-managed mocks.
- **Controller tests**: `@WebMvcTest` extending the `ControllerTest` base class, which sets up MockMvc with
  security configuration. New controller tests that do not extend `ControllerTest` may be missing security
  filter setup.
- **Integration tests**: Playwright-based, using TestContainers for PostgreSQL. Base classes
  (`IntegrationTest`, `IntegrationTestWithMutableData`, `IntegrationTestWithImmutableData`) handle DB lifecycle.
  Integration tests that do not extend the appropriate base class are a review finding.

Test method names use backtick-quoted descriptive strings (e.g. `` `submitting feedback escapes brackets and redirects` ``).

## Security

Beyond `@PreAuthorize` on controllers, review for:

- CSRF tokens: forms submitted via POST must include CSRF protection (Spring handles this via Thymeleaf, but
  custom AJAX calls or REST endpoints need explicit handling)
- No secrets, API keys, or credentials in source code
- New routes: any new URL pattern must be covered by the appropriate security filter chain
  (`LandlordSecurityConfig`, `LocalCouncilSecurityConfig`, or `DefaultSecurityConfig`)

## What Not to Flag

- Style and formatting issues covered by ktlint (indentation, spacing, import ordering)
- Code that follows an established pattern in the codebase, even if the pattern is not ideal
- Minor naming preferences where the chosen name is clear and consistent with surrounding code
