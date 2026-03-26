---
applyTo: "**/exceptions/**"
---

# Exceptions Instructions

## Base Class

Most custom exceptions extend `PrsdbWebException`, an open class extending `RuntimeException`:

```kotlin
class MyNewException(message: String) : PrsdbWebException(message)
```

`PrsdbWebException` supports multiple constructor patterns (message, cause, or both).

## Naming Convention

Use PascalCase with a descriptive business context and the `Exception` suffix:

| Pattern | Examples |
|---------|----------|
| Domain + Exception | `UpdateConflictException`, `PropertyOwnershipMismatchException` |
| State/Business Logic | `CyaDataHasChangedException`, `JourneyInitialisationException` |
| Validation Errors | `InvalidCoreIdentityException`, `InvalidVerifiedCredentialsException` |
| Limit Exceeded | `RateLimitExceededException`, `PasscodeLimitExceededException` |
| External Service Errors | `PersistentEmailSendException`, `TransientEmailSentException` |
| Invitation Errors | `InvalidInvitationException` |
| Null Safety | `NotNullFormModelValueIsNullException` |
| Token/Auth | `TokenNotFoundException`, `VerifiedCredentialParsingException` |
| Database Errors | `RepositoryQueryTimeoutException` |

## Exception Handling

### Global Exception Handler

`GlobalExceptionHandler` (in `controllers/controllerAdvice/GlobalExceptionHandler.kt`, annotated with `@PrsdbControllerAdvice`) maps exceptions to redirect responses:

```kotlin
@PrsdbControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CyaDataHasChangedException::class)
    fun handleCyaDataChanged() = redirectTo(CYA_ERROR_ROUTE)

    @ExceptionHandler(UpdateConflictException::class)
    fun handleUpdateConflict() = redirectTo(UPDATE_CONFLICT_ERROR_ROUTE)
}
```

Only a small number of exceptions have explicit handlers — others use Spring's default behaviour or are caught at the service layer.

### Custom Error Config

`MalformedGETRequestExceptionResolver` in `config/CustomErrorConfig.kt` maps GET request validation errors (e.g. `MethodArgumentTypeMismatchException`) to **404** instead of 400.

## Where to Put New Exceptions

- Place exception classes in `src/main/kotlin/.../exceptions/`
- Journey-specific exceptions (e.g. `NoSuchJourneyException`) may live in the `journeys/` package
- If the exception should map to a specific HTTP response or redirect, add a handler method to `GlobalExceptionHandler`
