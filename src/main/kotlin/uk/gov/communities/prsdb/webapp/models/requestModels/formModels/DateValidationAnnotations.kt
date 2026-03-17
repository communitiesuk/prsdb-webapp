package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingAll",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingDM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndMonthBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingDY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingD",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidAll",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidDM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndMonthInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidDY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidD",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidDay",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidFormat",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidDate",
        ),
    ],
)
annotation class AnyDateDayValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndMonthBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingMY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notMonthAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingM",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndMonthInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidMY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notMonthAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidM",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidMonth",
        ),
    ],
)
annotation class AnyDateMonthValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notAllBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notMonthAndYearBlank",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.missingY",
            validatorType = NotBlankConstraintValidator::class,
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notAllInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notDayAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "notMonthAndYearInvalid",
        ),
        ConstraintDescriptor(
            messageKey = "forms.date.error.invalidY",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidYear",
        ),
    ],
)
annotation class AnyDateYearValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@AnyDateDayValidation
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "forms.todayOrPastDate.error.invalidDate",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidDateFromTodayOrPast",
        ),
    ],
)
annotation class TodayOrPastDateDayValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@AnyDateMonthValidation
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidDateFromTodayOrPast",
        ),
    ],
)
annotation class TodayOrPastDateMonthValidation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@AnyDateYearValidation
@ValidatedBy(
    constraints = [
        ConstraintDescriptor(
            messageKey = "_",
            validatorType = DelegatedPropertyConstraintValidator::class,
            targetMethod = "isValidDateFromTodayOrPast",
        ),
    ],
)
annotation class TodayOrPastDateYearValidation
