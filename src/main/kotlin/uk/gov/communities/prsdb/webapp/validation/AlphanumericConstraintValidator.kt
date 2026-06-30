package uk.gov.communities.prsdb.webapp.validation

class AlphanumericConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val stringValue = value?.toString() ?: return true
        return stringValue.all { it.isDigit() || it.uppercaseChar() in 'A'..'Z' }
    }
}
