package uk.gov.communities.prsdb.webapp.validation

class TrueConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = value.toString().equals("true", ignoreCase = true)
}
