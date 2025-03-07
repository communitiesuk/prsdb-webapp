package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForInteger

class PositiveIntegerValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val integerValue = value.toString().toIntOrNull() ?: return false
        return PositiveValidatorForInteger().isValid(integerValue, null)
    }
}
