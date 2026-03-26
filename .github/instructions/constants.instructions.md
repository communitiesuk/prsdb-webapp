---
applyTo: "**/constants/**,**/enums/**"
---

# Constants Instructions

## Package Structure

```
constants/
├── enums/              # Domain enum classes (PropertyType, TaskStatus, etc.)
├── SessionAttributes.kt
├── UrlParameterNames.kt
├── ExternalLinks.kt
├── FeatureFlagNames.kt
├── FormConstants.kt
├── JourneyNames.kt
├── PaginationConstants.kt
└── ... (other constant files)
```

## Declaration Patterns

### Top-level `const val` (for simple string/int constants)
```kotlin
const val PROPERTY_REGISTRATION_NUMBER = "propertyRegistrationNumber"
const val FILE_UPLOAD_URL_SUBSTRING = "file-upload"
```

### Enum Classes (for type-safe domain values)
```kotlin
enum class PropertyType {
    DETACHED_HOUSE, SEMI_DETACHED_HOUSE, TERRACED_HOUSE, FLAT, OTHER
}

enum class TaskStatus {
    CANNOT_START, NOT_STARTED, IN_PROGRESS, COMPLETED
}
```

### Collections (for validated sets of constants)
```kotlin
val featureFlagNames = listOf(
    EXAMPLE_FEATURE_FLAG_ONE,
    MIGRATE_PROPERTY_REGISTRATION,
    // ...
)
```

## Constant Categories

| File | Contains |
|------|----------|
| `SessionAttributes.kt` | Session attribute key names |
| `UrlParameterNames.kt` | Query/path parameter names |
| `ExternalLinks.kt` | External GOV.UK and service URLs |
| `FeatureFlagNames.kt` | Feature flag name constants and validation list |
| `FormConstants.kt` | Form attribute names |
| `JourneyNames.kt` | Journey identifier strings |
| `PaginationConstants.kt` | Pagination defaults |
| `UserRoleConstants.kt` | User role string constants |
| `UrlSegmentConstants.kt` | URL path segment constants |
| `ExternalEmails.kt` | External email addresses |
| `OneLoginClaimKeys.kt` | One Login JWT claim key names |
| `InvitationLifetimeConstants.kt` | Invitation expiry durations |
| `PropertyComplianceConstants.kt` | Compliance-related constants (validity periods, etc.) |
| `SelectAddressConstants.kt` | Address selection form constants |
| `FeatureFlagReleaseNames.kt` | Feature flag release name constants |

## Enums

Domain enums live in `constants/enums/`. They are used in `when` expressions throughout the codebase and often mapped to i18n message keys via `MessageKeyConverter`.

Common enums include: `PropertyType`, `TaskStatus`, `RentFrequency`, `OwnershipType`, `LicensingType`, `FurnishedStatus`, `BillsIncluded`, `JourneyType`, `FileUploadStatus`, `FileCategory`, `RegistrationNumberType`, `ComplianceCertStatus`, and compliance exemption enums (`GasSafetyExemptionReason`, `EicrExemptionReason`, `EpcExemptionReason`, `MeesExemptionReason`).

When adding a new enum:
1. Create it in `constants/enums/`
2. If it needs display text, add a conversion branch in `MessageKeyConverter`
3. Add corresponding message keys to the appropriate YAML file in `messages/`

## Adding New Constants

- Place constants in the appropriate existing file by category
- Create a new file only if the constant doesn't fit any existing category
- Use `const val` for compile-time string/numeric constants
- Use enum classes for fixed sets of domain values
