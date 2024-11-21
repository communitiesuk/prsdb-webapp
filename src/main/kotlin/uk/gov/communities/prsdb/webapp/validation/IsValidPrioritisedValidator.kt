package uk.gov.communities.prsdb.webapp.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters

/**
 * Searches for all properties on the class of the instance which have @ValidatedBy annotations, then checks
 * each of the constraints in order; if one fails, the message key for that constraint is added to the field and
 * the validator moves on to the next @ValidatedBy annotation.
 */
class IsValidPrioritisedValidator : ConstraintValidator<IsValidPrioritised, Any> {
    override fun isValid(
        instance: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        var isValid = true

        for (property in instance!!::class.memberProperties) {
            for (validatedBy in property.annotations.filterIsInstance<ValidatedBy>()) {
                for (constraint in validatedBy.constraints) {
                    val validationPassed =
                        when {
                            constraint.validatorType.isSubclassOf(DelegatedPropertyConstraintValidator::class) ->
                                validateDelegatedPropertyConstraint(
                                    instance,
                                    constraint,
                                    context,
                                    property.name,
                                )

                            constraint.validatorType.isSubclassOf(PropertyConstraintValidator::class) ->
                                validatePropertyConstraint(
                                    instance,
                                    constraint,
                                    context,
                                    property,
                                )

                            else -> throw UnsupportedOperationException("Unknown constraint validator: ${constraint.validatorType}")
                        }
                    if (!validationPassed) {
                        isValid = false
                        break
                    }
                }
            }
        }

        return isValid
    }

    private fun validateDelegatedPropertyConstraint(
        instance: Any,
        constraint: ConstraintDescriptor,
        context: ConstraintValidatorContext?,
        propertyName: String,
    ): Boolean {
        if (constraint.targetMethod.isBlank()) {
            throw IllegalArgumentException(
                "Constraint with validatorType ${constraint.validatorType.simpleName} must supply a targetMethod",
            )
        }
        val function = instance::class.memberFunctions.firstOrNull { it.name == constraint.targetMethod }
        if (function == null) {
            throw IllegalArgumentException(
                "No function named ${constraint.targetMethod} found on ${instance::class.simpleName}",
            )
        }
        if (function.returnType != Boolean::class.createType() || function.valueParameters.isNotEmpty()) {
            throw IllegalArgumentException(
                "Function named ${constraint.targetMethod} on ${instance::class.simpleName} must return Boolean and take no parameters",
            )
        }
        @Suppress("UNCHECKED_CAST")
        val typedFunction = function as KFunction<Boolean>

        return if (!typedFunction.call(instance)) {
            buildViolation(context, constraint.messageKey, propertyName)
            false
        } else {
            true
        }
    }

    private fun validatePropertyConstraint(
        instance: Any,
        constraint: ConstraintDescriptor,
        context: ConstraintValidatorContext?,
        property: KProperty1<out Any, *>,
    ): Boolean {
        val validator: PropertyConstraintValidator
        try {
            validator =
                constraint.validatorType.constructors
                    .first()
                    .call(*constraint.validatorArgs) as PropertyConstraintValidator
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException(
                "${constraint.validatorType.simpleName} expects ${constraint.validatorType.constructors.first().valueParameters.size}" +
                    " validatorArgs, but ${constraint.validatorArgs.size} were provided",
            )
        }

        val propertyValue = property.getter.call(instance)

        return if (!validator.isValid(propertyValue)) {
            buildViolation(context, constraint.messageKey, property.name)
            false
        } else {
            true
        }
    }

    private fun buildViolation(
        context: ConstraintValidatorContext?,
        message: String,
        propertyName: String,
    ) {
        context?.disableDefaultConstraintViolation()
        context
            ?.buildConstraintViolationWithTemplate(message)
            ?.addPropertyNode(propertyName)
            ?.addConstraintViolation()
    }
}
