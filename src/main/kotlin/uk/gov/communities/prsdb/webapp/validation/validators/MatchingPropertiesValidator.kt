package uk.gov.communities.prsdb.webapp.validation.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.communities.prsdb.webapp.validation.constraints.HasMatchingProperties
import uk.gov.communities.prsdb.webapp.validation.constraints.HasMatchingProperties.Matches
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class MatchingPropertiesValidator : ConstraintValidator<HasMatchingProperties, Any> {
    override fun isValid(
        value: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return false

        value::class.memberProperties.forEach { property ->
            if (property.hasAnnotation<Matches>()) {
                val annotation = property.findAnnotation<Matches>()!!
                val matchingProperty = value::class.memberProperties.single { it.name == annotation.propertyName }

                if (matchingProperty.call(value) != property.call(value)) {
                    return false
                }
            }
        }

        return true
    }
}
