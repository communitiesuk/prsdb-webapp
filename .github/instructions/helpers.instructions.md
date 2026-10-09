---
applyTo: "**/helpers/**,**/extensions/**,**/converters/**"
---

# Helpers Instructions

## Package Structure

Common locations (not an exhaustive inventory):
```
helpers/
├── converters/        # MessageKeyConverter for domain → i18n key mapping
├── extensions/        # Kotlin extension functions
│   ├── savedJourneyStateExtensions/SavedJourneyStateExtensions.kt
│   ├── FileItemInputIteratorExtensions.kt
│   ├── MessageSourceExtensions.kt
│   ├── PreparedStatementExtensions.kt
│   ├── StringExtensions.kt
│   ├── SummaryCardViewModelExtensions.kt
│   ├── SummaryListViewModelExtensions.kt
│   └── ZipInputStreamExtensions.kt
├── AddressHelper.kt, BillsIncludedHelper.kt, CompleteByDateHelper.kt, ...
├── DateTimeHelper.kt, RentDataHelper.kt, ...
├── CertificateFilenameHelper.kt               # Certificate upload keys
├── CertificateUploadHelper.kt                 # Upload processing → FormData
├── LocalDateSerializer.kt                     # KotlinX serializer for java.time.LocalDate
├── MaximumLengthInputStream.kt                # Bounded input stream
├── MetricsDurationHelper.kt                   # Localised metrics durations
├── TransactionHelper.kt                       # Post-commit callbacks
└── URIQueryBuilder.kt                         # Fluent URI builder

journeys/shared/helpers/
├── ComplianceDetailsHelper.kt                 # Typed-state compliance CYA content
└── OccupancyDetailsHelper.kt                  # Typed-state occupancy CYA rows
```

## Converters

`MessageKeyConverter` maps domain objects to i18n message keys using exhaustive `when` expressions:

```kotlin
class MessageKeyConverter {
    companion object {
        fun convert(boolean: Boolean): String = when (boolean) {
            true -> "commonText.yes"
            false -> "commonText.no"
        }

        fun convert(enum: Enum<*>): String = when (enum) {
            is PropertyType -> convertPropertyType(enum)
            is OwnershipType -> convertOwnershipType(enum)
            else -> throw NotImplementedError(...)
        }
    }
}
```

When adding a new enum that needs display text, add a branch to the converter.

## Extension Functions

Both top-level functions and companion-scoped extensions are used. Follow the surrounding file's pattern and import companion extension functions explicitly (e.g. `MessageSourceExtensions.Companion.getMessageForKey`).

- `StringExtensions` normalises currency, integer and email strings and supplies normalised email comparison/collection helpers
- `SavedJourneyStateExtensions` remains responsible for extracting the property-registration address from a serialised saved state

Extension files are in `helpers/extensions/`. Place new extensions in the appropriate file, or create a new `{Type}Extensions.kt` file.

### Receiver Extensions
```kotlin
class MessageSourceExtensions {
    companion object {
        fun MessageSource.getMessageForKey(
            key: String,
            args: Array<Any>? = null,
        ) = getMessage(key, args, Locale.getDefault())
    }
}
```

### Collection Extensions (DSL-style builders)
```kotlin
val rows = mutableListOf<SummaryListRowViewModel>()
rows.addRow(
    key = "forms.checkPropertyAnswers.tenancyDetails.occupied",
    value = isOccupied,
    actionText = "forms.links.change",
    actionLink = changeDestination.toUrlStringOrNull(),
)
```

`addRow` adds an action only when both `actionText` and `actionLink` are non-null and `withActionLink` is true (the default).

### Null-Safe DB Operations
```kotlin
fun PreparedStatement.setStringOrNull(parameterIndex: Int, value: String?) {
    if (value == null) setNull(parameterIndex, Types.VARCHAR)
    else setString(parameterIndex, value)
}
```

## Journey and Upload Helpers

- Prefer typed journey state and `step.formModel` / `formModelOrNull` for answers; `FormData` is the submitted-data map (`Map<String, Any?>`), not a replacement for typed state
- Reuse `journeys/shared/helpers/ComplianceDetailsHelper` and `OccupancyDetailsHelper` for CYA content/rows and child-journey `Destination` links
- `CertificateFilenameHelper` constructs certificate upload keys; `CertificateUploadHelper` validates upload tokens/metadata, handles streaming uploads and returns `FormData`. Import `MaximumLengthInputStream.Companion.withMaxLength` for bounded streams

## URI Query Builder

Fluent builder for manipulating request query parameters. `build()` returns `UriComponents`; call `toUriString()` when a string URL is needed:

```kotlin
URIQueryBuilder.fromHTTPServletRequest(request)
    .updateParam("page", 2)
    .removeParam("filter")
    .build()
    .toUriString()
```

## Transaction Callbacks

`TransactionHelper.runAfterTransactionCommits { ... }` registers an `afterCommit` callback when Spring transaction synchronisation is active; otherwise it runs the action immediately. It does not start a transaction. Use it for post-commit side effects within appropriately transactional flows.

## Kotlin Idioms Used

| Idiom | Example |
|-------|---------|
| Type aliases | `FormData` = `Map<String, Any?>` |
| Scope functions | `dateString?.let { LocalDate.parse(it) }` |
| Default parameters | `fun helper(args: Array<Any>? = null)` |
| Companion objects | Static-like factory methods |
| Elvis operator | `formData ?: return null` |
