package uk.gov.communities.prsdb.webapp.validation

import kotlin.reflect.KClass

annotation class ConstraintDescriptor(
    val validatorType: KClass<out PrioritisedConstraintValidator>,
    val messageKey: String,
    // Must be supplied if the validatorType is DelegatedPropertyConstraintValidator
    val targetMethod: String = "",
)
