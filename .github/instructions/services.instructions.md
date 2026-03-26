---
applyTo: "**/services/**"
---

# Service Instructions

## Naming Conventions
- Services: `{Feature}Service.kt` (e.g., `LandlordService.kt`)
- Test classes: `{Feature}ServiceTests.kt`

## Annotations
```kotlin
@PrsdbWebService  // Custom service annotation
class ExampleService(
    private val exampleRepository: ExampleRepository,
    private val otherService: OtherService
) {
    // Business logic here
}
```

`@PrsdbTaskService` is used for services that only run during scheduled/one-time tasks (not web server mode).

## Repository Interactions
- Inject repositories via constructor
- Use repository methods for data access
- Keep repository calls in services, not controllers

## Transaction Handling
```kotlin
@Transactional
fun updateEntity(id: Long, data: UpdateData) {
    val entity = repository.findById(id).orElseThrow()
    entity.apply { /* update fields */ }
    repository.save(entity)
}

@Transactional(readOnly = true)
fun findEntity(id: Long): Entity? {
    return repository.findById(id).orElse(null)
}
```

## Error Handling
- Throw domain-specific exceptions (e.g., `EntityNotFoundException`)
- Let controllers handle exception translation to HTTP responses
- Use `orElseThrow()` with meaningful exception messages

## Service Tests
```kotlin
@ExtendWith(MockitoExtension::class)
class ExampleServiceTests {
    @Mock
    private lateinit var repository: ExampleRepository
    
    @InjectMocks
    private lateinit var service: ExampleService
    
    @Test
    fun `finds entity by id`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(testEntity))
        
        val result = service.findEntity(1L)
        
        assertThat(result).isEqualTo(testEntity)
        verify(repository).findById(1L)
    }
}
```

## Feature-Flagged Services
- Define interface with `@PrsdbFlip` annotation
- Create two implementations with `@PrsdbWebService("bean-name")`
- Mark default implementation with `@Primary`
- See `docs/FeatureFlagsReadMe.md` for details
