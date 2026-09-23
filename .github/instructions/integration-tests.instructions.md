---
applyTo: "src/test/kotlin/**/integration/**,src/test/kotlin/**/SharedContainers.kt,src/test/kotlin/**/TestcontainersConfiguration.kt,src/test/kotlin/**/testHelpers/IntegrationTestHelper.kt,src/test/kotlin/**/journeys/JourneyFrameworkComponentTests.kt,src/test/kotlin/**/testHelpers/builders/*State*Builder.kt,src/test/resources/**/*.sql,src/test/resources/application*.yml,src/test/resources/docker-java.properties"
---

# Integration Test Instructions

## Documentation Reference
- Full guide: [docs/IntegrationTestReadMe.md](../../docs/IntegrationTestReadMe.md)
- Shared Kotlin conventions and test layers: [unit-tests.instructions.md](unit-tests.instructions.md)
- Migrations and reference data: [database.instructions.md](database.instructions.md)
- Test commands, JVM versus Node tests, sharding and Docker requirements: [build-ci.instructions.md](build-ci.instructions.md)

This scope includes browser tests and their shared container, database-reset, journey-session and resource fixtures,
plus `journeys/JourneyFrameworkComponentTests.kt`, which extends `IntegrationTest` outside the `integration/` directory.
It does not classify all Kotlin tests as integration tests.

## Test Base Classes

### Choose the Right Base Class
- `IntegrationTestWithMutableData`: Resets and seeds the DB before **each test** (`@BeforeEach`; tests modify data)
- `IntegrationTestWithImmutableData`: Resets and seeds the DB before **each class** (`@BeforeAll`; tests do not modify DB data)
- `IntegrationTest`: No DB reset or seed; valid for contexts needing no seeded data, or when all seeding is supplied by nested fixtures

Do not rely on data left behind by another test class.

The base [IntegrationTest](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/IntegrationTest.kt) supplies
inherited `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `@TestInstance(PER_CLASS)`, `@UsePlaywright` and
`@ActiveProfiles("local", "local-no-auth")` defaults, and imports `TestcontainersConfiguration`.
Subclasses can add profiles, for example through `@WithOrgLandlordProfile`; the `integration` profile is **not**
activated by this base class merely because `application-integration.yml` exists.

### Nested Classes
- `NestedIntegrationTestWithMutableData`: For tests needing different seed data
- `NestedIntegrationTestWithImmutableData`: Same, for read-only tests

### Shared Context Setup
- Reuse inherited mocks for Notify, One Login identity, S3 and OS downloads, and spies for the client-registration
  repository and `FeatureFlagManager`; add scenario-specific stubbing rather than redeclaring the common setup
- Base `@BeforeEach` methods adjust local OAuth URLs to the random port and create `navigator = Navigator(page, port)`
- The base routes requests at the **Playwright browser-context** level to abort external hosts other than `localhost`
  and `127.0.0.1`, covering additional pages in that context too. This is not a block on HTTP requests made by the
  Spring application or other server-side clients
- Base `@AfterEach` resets feature flags; see [Feature Flags in Integration Tests](#feature-flags-in-integration-tests)

## Seed Data

The seeded bases and their nested variants accept either one classpath script name or an ordered `List<String>`:

```kotlin
class ExampleTests : IntegrationTestWithMutableData("data-mockuser-landlord-with-properties.sql") {
    // The database is reset and this script is applied before each test.
}
```

### Containers and Database Reset

[SharedContainers](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/SharedContainers.kt) supplies singleton
PostgreSQL **and Redis** containers per JVM.
[TestcontainersConfiguration](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/TestcontainersConfiguration.kt)
exposes them through service-connection beans. They outlive individual Spring test contexts; they are not recreated
for every test or context.

[IntegrationTestHelper](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/testHelpers/IntegrationTestHelper.kt)
resets the `public` schema's tables using `TRUNCATE ... RESTART IDENTITY CASCADE`, except for `PRESERVED_TABLES`:
- `flyway_schema_history` keeps Flyway migration history
- `local_council` keeps migration-created reference data

Seed scripts then run in the supplied order. Resets do not drop the schema and reapply migrations.
If a migration adds reference data or renames a preserved table, maintain `PRESERVED_TABLES` and the documented list
in [IntegrationTestReadMe.md](../../docs/IntegrationTestReadMe.md); otherwise resets can erase that reference data.
`IntegrationTestHelperTests` checks preserved tables. See also [database.instructions.md](database.instructions.md).

## Page Objects

### Structure

`BasePage` takes a Playwright `Page`, not a parent `Locator`. For example,
[LettingAgentEmailPagePropertyRegistration](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/pages/propertyRegistrationJourneyPages/LettingAgentEmailPagePropertyRegistration.kt)
uses a page-based form and component factories:

```kotlin
class LettingAgentEmailPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${LettingAgentEmailStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val form = LettingAgentEmailForm(page)
    val submitButton = Button.byText(page, "Save and continue")
    val backLink = BackLink.default(page)

