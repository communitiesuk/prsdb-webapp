package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForInteger

class PositiveOrZeroIntegerValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        try {
            val integerValue = value.toString().toInt()
            return PositiveOrZeroValidatorForInteger().isValid(integerValue, null)
        } catch (e: NumberFormatException) {
            return false
        }
    }
}
