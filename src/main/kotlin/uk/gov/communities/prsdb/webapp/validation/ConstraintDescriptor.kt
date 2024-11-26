package uk.gov.communities.prsdb.webapp.validation

import kotlin.reflect.KClass

annotation class ConstraintDescriptor(
    val messageKey: String,
    val validatorType: KClass<out PrioritisedConstraintValidator>,
    // Validator constructor arguments are passed as strings due to limitations on annotation member types
    // https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.1
    val validatorArgs: Array<String> = [],
    // Must be supplied if the validatorType is DelegatedPropertyConstraintValidator
    val targetMethod: String = "",
)
