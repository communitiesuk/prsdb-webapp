---
name: test-driven-development
description: Use when implementing any feature or bugfix. Write the test first, watch it fail, write minimal code to pass.
---

# Test-Driven Development

**Core principle:** If you did not watch the test fail, you do not know if it
tests the right thing.

## When to Use

- New features (always)
- Bug fixes (always — diagnose first with `systematic-debugging`, then write
  regression test before fixing)
- Behaviour changes (always)

Exceptions (confirm with user): throwaway prototypes, generated code, config files.

## Red-Green-Refactor

1. **RED** — Write a failing test
2. **Verify RED** — Run it, confirm it fails for the right reason
3. **GREEN** — Write minimal code to make it pass
4. **Verify GREEN** — Run it, confirm it passes
5. **REFACTOR** — Clean up (tests still passing)
6. **Commit**

## Test Stack Reference

### Unit Tests

```kotlin
@ExtendWith(MockitoExtension::class)
class FooServiceTests {
    @Mock
    private lateinit var mockRepository: FooRepository

    private lateinit var service: FooService

    @BeforeEach
    fun setup() {
        service = FooService(mockRepository)
    }

    @Test
    fun `descriptive name explaining expected behaviour`() {
        // arrange
        whenever(mockRepository.findById(1L)).thenReturn(Optional.of(fooEntity))

        // act
        val result = service.getFoo(1L)

        // assert
        assertThat(result.name).isEqualTo("expected")
    }
}
```

Run:
```powershell
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.services.FooServiceTests" --console=plain
```
```bash
./gradlew test --tests "uk.gov.communities.prsdb.webapp.services.FooServiceTests" --console=plain
```

### Controller Tests

```kotlin
class FooControllerTests : ControllerTest() {
    @MockitoBean
    private lateinit var mockService: FooService

    @Test
    fun `GET foo returns 200 with expected model`() {
        whenever(mockService.getFoo()).thenReturn(fooData)

        mockMvc.get("/landlord/foo")
            .andExpect {
                status { isOk() }
                model { attributeExists("fooData") }
                view { name("foo") }
            }
    }
}
```

Run:
```powershell
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.FooControllerTests" --console=plain
```
```bash
./gradlew test --tests "uk.gov.communities.prsdb.webapp.controllers.FooControllerTests" --console=plain
```

### Integration Tests

```kotlin
@UsePlaywright
class FooJourneyTests : IntegrationTest("db/integrationTestData.sql") {
    @Test
    fun `completing foo journey creates record`(page: Page) {
        navigator.goTo(page, FooPage::class)
        val fooPage = FooPage(page)
        fooPage.fillName("Test")
        fooPage.submit()
        // assertions...
    }
}
```

Run (requires Docker):
```powershell
.\gradlew test --tests "uk.gov.communities.prsdb.webapp.integration.FooJourneyTests" --console=plain
```
```bash
./gradlew test --tests "uk.gov.communities.prsdb.webapp.integration.FooJourneyTests" --console=plain
```

### Journey Step Configuration Tests

```kotlin
class FooStepConfigTests {
    @Test
    fun `step config produces expected page`() {
        val config = FooStepConfig()
        val page = config.page(FooFormModel())
        assertThat(page).isInstanceOf(Page::class.java)
    }
}
```

## Test Naming

Always use backtick-quoted descriptive strings:
```kotlin
@Test
fun `submitting empty form returns validation errors`() { }

@Test
fun `landlord with expired EPC sees warning banner`() { }

@Test
fun `deregistering property sends confirmation email`() { }
```

## Test Location

| Source Under Test | Test Location |
|-------------------|---------------|
| `src/main/.../services/FooService.kt` | `src/test/.../services/FooServiceTests.kt` |
| `src/main/.../controllers/FooController.kt` | `src/test/.../controllers/FooControllerTests.kt` |
| `src/main/.../validation/FooValidator.kt` | `src/test/.../validation/FooValidatorTests.kt` |
| Integration (journey flow) | `src/test/.../integration/FooJourneyTests.kt` |
| Integration (single page) | `src/test/.../integration/pageTests/FooPageTests.kt` |

## When TDD Does Not Apply

- Database migrations (SQL, not testable via unit tests)
- Thymeleaf templates (verified via integration tests, not unit-testable)
- Configuration classes (verified by application startup)
- Pure wiring (Spring bean registration)

For these, write the code first, then verify via integration tests or smoke testing.

## Verification Commands

Quick feedback (unit + controller, no Docker):
```powershell
.\gradlew testWithoutIntegration --console=plain
```
```bash
./gradlew testWithoutIntegration --console=plain
```

Full suite (requires Docker, ~20 min):
```powershell
.\gradlew test --console=plain
```
```bash
./gradlew test --console=plain
```
