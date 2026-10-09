---
applyTo: "**/exceptions/**,**/journeys/NoSuchJourneyException.kt,**/controllers/controllerAdvice/GlobalExceptionHandler.kt,**/controllers/CustomErrorController.kt,**/config/CustomErrorConfig.kt"
---

# Exceptions Instructions

## Base Class

Most custom exceptions extend `PrsdbWebException`, an open class extending `RuntimeException`:

```kotlin
class MyNewException(message: String) : PrsdbWebException(message)
```

`PrsdbWebException` supports multiple constructor patterns (message, cause, or both). Preserve causes when wrapping failures.

Keep existing alternate hierarchies: `InvalidCoreIdentityException`, `InvalidVerifiedCredentialsException` and
`VerifiedCredentialParsingException` extend `Exception`, while
[RateLimitExceededException](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/exceptions/RateLimitExceededException.kt)
extends Apache `org.apache.http.HttpException`, also thrown directly by `OsDownloadsClient`.
Do not require every exception to inherit the runtime base.

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

For email failures, preserve the classification in
[NotifyEmailNotificationService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/NotifyEmailNotificationService.kt):
`TransientEmailSentException` (that is the actual spelling) indicates retry may succeed; `PersistentEmailSendException`
needs correction or changed conditions rather than an immediate retry. `NotifyAllowlistException` extends
`PersistentEmailSendException`. Wrappers retain the Notify cause; this classification does not implement automatic retries.

## Exception Handling

### Global Exception Handler

[GlobalExceptionHandler](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/controllerAdvice/GlobalExceptionHandler.kt)
uses `@PrsdbControllerAdvice`, logs context and returns Spring `redirect:` view names with route constants from
`CustomErrorController`:

```kotlin
@PrsdbControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CyaDataHasChangedException::class)
    fun handleCyaDataHasChangedException(ex: CyaDataHasChangedException): String {
        println("CYA data has changed: ${ex.message}")
        return "redirect:$CYA_ERROR_ROUTE"
    }

    @ExceptionHandler(UpdateConflictException::class)
    fun handleUpdateConflictException(ex: UpdateConflictException): String {
        println("Update conflict occurred: ${ex.message}")
        return "redirect:$UPDATE_CONFLICT_ERROR_ROUTE"
    }

    @ExceptionHandler(NotifyAllowlistException::class)
    fun handleNotifyAllowlistException(ex: NotifyAllowlistException): String {
        println("Email sent to an address not on the Notify allowlist: ${ex.message}")
        return "redirect:$NOTIFY_ALLOWLIST_ERROR_ROUTE"
    }
}
```

Only a small number of exceptions have explicit handlers; others use Spring's default behaviour or caller-specific handling.
`ResponseStatusException` already carries an HTTP status and does not require a global handler.

### Error Page Selection

[CustomErrorController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/CustomErrorController.kt)
reads `RequestDispatcher.ERROR_STATUS_CODE`: 404 selects `error/404`, 403 selects `error/403`, and everything else
selects `error/500`. Its dedicated redirect targets also render the CYA/update-conflict and Notify allowlist pages.
A view name such as `error/500` does not itself set the HTTP response status.

### Custom Error Config

`MalformedGETRequestExceptionResolver` in
[CustomErrorConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/CustomErrorConfig.kt) sends **404** only
for **GET** requests with `HandlerMethodValidationException` or `MethodArgumentTypeMismatchException`.
Otherwise it delegates to `DefaultHandlerExceptionResolver`; this is not a blanket rule for validation errors.

## Where to Put New Exceptions

- Place exception classes in `src/main/kotlin/.../exceptions/`
- Journey-specific exceptions such as [NoSuchJourneyException](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/NoSuchJourneyException.kt) may live in `journeys/`
- Add advice when a custom business redirect/page is needed, not merely because an exception represents an HTTP error.
  Use the existing Spring status-handling contract where appropriate.
