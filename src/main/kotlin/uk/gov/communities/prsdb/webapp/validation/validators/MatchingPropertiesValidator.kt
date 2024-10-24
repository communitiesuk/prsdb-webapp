package uk.gov.communities.prsdb.webapp.validation.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.ValidationException
import uk.gov.communities.prsdb.webapp.validation.constraints.HasMatchingStringProperties
import uk.gov.communities.prsdb.webapp.validation.constraints.HasMatchingStringProperties.Matches
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class MatchingPropertiesValidator : ConstraintValidator<HasMatchingStringProperties, Any> {
    override fun isValid(
        value: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return false

        value::class.memberProperties.forEach { property ->
            if (property.hasAnnotation<Matches>()) {
                val annotation = property.findAnnotation<Matches>()!!
                val matchingProperty = value::class.memberProperties.single { it.name == annotation.propertyName }

                val stringValue = property.call(value) as? String
                val matchingStringValue = matchingProperty.call(value) as? String

                if (stringValue == null || matchingStringValue == null) {
                    throw ValidationException(
                        exceptionMessageForProperties(value, property, matchingProperty),
                    )
                }
                if (stringValue.trim() != matchingStringValue.trim()) {
                    return false
                }
            }
        }

        return true
    }

    companion object {
        fun exceptionMessageForProperties(
            value: Any,
            prop1: KProperty1<out Any, *>,
            prop2: KProperty1<out Any, *>,
        ): String =
            "On annotated class ${value::class.simpleName}, " +
                "property ${prop1.name} was type: ${prop1.returnType} and " +
                "property ${prop2.name} was type: ${prop1.returnType}. \n" +
                MESSAGE_CONSTANT

        const val MESSAGE_CONSTANT =
            "HasMatchingStringProperties can only compare matching String properties on a data model. " +
                "If you need to have other matching property types, create a new Annotation and Validator."
    }
}
