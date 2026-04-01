---
applyTo: "**/models/**"
---

# Models Instructions

The `models/` package has three categories, each serving a different layer of the application.

## DataModels (`models/dataModels/`)

Represent core business data, often serialised to/from the database or external APIs.

```kotlin
@Serializable
data class AddressDataModel(
    val singleLineAddress: String,
    val uprn: Long? = null,
    val postcode: String? = null,
) {
    companion object {
        fun fromAddress(address: Address) = AddressDataModel(...)
    }
}
```

**Conventions:**
- Use `data class` (immutable)
- Add `@Serializable` when stored as JSON (e.g. in journey data)
- Include companion factory methods (`fromEntity`, `fromAddress`) for conversion from entities
- Can contain domain logic methods (e.g. `isPastExpiryDate()`)
- `updateModels/` subdirectory holds change-tracking models for update journeys. These use plain `data class` (NOT `@Serializable`) as they represent deltas, not persisted data. Example: `PropertyOwnershipUpdateModel`, `PropertyComplianceUpdateModel`, `LandlordUpdateModel`

## RequestModels (`models/requestModels/`)

Bind user form submissions. Implement the `FormModel` interface.

```kotlin
class RentAmountFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.rentAmount.error",
                validatorType = PositiveBigDecimalValidator::class,
            ),
        ],
    )
    var rentAmount: String = ""
}
```

**Conventions:**
- Implement `FormModel` interface (provides `toPageData()`)
- Use `var` fields (mutable for form binding)
- Validation via `@ValidatedBy` with `ConstraintDescriptor` entries
- `formModels/` subdirectory for web form models
- `searchModels/` subdirectory for search/filter models extending `SearchRequestModel`

## ViewModels (`models/viewModels/`)

Transform data for Thymeleaf template rendering. Display-only.

```kotlin
data class SummaryListRowViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val action: SummaryListRowActionViewModel? = null,
)
```

**Conventions:**
- Use immutable `data class`
- No validation — these are purely for display
- Subdirectories: `formModels/` (UI components like radios, selects, checkboxes), `summaryModels/` (including `propertyComplianceViewModels/`), `emailModels/`, `taskModels/`, `filterPanelModels/`, `searchResultModels/`
- ViewModels are passed to templates and accessed via `${model.property}`
- Use `RadiosViewModel` / `SelectViewModel<T>` for form UI components

## Summary

| Aspect | DataModels | RequestModels | ViewModels |
|--------|-----------|--------------|-----------|
| **Layer** | Database / Domain | User Input | Display |
| **Mutability** | Immutable (`val`) | Mutable (`var`) | Immutable (`val`) |
| **Validation** | Business logic methods | `@ValidatedBy` annotations | None |
| **Base** | None (data classes) | `FormModel` interface | None |
| **Serialisation** | `@Serializable` | N/A | N/A |
