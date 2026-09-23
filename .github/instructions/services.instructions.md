---
applyTo: "**/services/**"
---

# Service Instructions

## Naming Conventions
- Services: `{Feature}Service.kt` (e.g., `LandlordService.kt`)
- Test classes: `{Feature}ServiceTests.kt`

## Annotations
Use `@PrsdbWebService` by default for web-only services; choose annotations by lifecycle, not just package.

```kotlin
@PrsdbWebService  // Web-server-only service
class ExampleService(
    private val exampleRepository: ExampleRepository,
    private val otherService: OtherService
) {
    // Business logic here
}
```

`@PrsdbTaskService` is for task-only services (not web server mode). Plain `@Service` is intentional for shared
services used in both modes, such as
[NotifyEmailNotificationService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/NotifyEmailNotificationService.kt) and
[AbsoluteUrlProvider](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/AbsoluteUrlProvider.kt).
Do not blindly replace shared services' annotations with web-only wrappers.

## Repository Interactions
- Inject repositories via constructor
- Use repository methods for data access
- Keep repository calls in services, not controllers

## Transaction Handling
The normal annotation is `jakarta.transaction.Transactional`, which has no `readOnly` argument.
Put transaction boundaries around mutations; read methods often have no service-level transaction annotation.

```kotlin
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional

@Transactional
fun updateEntity(id: Long, data: UpdateData) {
    val entity = repository.findById(id).orElseThrow { EntityNotFoundException("Entity $id not found") }
    entity.apply { /* update fields */ }
    // Managed entity changes are persisted at commit.
}

fun findEntity(id: Long): Entity? {
    return repository.findById(id).orElse(null)
}
```

Managed entities changed within a transaction can rely on dirty checking without an explicit `save`, as in
[LandlordService.updateOrganisationLandlordForUser](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/LandlordService.kt).
This does not replace persistence calls needed for new or detached entities.

- Preserve the timestamp stale-update guard in property update flows:
  [PropertyOwnershipService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/PropertyOwnershipService.kt)
  compares the session's original timestamp with `getMostRecentlyUpdated()` before mutation and throws
  `UpdateConflictException` on a mismatch. This is application-level conflict detection, not a database lock.
- Use [TransactionHelper.runAfterTransactionCommits](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/helpers/TransactionHelper.kt)
  for side effects that should follow a successful commit. It registers an `afterCommit` callback when transaction
  synchronization is active; otherwise it runs the action immediately.

## Error Handling
- Choose nullable or throwing retrieval deliberately: `LandlordService.retrieveLandlordById` returns `Landlord?`,
  while `PropertyOwnershipService.getPropertyOwnership` throws a meaningful `ResponseStatusException(NOT_FOUND, ...)`.
- Web-facing services may intentionally throw `ResponseStatusException`; translation is not controller-only.
  [EpcLookupService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/EpcLookupService.kt) also maps invalid EPC requests to HTTP 400.
- Use domain-specific exceptions where appropriate, and meaningful messages with `orElseThrow()`.
  See [exception handling instructions](exceptions.instructions.md) for status, redirect and page handling.

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
For small branches, use the active
[FeatureFlagManager.checkFeature](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/managers/FeatureFlagManager.kt)
API with named constants from
[FeatureFlagNames.kt](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/constants/FeatureFlagNames.kt), rather than requiring two implementations:

```kotlin
if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
    // Flag-enabled behaviour
} else {
    // Existing behaviour
}
```

[@PrsdbFlip](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/annotations/webAnnotations/PrsdbFlip.kt)
remains supported and documented, although there are currently no main application call sites.
For that alternative pattern, annotate the interface or its methods with
`@PrsdbFlip(name = FLAG_CONSTANT, alterBean = "newImpl")`, provide a default implementation with `@PrsdbWebService`
and `@Primary` plus a named alternate such as `@PrsdbWebService("newImpl")`, and inject the interface into consumers.
See the [feature flag guide](../../docs/FeatureFlagsReadMe.md) and [feature flag instructions](feature-flags.instructions.md).
