package uk.gov.communities.prsdb.webapp.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * @IsValidPrioritised should be placed on a class to cause all properties with @ValidatedBy annotations to be
 * validated according to their constraints
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IsValidPrioritisedValidator::class])
annotation class IsValidPrioritised(
    val message: String = "Prioritised validators were not satisfied",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
