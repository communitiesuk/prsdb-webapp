package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForInteger

class PositiveIntegerValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        try {
            val integerValue = value.toString().toInt()
            return PositiveValidatorForInteger().isValid(integerValue, null)
        } catch (e: NumberFormatException) {
            return false
        }
    }
}