    fun submitEmail(email: String) {
        form.emailInput.fill(email)
        submitButton.clickAndWait()
    }

    class LettingAgentEmailForm(
        page: Page,
    ) : Form(page) {
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
    }
}
```

### Key Principles
- Page objects are **reactive** (use `Locator`, add getters for derived properties)
- Child components are **scoped** to their parent; follow each component's constructor/factory contract
- Components often accept a `Locator`, but `Page` overloads and factories such as `Form(page)` and `Button.byText(page, text)` are valid
- Prefer **properties** for reusable child components, using existing factories to construct them; dynamic lookup methods are also valid
- Prefer **custom classes** over raw `Locator`

### Page Validation and Assertions

Use `BasePage.Companion.createValidPage` or `assertPageIs` after navigation. These helpers wait for load, construct the
page object, check that the URL contains its configured segment (when supplied), and assert no axe violations for
WCAG 2/2.1 A/AA tags (`wcag2a`, `wcag2aa`, `wcag21a`, `wcag21aa`), using the exclusions configured in `BasePage`.
Direct construction alone does not perform those checks; see
[BasePage](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/pages/basePages/BasePage.kt).

For component wrappers, use `BaseComponent.assertThat`; use Playwright's `assertThat` for raw locators:

```kotlin
BaseComponent.assertThat(emailPage.heading).isVisible()
```

## Components

Page objects use reusable components from `integration/pageObjects/components/` (35+ components):

### Atomic Components
`Button`, `Link`, `TextInput`, `TextArea`, `Select`, `Checkboxes`, `Radios`, `FileUpload`

### Composite Components
`Form`, `FormWithSectionHeader`, `Table`, `Tabs`, `Pagination`, `TaskList`, `SearchBar`, `FilterPanel`

### Display Components
`SummaryCard`, `SummaryList`, `NotificationBanner`, `ErrorSummary`, `Warning`, `InsetText`

Components wrap locators via `BaseComponent`, with constructor/factory overloads as described above.
`Button` and `Link` expose `clickAndWait()` through `ClickAndWaitable`; use `form.submit()` for the normal submit
button or `form.submitSelectedButton(value)` for a specific submit action. These helpers click and wait for load;
do not call a nonexistent `Button.click()` API.

## Navigator Patterns

[Navigator](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/Navigator.kt) is supplied by the base test:

```kotlin
// Navigate and return a validated page object
val searchPage = navigator.goToLandlordSearchPage()

// Navigate without returning a page object
navigator.navigateToLandlordRegistrationStartPage()

