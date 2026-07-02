package uk.gov.communities.prsdb.webapp.validation

class DigitsOnlyConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val stringValue = value?.toString() ?: return true
        return stringValue.all { it.isDigit() }
    }
}
