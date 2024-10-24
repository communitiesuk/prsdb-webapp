package uk.gov.communities.prsdb.webapp.validation.constraints

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.communities.prsdb.webapp.validation.validators.MatchingPropertiesValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [MatchingPropertiesValidator::class])
annotation class HasMatchingStringProperties(
    val message: String = "Property values must match",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
) {
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Matches(
        val propertyName: String,
    )
}
