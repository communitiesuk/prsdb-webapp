---
applyTo: "src/test/kotlin/**/*.kt"
---

# Unit Test Instructions

These shared Kotlin test conventions apply across the test tree, but not every Kotlin test is an isolated unit test.
Use the setup appropriate to the behaviour under test; do not add Spring or browser infrastructure to isolated logic tests.

## Testing Framework Stack
- **JUnit 5** (Jupiter) for test lifecycle
- **JUnit assertions or `kotlin.test`** for assertions; follow the surrounding tests
- **Mockito-Kotlin** for mocking (`org.mockito.kotlin.*`)
- **Spring Boot Test** for MVC slices (`@WebMvcTest`) and application-context tests (`@SpringBootTest`)
- **Spring Security Test** for auth (`@WithMockUser`) and CSRF (`csrf()`)

## Quick Reference: Choose the Test Layer

| Layer | Typical setup |
|-------|---------------|
| Isolated unit logic | Plain JUnit and Mockito for services, helpers, journey routing, factories, tasks, builders and state |
| MVC slice | `@WebMvcTest` and `ControllerTest`, directly or through a shared intermediate base |
| Form validation | Real Jakarta validator; `SpringValidatorAdapter` when exercising Spring form binding |
| Application context/component | Spring context and any imported fixtures; not necessarily isolated or container-free |
| Browser integration | Playwright and `IntegrationTest`; see [integration-tests.instructions.md](integration-tests.instructions.md) |
| Live template contract | Optional Notify service comparisons; see [Live Notify Template Contracts](#live-notify-template-contracts) |

Directory names are not a test-type guarantee:
[JourneyFrameworkComponentTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/JourneyFrameworkComponentTests.kt)
lives under `journeys/` but extends `IntegrationTest`, while
[PrsdbWebappApplicationTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/applications/PrsdbWebappApplicationTests.kt)
loads an application context and imports `TestcontainersConfiguration`.
For task selection, JVM versus Node tests, sharding and Docker requirements, see
[build-ci.instructions.md](build-ci.instructions.md), rather than inferring requirements from a directory or task name.

## Controller Tests

Extend the `ControllerTest` base class directly or through an appropriate shared base. It sets up `mvc` before each test
with Spring Security and the trailing-slash filter, imports common security/error/back-link configuration, and supplies
common `@MockitoBean` dependencies. Reuse this setup rather than rebuilding it in each test class.

```kotlin
@WebMvcTest(MyController::class)
class MyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {

    @MockitoBean
    private lateinit var myService: MyService

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `myEndpoint returns 200 for authorised landlord user`() {
        whenever(myService.getData()).thenReturn(data)

        mvc.get("/landlord/my-endpoint").andExpect {
            status { isOk() }
        }
    }
}
```

**Key points:**
- Use `@WebMvcTest(ControllerClass::class)` to load only the controller slice
- Use `@MockitoBean` for Spring-managed dependencies
- Use `@WithMockUser(roles = [...])` for protected routes and role-based access tests, alongside unauthenticated and wrong-role cases
- Do not add authentication to public invitation tests: [LettingAgentInvitationControllerTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/controllers/LettingAgentInvitationControllerTests.kt) deliberately tests access without a user
- Use the `mvc` field from `ControllerTest` for request assertions
- Indirect inheritance is valid: [UpdateOccupancyControllerTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/controllers/UpdateOccupancyControllerTests.kt) inherits `ControllerTest` through `BasePropertyDetailsUpdateControllerTests`

Include `with(csrf())` on POST requests unless testing missing/invalid CSRF protection. Import `csrf` from
`org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors`.
For example, the shared [property-details update tests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/controllers/BasePropertyDetailsUpdateControllerTests.kt) use:

```kotlin
mvc.post(updateStepRoute) {
    contentType = MediaType.APPLICATION_FORM_URLENCODED
    content = formContent
    with(csrf())
}.andExpect {
    status { is3xxRedirection() }
}
```

## Service Tests

Use `@ExtendWith(MockitoExtension::class)` with `@Mock` and `@InjectMocks`, or explicitly construct the service with
mocked dependencies. Both patterns are valid:

```kotlin
@ExtendWith(MockitoExtension::class)
class MyServiceTests {

    @Mock
    private lateinit var mockRepository: MyRepository

    @InjectMocks
    private lateinit var myService: MyService

    @Test
    fun `retrieveItem returns item when it exists`() {
        whenever(mockRepository.findById(anyString())).thenReturn(item)

        val result = myService.retrieveItem(id)

        assertEquals(expected, result)
        verify(mockRepository).findById(id)
    }
}
```

**Key points:**
- Use `@Mock` with `@InjectMocks`, or `mock<Dependency>()` and an explicit constructor call
- Plain JUnit tests using `mock()` do not need `MockitoExtension` just to create those mocks
- Use `whenever(...).thenReturn(...)` from mockito-kotlin
- Use `verify(mock).method()` to assert interactions

## Modern Journey Unit Tests

### Step Configuration and Routing

Construct step configs directly and mock the relevant `JourneyState` interface. When the API being exercised uses
form binding, initialise both `urlPath` and `validator` first, including for `mode` implementations that read a form
model from state:

```kotlin
val state = mock<JourneyState>()
val routeSegment = ConfirmChangeToLettingAgentStep.ROUTE_SEGMENT
val stepConfig = ConfirmChangeToLettingAgentStepConfig().apply {
    urlPath = routeSegment
    validator = AlwaysTrueValidator()
}
whenever(state.getStepData(routeSegment)).thenReturn(emptyMap())

assertEquals(Complete.COMPLETE, stepConfig.mode(state))
```

See [ConfirmChangeToLettingAgentStepConfigTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/steps/ConfirmChangeToLettingAgentStepConfigTests.kt)
and [HasElectricalCertStepConfigTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/steps/HasElectricalCertStepConfigTests.kt).
Their private `setupStepConfig` functions are test-local helpers, not a shared framework API.

Reuse the existing [AlwaysTrueValidator and AlwaysFalseValidator](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/testHelpers/mockObjects/AlwaysTrueValidator.kt)
to force validation outcomes for routing/completion tests. They do not test real constraints; use a real validator when
binding or validation behaviour is the subject of the test.

Pure routing and factory tests can use plain JUnit and Mockito without Spring or form-binding setup when they do not
exercise that API. Follow
[WhoProvidesUpdateRoutingStepConfigTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/steps/WhoProvidesUpdateRoutingStepConfigTests.kt)
and [PropertyRegistrationJourneyFactoryTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/PropertyRegistrationJourneyFactoryTests.kt).

### Tasks, Builders and State

- For task routing, compose real steps/configs with mocked service/state inputs, bind the task dependencies, and initialise
  the task with `TaskInitialiser` before asserting destinations, reachability or completion. See
  [WhoProvidesDetailsTaskTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/tasks/WhoProvidesDetailsTaskTests.kt);
  its private `buildTask` helper is local to that test class.
- Keep builder configuration tests isolated: [JourneyBuilderTest](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/builders/JourneyBuilderTest.kt)
  exercises initialisation and parentage with builders and mocks, without an application context.
- Test state delegation with a mocked `JourneyStateService`, as in
  [AbstractJourneyStateTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/AbstractJourneyStateTests.kt).
  [JourneyStateServiceTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/JourneyStateServiceTests.kt)
  also exercises session behaviour using mocked dependencies and `MockHttpSession`, without a live server.
- Browser journey-session setup via state builders and `Navigator` is a separate integration concern; see
  [Navigator Patterns](integration-tests.instructions.md#navigator-patterns).

## Form Validation Tests

Exercise form constraints with a real Jakarta validator and assert the message keys, not translated page copy.
[WhoProvidesRentalDetailsFormModelTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/models/requestModels/formModels/WhoProvidesRentalDetailsFormModelTests.kt)
covers missing and valid enum values:

```kotlin
Validation.buildDefaultValidatorFactory().use { factory ->
    val violations = factory.validator.validate(WhoProvidesRentalDetailsFormModel())

    assertEquals(1, violations.size)
    assertEquals(
        "registerProperty.whoProvidesRentalDetails.radios.error.missing",
        violations.single().message,
    )
}
```

For step-config form-binding tests, set `urlPath` and use `SpringValidatorAdapter(factory.validator)` as the config's
Spring validator. [StepConfigTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/StepConfigTests.kt)
demonstrates binding stored values into a form model with the real validator.

## Test Method Naming

Prefer backtick-enclosed descriptive names:

```kotlin
fun `myEndpoint returns 200 for authorised user`()
fun `retrieveItem throws exception when item not found`()
fun `createLicense creates a license with correct parameters`()
```

**Pattern:** `` `[subject] [action/result] [conditions]` ``

Established conventional names such as `contextLoads` are also valid; do not rename them solely to enforce this pattern.

## Test Data Creation

### Factory Companion Methods (for simple objects)

Use the existing [MockLandlordData](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/testHelpers/mockObjects/MockLandlordData.kt)
factories rather than inventing a `createLandlord` API:

```kotlin
val landlord = MockLandlordData.createIndividualLandlord(name = "Test Landlord")
val organisation = MockLandlordData.createOrgLandlord(name = "Test Organisation")
```

### Builder Pattern (for complex objects with optional properties)
```kotlin
PropertyComplianceBuilder()
    .withGasSafetyCert(issueDate = LocalDate.now())
    .build()
```

Existing mock data helpers are in `src/test/kotlin/**/testHelpers/`. Check for existing factories before creating new ones.

### Reflection for Private Fields
```kotlin
ReflectionTestUtils.setField(entity, "createdDate", createdDate)
```

## Parameterised Tests

Use `@ParameterizedTest` with a source appropriate to the cases. `@EnumSource`, `@NullSource`, `@ValueSource` and other
JUnit sources are valid alternatives; nullable cases need nullable parameters. Use `@MethodSource` for richer or
multi-argument scenarios:

```kotlin
@ParameterizedTest
@MethodSource("provideTestCases")
fun `myMethod handles various inputs`(input: String, expected: String) {
    assertEquals(expected, myService.myMethod(input))
}

companion object {
    @JvmStatic
    fun provideTestCases() = listOf(
        Arguments.of("input1", "expected1"),
        Arguments.of("input2", "expected2"),
    )
}
```

## Live Notify Template Contracts

[NotifyEmailTemplateTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/notify/NotifyEmailTemplateTests.kt)
is an optional live contract suite, not an isolated unit test. It compares source-controlled templates with the
integration and production Notify services using `EMAILNOTIFICATIONS_APIKEY` and
`EMAILNOTIFICATIONS_PRODUCTION_APIKEY`, respectively. Live comparisons skip an environment without its key, and the
whole class is disabled when neither key is configured. Never commit keys or configuration containing them.
See [emails.instructions.md](emails.instructions.md) for template and environment guidance.
