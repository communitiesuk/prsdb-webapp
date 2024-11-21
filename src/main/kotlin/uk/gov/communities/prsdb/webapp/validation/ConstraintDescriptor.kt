package uk.gov.communities.prsdb.webapp.validation

import kotlin.reflect.KClass

annotation class ConstraintDescriptor(
    val messageKey: String,
    val validatorType: KClass<out PrioritisedConstraintValidator>,
    val validatorArgs: Array<String> = [],
    // Must be supplied if the validatorType is DelegatedPropertyConstraintValidator
    val targetMethod: String = "",
)
