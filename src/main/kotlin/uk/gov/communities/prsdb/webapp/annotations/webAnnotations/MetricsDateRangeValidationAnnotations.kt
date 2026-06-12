package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// "From" date validation. Mirrors the AnyDate* validation chains, but delegates to the
// from-prefixed methods on MetricsDateRangeFormModel so that two dates can be validated on
// a single form model.

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingAll",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingDM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndMonthBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingDY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingD",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidAll",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidDM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndMonthInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidDY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidD",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromIsValidDay",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidFormat",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromIsValidDate",
        ),
    ],
)
annotation class FromDateDayValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndMonthBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingMY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotMonthAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingM",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndMonthInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidMY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotMonthAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromIsValidMonth",
        ),
    ],
)
annotation class FromDateMonthValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotMonthAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingY",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotDayAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromNotMonthAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "fromIsValidYear",
        ),
    ],
)
annotation class FromDateYearValidation

// "To" date validation. As above but delegates to the to-prefixed methods. The day annotation
// additionally appends the cross-field "to is on or after from" constraint as its final check,
// so it only fires once the to date is itself a valid, complete date.

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingAll",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingDM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndMonthBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingDY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingD",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidAll",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidDM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndMonthInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidDY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidD",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toIsValidDay",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidFormat",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toIsValidDate",
        ),
        ConstraintDescriptor(
            messageKey = "metrics.dateRange.error.toBeforeFrom",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toIsOnOrAfterFromDate",
        ),
    ],
)
annotation class ToDateDayValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndMonthBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingMY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotMonthAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingM",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndMonthInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidMY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotMonthAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toIsValidMonth",
        ),
    ],
)
annotation class ToDateMonthValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotMonthAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingY",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotDayAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toNotMonthAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "toIsValidYear",
        ),
    ],
)
annotation class ToDateYearValidation
