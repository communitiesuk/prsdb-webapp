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
- Prefer immutable `data class` DTOs; follow existing mutability where the model requires it
- Match serialisation to the boundary: KotlinX `@Serializable` for journey-state delegate values, Jackson for API payloads (e.g. `PlausibleQuery`), and Java `Serializable` where session storage requires it. `VerifiedIdentityDataModel` uses both KotlinX and Java serialisation; JSON alone does not imply KotlinX
- Include companion factory methods (`fromEntity`, `fromAddress`) for conversion from entities
- Can contain domain logic methods (e.g. `isPastExpiryDate()`)
- `updateModels/` holds `IndividualLandlordUpdateModel` and `OrganisationLandlordUpdateModel`. These are plain data classes without `@Serializable`, representing update deltas rather than persisted journey snapshots

## RequestModels (`models/requestModels/`)

Bind HTTP input, including journey forms and search/filter requests. Only journey form models need the `FormModel` interface.

```kotlin
@IsValidPrioritised
class RentAmountFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.rentAmount.error",
                validatorType = PositiveBigDecimalValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.rentAmount.error",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotMoreThanTwoDecimalPlaces",
            ),
        ],
    )
    var rentAmount: String = ""
        set(value) {
            field = value.toNormalizedCurrencyString()
        }

    fun isNotMoreThanTwoDecimalPlaces() = rentAmount.toBigDecimal().scale() <= 2
}
```

**Conventions:**
- Journey forms implement `FormModel`, whose `toPageData()` returns `FormData` (`Map<String, Any?>`)
- The journey framework constructs forms reflectively with `createInstance()`: provide a no-argument constructor or default all constructor arguments
- Use mutable `var` properties for binding, declared in the body or as defaulted constructor properties. Reuse normalisation helpers such as `StringExtensions.Companion.toNormalizedCurrencyString`
- Prefer class-level `@IsValidPrioritised` plus property-level `@ValidatedBy` / `ConstraintDescriptor`; see [Validation instructions](validation.instructions.md) for ordering and existing Jakarta exceptions
- `formModels/` subdirectory for web form models
- Other HTTP models (e.g. `ConfirmedEmailRequestModel`) need not implement `FormModel`; `searchModels/` contains search/filter models extending `SearchRequestModel`

## ViewModels (`models/viewModels/`)

Prepare data for presentation. Most are for Thymeleaf rendering; `emailModels/` supplies Notify personalisation instead.

Summary rows support multiple actions (constructor excerpt):
```kotlin
data class SummaryListRowViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val actions: List<SummaryListRowActionsViewModel> = emptyList(),
    // Additional display options omitted.
)
```

**Conventions:**
- Prefer immutable data classes for display DTOs, but preserve component hierarchies and builders (e.g. abstract `RadiosViewModel` and `ComplianceActionViewModelBuilder`)
- No form validation — validate submitted input in request models
- Subdirectories: `formModels/` (UI components like radios, selects, checkboxes), `summaryModels/` (including `propertyComplianceViewModels/`), `emailModels/`, `taskModels/`, `filterPanelModels/`, `searchResultModels/`
- Thymeleaf view models are passed to templates and accessed via `${model.property}`
- Use `RadiosViewModel` / `SelectViewModel<T>` for form UI components
- `SummaryListRowViewModel.forCheckYourAnswersPage` has both string-URL and `Destination` overloads, including multiple destination-backed actions. Use `Destination` for journey-aware change links; string-URL overloads still exist
- `EmailTemplateModel.template` selects an `EmailTemplate`; `toHashMap()` supplies Notify personalisation, not a Thymeleaf model. See [Email instructions](emails.instructions.md)

## Summary

| Aspect | DataModels | RequestModels | ViewModels |
|--------|-----------|--------------|-----------|
| **Layer** | Database / Domain / API data | HTTP input | Presentation / Notify |
| **Mutability** | Immutable by default | Mutable for binding | Immutable DTOs; UI types vary |
| **Validation** | Business logic methods | Prioritised constraints preferred | No form validation |
| **Base** | Usually data classes | `FormModel` for journey forms; `SearchRequestModel` for filters | Component-specific |
| **Serialisation** | Boundary-specific: KotlinX / Jackson / Java | HTTP/binding-specific | Template data or Notify personalisation |
