package uk.gov.communities.prsdb.webapp.validation

/**
 * Used to validate a property simply by inspecting its value
 */
interface PropertyConstraintValidator : PrioritisedConstraintValidator {
    fun isValid(value: Any?): Boolean
}
