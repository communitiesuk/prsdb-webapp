---
applyTo: "**/validation/**"
---

# Validation Instructions

## Custom Validation Framework

The codebase uses a custom validation framework based on `@ValidatedBy` annotations on form model fields, NOT standard Jakarta constraint annotations.

### Form Model Validation
```kotlin
@IsValidPrioritised
class ExampleFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.example.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.example.error.invalidFormat",
                validatorType = PhoneNumberConstraintValidator::class,
            ),
        ],
    )
    var fieldName: String? = null
}
```

### Validator Hierarchy
All validators extend `PropertyConstraintValidator`:
```kotlin
class MyValidator : PropertyConstraintValidator() {
    override fun isValid(value: Any?): Boolean {
        // validation logic
    }
}
```

## Common Validators
- `NotBlankConstraintValidator` — required field (non-blank)
- `NotNullConstraintValidator` — required field (non-null)
- `EmailConstraintValidator` — email format (+ `OptionalEmailConstraintValidator` variant)
- `PhoneNumberConstraintValidator` — UK phone numbers (using libphonenumber)
- `GasSafeEngineerNumConstraintValidator` — gas safety engineer registration numbers
- `LengthConstraintValidator` — string length bounds
- `PositiveIntegerValidator` — positive integer range
- `PositiveBigDecimalValidator` — positive decimal numbers
- `TrueConstraintValidator` — boolean true assertion
- `DateValidator` — date format and range validation
- `DelegatedPropertyConstraintValidator` — delegates to a method for validation

## Error Messages

### Message Keys
- Define in YAML files in `messages/`
- Use pattern: `forms.{section}.error.{errorType}`
- Example: `forms.phoneNumber.error.invalid: Enter a valid UK phone number`

### Custom Error Messages
```kotlin
override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
    if (!isValidFormat(value)) {
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate("validation.custom.message")
            .addConstraintViolation()
        return false
    }
    return true
}
```

## Page-Level Validation
```kotlin
class ExamplePage : Page() {
    
    override fun validate(journeyData: JourneyData): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Cross-field validation
        if (startDate > endDate) {
            errors.add(ValidationError("endDate", "validation.dateRange.invalid"))
        }
        
        return errors
    }
}
```

## Testing Validators
```kotlin
class ExampleValidatorTests {
    private val validator = ExampleValidator()
    
    @Test
    fun `valid input passes`() {
        assertThat(validator.isValid("valid", mockContext)).isTrue()
    }
    
    @Test
    fun `invalid input fails`() {
        assertThat(validator.isValid("invalid", mockContext)).isFalse()
    }
}
```
