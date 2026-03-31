---
applyTo: "**/helpers/**,**/extensions/**,**/converters/**"
---

# Helpers Instructions

## Package Structure

```
helpers/
├── converters/        # MessageKeyConverter for domain → i18n key mapping
├── extensions/        # Kotlin extension functions
│   ├── journeyExtensions/   # JourneyExtensions, JourneyDataExtensions, PropertyComplianceJourneyDataExtensions
│   ├── savedJourneyStateExtensions/
│   ├── FileItemInputIteratorExtensions.kt
│   ├── MessageSourceExtensions.kt
│   ├── PreparedStatementExtensions.kt
│   ├── PropertyComplianceViewModelExtensions.kt
│   ├── SummaryCardViewModelExtensions.kt
│   ├── SummaryListViewModelExtensions.kt
│   └── ZipInputStreamExtensions.kt
├── AddressHelper.kt, BillsIncludedHelper.kt, CompleteByDateHelper.kt, ...
├── DateTimeHelper.kt, JourneyDataHelper.kt, RentDataHelper.kt, ...
├── LocalDateSerializer.kt                     # KotlinX serializer
├── MaximumLengthInputStream.kt                 # Bounded input stream
├── PropertyComplianceJourneyHelper.kt          # Compliance journey utilities
├── PropertyRegistrationJourneyDataHelper.kt    # Registration journey utilities
└── URIQueryBuilder.kt                          # Fluent URI builder
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

### Receiver Extensions (most common pattern)
```kotlin
fun MessageSource.getMessageForKey(key: String, args: Array<Any>? = null) =
    getMessage(key, args, Locale.getDefault())
```

### Collection Extensions (DSL-style builders)
```kotlin
fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    actionText: String? = null,
) { ... }
```

### Null-Safe DB Operations
```kotlin
fun PreparedStatement.setStringOrNull(parameterIndex: Int, value: String?) {
    if (value == null) setNull(parameterIndex, Types.VARCHAR)
    else setString(parameterIndex, value)
}
```

The `journeyExtensions/` subdirectory contains 3 files:
- `JourneyExtensions.kt` — Map extension for back URL handling
- `JourneyDataExtensions.kt` — address/journey data parsing
- `PropertyComplianceJourneyDataExtensions.kt` — 40+ getter functions for compliance journey data (gas safety, EICR, EPC fields)

Extension files are in `helpers/extensions/`. Place new extensions in the appropriate file, or create a new `{Type}Extensions.kt` file.

## URI Query Builder

Fluent builder for manipulating request query parameters:

```kotlin
URIQueryBuilder.fromHTTPServletRequest(request)
    .updateParam("page", 2)
    .removeParam("filter")
    .build()
```

## Kotlin Idioms Used

| Idiom | Example |
|-------|---------|
| Reified type parameters | `inline fun <reified E : Enum<E>> getFieldEnumValue()` |
| Scope functions | `selectedAddress?.let { LocalDate.parse(it) }` |
| Default parameters | `fun helper(args: Array<Any>? = null)` |
| Companion objects | Static-like factory methods |
| Elvis operator | `pageData ?: return null` |
