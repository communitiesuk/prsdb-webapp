---
applyTo: "src/test/**"
---

# Unit Test Instructions

## Testing Framework Stack
- **JUnit 5** (Jupiter) for test lifecycle
- **Mockito-Kotlin** for mocking (`org.mockito.kotlin.*`)
- **Spring Boot Test** for controller tests (`@WebMvcTest`)
- **Spring Security Test** for auth (`@WithMockUser`)

## Controller Tests

Extend the `ControllerTest` base class which sets up MockMvc with Spring Security:

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
- Use `@WithMockUser(roles = [...])` to test role-based access
- Use the `mvc` field from `ControllerTest` for request assertions

## Service Tests

Use `@ExtendWith(MockitoExtension::class)` with `@Mock` and `@InjectMocks`:

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
- Use `@Mock` for dependencies and `@InjectMocks` for the service under test
- Use `whenever(...).thenReturn(...)` from mockito-kotlin
- Use `verify(mock).method()` to assert interactions

## Test Method Naming

Use backtick-enclosed descriptive names:

```kotlin
fun `myEndpoint returns 200 for authorised user`()
fun `retrieveItem throws exception when item not found`()
fun `createLicense creates a license with correct parameters`()
```

**Pattern:** `` `[subject] [action/result] [conditions]` ``

## Test Data Creation

### Factory Companion Methods (for simple objects)
```kotlin
class MockLandlordData {
    companion object {
        fun createLandlord(
            name: String = "name",
            email: String = "example@email.com",
        ): Landlord { ... }
    }
}
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

Use `@ParameterizedTest` with `@MethodSource` for multiple scenarios:

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
