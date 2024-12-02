package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForInteger

class PositiveOrZeroIntegerValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = PositiveOrZeroValidatorForInteger().isValid(value as Int, null)
}
