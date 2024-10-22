package uk.gov.communities.prsdb.webapp.validation.constraints

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.communities.prsdb.webapp.validation.validators.ConfirmedEmailValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ConfirmedEmailValidator::class])
annotation class ValidConfirmedEmail(
    val message: String = "Email must match ConfirmedEmail",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)
