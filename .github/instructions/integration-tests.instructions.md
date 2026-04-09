---
applyTo: "**/integration/**"
---

# Integration Test Instructions

## Documentation Reference
- Full guide: [docs/IntegrationTestReadMe.md](../../docs/IntegrationTestReadMe.md)

## Test Base Classes

### Choose the Right Base Class
- `IntegrationTestWithMutableData`: Resets DB before **each test** (tests modify data)
- `IntegrationTestWithImmutableData`: Resets DB before **each class** (read-only tests)
- `IntegrationTest`: No DB reset (use nested classes for different seed data)

All integration test classes are annotated with `@UsePlaywright` (from Playwright) and `@ActiveProfiles("local", "local-no-auth")`.

### Nested Classes
- `NestedIntegrationTestWithMutableData`: For tests needing different seed data
- `NestedIntegrationTestWithImmutableData`: Same, for read-only tests

## Seed Data
```kotlin
class ExampleTests : IntegrationTestWithMutableData("seed-data-script.sql") {
    // Tests here use seed-data-script.sql
}
```

## Page Objects

### Structure
```kotlin
class ExamplePage(parentLocator: Locator) : BasePage(parentLocator) {
    
    val heading: Locator
        get() = parentLocator.locator("h1")
    
    val submitButton = Button(parentLocator.locator("[data-testid='submit']"))
    
    val form = ExampleForm(parentLocator.locator("form"))
}
```

### Key Principles
- Page objects are **reactive** (use `Locator`, add getters for derived properties)
- Page objects are **scoped** (take `parentLocator` in constructor)
- Prefer **properties** over factory methods
- Prefer **custom classes** over raw `Locator`

## Components

Page objects use reusable components from `integration/pageObjects/components/` (35+ components):

### Atomic Components
`Button`, `Link`, `TextInput`, `TextArea`, `Select`, `Checkboxes`, `Radios`, `FileUpload`

### Composite Components
`Form`, `FormWithSectionHeader`, `Table`, `Tabs`, `Pagination`, `TaskList`, `SearchBar`, `FilterPanel`

### Display Components
`SummaryCard`, `SummaryList`, `NotificationBanner`, `ErrorSummary`, `Warning`, `InsetText`

All components extend `BaseComponent` and take a `Locator` in their constructor.

## Navigator Patterns
```kotlin
// Navigate and return page object
val page = navigator.goToExamplePage()

// Navigate with setup (configures session first)
val page = navigator.skipToCheckAnswers()

// Just navigate (no return)
navigator.navigateToHomePage()
```

## Test Structure
```kotlin
@Test
fun `user can submit form successfully`() {
    val page = navigator.goToExamplePage()
    
    page.form.fillName("Test Name")
    page.submitButton.click()
    
    val resultPage = ResultPage(page.playwright.page)
    assertThat(resultPage.confirmationBanner.isVisible).isTrue()
}
```

## Test Class Naming

Test classes follow the pattern: `{JourneyName}{SinglePageTests|JourneyTests}`

- `SinglePageTests` — test individual page rendering and validation
- `JourneyTests` — test end-to-end journey flows

## Feature Flags in Integration Tests
- Flags reset automatically after each test
- Use `FeatureFlagManager` to enable/disable flags
- See `docs/FeatureFlagsReadMe.md` for complex flag updates

Feature flags are automatically reset after each test via `@AfterEach` in the base `IntegrationTest` class, preventing test pollution.
