package uk.gov.communities.prsdb.webapp.validation

class GasSafeEngineerNumConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val baseValue = value.toString().filter { !it.isWhitespace() }
        return baseValue.length == 7 && baseValue.all { it.isDigit() }
    }
}
