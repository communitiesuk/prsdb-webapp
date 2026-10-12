---
applyTo: "**/validation/**,**/models/requestModels/**,**/annotations/webAnnotations/DateValidationAnnotations.kt,**/annotations/webAnnotations/MetricsDateRangeValidationAnnotations.kt"
---

# Validation Instructions

## Custom Validation Framework

Prefer prioritised validation for new work: class-level `@IsValidPrioritised` integrates with Jakarta validation, and property-level `@ValidatedBy` declares ordered constraints. Existing Jakarta/mixed models such as `OccupancyFormModel` and `RentFrequencyFormModel` are policy exceptions, not interchangeable defaults for new forms.

### Form Model Validation
```kotlin
@IsValidPrioritised
class PhoneNumberFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerAsALandlord.phoneNumber.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "registerAsALandlord.phoneNumber.error.invalidFormat",
                validatorType = PhoneNumberConstraintValidator::class,
            ),
        ],
    )
    var phoneNumber: String? = null
}
```

- Constraints run in order and stop at the first failure **per property**, not the first failure across the whole model
- Composed annotations are supported: constraints are discovered recursively in annotation declaration order. Reuse `DateValidationAnnotations.kt` and `MetricsDateRangeValidationAnnotations.kt` under `annotations/webAnnotations/`

### Validator Hierarchy
`PrioritisedConstraintValidator` is the umbrella marker interface. Its two branches are `PropertyConstraintValidator` (value-based validation) and `DelegatedPropertyConstraintValidator` (a marker for methods on the validated model).

Implement the interface without constructor parentheses; leaf validators take only the property value:
```kotlin
class MyValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = (value as? String)?.isNotBlank() == true
}
```

## Common Validators
- `NotBlankConstraintValidator` — required field (non-blank)
- `NotNullConstraintValidator` — required field (non-null)
- `EmailConstraintValidator` — email format (+ `OptionalEmailConstraintValidator` variant)
- `PhoneNumberConstraintValidator` — valid UK numbers and valid internationally diallable numbers (using libphonenumber)
- `GasSafeEngineerNumConstraintValidator` — gas safety engineer registration numbers
- `LengthConstraintValidator` — string length bounds
- `PositiveIntegerValidator` — parses a positive Kotlin `Int`; not a configurable range validator
- `PositiveBigDecimalValidator` — positive decimals strictly below `10000000`; rent's two-decimal-place rule is a separate delegated constraint
- `TrueConstraintValidator` — boolean true assertion
- `DateValidator` — companion helper for date parts, completeness and valid dates, not a `PropertyConstraintValidator`
- `DelegatedPropertyConstraintValidator` — invokes a no-argument `Boolean` method on the validated model

## Error Messages

### Message Keys
- Define in YAML files under `src/main/resources/messages/`
- Follow the relevant message namespace, commonly `forms.{section}.error.{errorType}`, but not exclusively: `WhoProvidesRentalDetailsFormModel` uses `registerProperty.whoProvidesRentalDetails.radios.error.missing`
- Company-number keys use `forms.orgCompanyNumber.error.*`, e.g. `forms.orgCompanyNumber.error.missing`
- `messageKey = "_"` marks a failure without a displayed message (e.g. additional date fields); it does not make the constraint pass

### Constraint Messages and Arguments
Leaf validators return a Boolean; `ConstraintDescriptor.messageKey` supplies the error message. Constructor arguments are passed as strings through `validatorArgs`:
```kotlin
ConstraintDescriptor(
    messageKey = "forms.orgCompanyNumber.error.length",
    validatorType = LengthConstraintValidator::class,
    validatorArgs = ["8", "8"],
)
```

For delegated constraints, set `validatorType = DelegatedPropertyConstraintValidator::class` and `targetMethod` to a method on the same model that takes no arguments and returns non-null `Boolean`.

## Cross-Field and Journey-State Validation
- Same-model cross-field checks use delegated constraints, e.g. `ConfirmedEmailRequestModel.isConfirmEmailSameAsEmail()`
- Checks needing earlier answers or cached journey data belong in the step config's `afterPrimaryValidation(state, bindingResult)`. Guard against primary errors before parsing values and use `rejectValueWithMessageKey` to attach a field error
- See `TenantsStepConfig` for comparing people with households, and `SelectAddressStepConfig` for rejecting an address outside the cached choices

Example inside `TenantsStepConfig`:
```kotlin
override fun afterPrimaryValidation(
    state: HouseholdsAndTenantsState,
    bindingResult: BindingResult,
) {
    if (bindingResult.hasErrors()) return
    val form = bindingResult.getFormModel()
    if (form.numberOfPeople.toInt() < state.households.formModel.numberOfHouseholds.toInt()) {
        bindingResult.rejectValueWithMessageKey(
            NewNumberOfPeopleFormModel::numberOfPeople.name,
            "forms.numberOfPeople.input.error.invalidNumber",
        )
    }
}
```

## Testing Validators
Call leaf validators with one argument; model-level tests should also assert the property and first failing message key, including composed/delegated cases.

```kotlin
class PositiveBigDecimalValidatorTests {
    private val validator = PositiveBigDecimalValidator()

    @Test
    fun `positive value below storage limit passes`() {
        assertThat(validator.isValid("9999999.99")).isTrue()
    }

    @Test
    fun `storage limit fails`() {
        assertThat(validator.isValid("10000000")).isFalse()
    }
}
```