// Configure journey session state, navigate, and return the page object
val namePage = navigator.skipToLandlordRegistrationNamePage()
```

These naming patterns are conventions, not guarantees that `goTo...` has no setup side effects. For example,
`goToRestructuredPropertyRegistrationTaskList(stateBuilder)` also sets session state before navigation.
It calls `build()` on the supplied `PropertyStateSessionBuilder` and posts the resulting state via the local
`SessionController` endpoint using Navigator's private `setJourneyStateInSession` helper.

Journey-session builders under `testHelpers/builders/` prepare submitted answers and additional state, **not database
seed data**. Choose the DB seed, session state and feature flags independently to make the intended page reachable.
The test example below uses the modern restructured task-list entry point.

## Revisiting Pages

When a test revisits the same page type, use `var` and reassign rather than creating new variables with suffixed names:

```kotlin
// Correct — var + reassignment
var checkJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPage::class)
checkJointLandlordPage.form.addAnotherButton.clickAndWait()
// ... navigate away and back ...
checkJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPage::class)
checkJointLandlordPage.form.submit()

// Wrong — new val each time
val firstCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPage::class)
// ...
val secondCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPage::class)
```

If a navigator method is called with different arguments pointing to genuinely different pages, those remain as separate `val` declarations.

## Test Structure

Accept the injected Playwright `Page` as a test parameter and pass it directly to `assertPageIs`.
This example follows [PropertyRegistrationWhoProvidesChangeSinglePageTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/PropertyRegistrationWhoProvidesChangeSinglePageTests.kt),
using real component APIs and reassigning the revisited check-answers page:

```kotlin
class PropertyRegistrationWhoProvidesExampleTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `entering a letting agent email returns to check answers`(page: Page) {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)

        val taskListPage = navigator.goToRestructuredPropertyRegistrationTaskList(
            PropertyStateSessionBuilder
                .beforePropertyRegistrationCheckAnswersOccupied()
                .withLandlordProvidesRentalDetails()
                .withBedrooms(),
        )
        taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
        var checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        checkAnswersPage.summaryList.whoProvidesRentalDetailsRow.clickFirstActionLinkAndWait()
        assertPageIs(page, WhoProvidesRentalDetailsFormPagePropertyRegistration::class)
            .submitLettingAgentProvidesDetails()
        assertPageIs(page, WhoProvidesChangeInterruptionPagePropertyRegistration::class).submit()

        val emailPage = assertPageIs(page, LettingAgentEmailPagePropertyRegistration::class)
        emailPage.form.emailInput.fill("letting.agent@example.com")
        emailPage.submitButton.clickAndWait()

        checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        assertThat(checkAnswersPage.summaryList.lettingAgentEmailRow.value)
            .containsText("letting.agent@example.com")
    }
}
```

Here `assertPageIs` is imported from `BasePage.Companion`, and `assertThat` from `PlaywrightAssertions` because the
summary-list row's `value` is a raw locator.

## Test Class Naming

Journey test classes commonly follow the pattern: `{JourneyName}{SinglePageTests|JourneyTests}`

- `SinglePageTests` — test individual page rendering and validation
- `JourneyTests` — test end-to-end journey flows

This is not universal: names such as `LandlordDashboardTests` and `SearchRegisterTests` are also established.

## Feature Flags in Integration Tests
- Use the inherited `featureFlagManager` to enable/disable flags needed by a scenario
- See [docs/FeatureFlagsReadMe.md](../../docs/FeatureFlagsReadMe.md) for complex flag updates

Feature flags are automatically reset after each test via `@AfterEach` in the base `IntegrationTest` class, preventing test pollution.

## Quick Reference

| Concern | Pattern |
|---------|---------|
| DB changes versus read-only DB fixtures | Mutable base resets/seeds per test; immutable base per class |
| Journey setup | SQL seeds supply DB rows; state builders and `Navigator` supply journey-session state |
| Page transitions | `form.submit()` / `clickAndWait()`, then `assertPageIs(page, ExpectedPage::class)` |
| Wrapper assertions | `BaseComponent.assertThat(component)` |
| Revisited page type | Reassign a `var`; do not introduce suffixed page variables |
| Commands and runtime prerequisites | [build-ci.instructions.md](build-ci.instructions.md) |
