package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigDecimal

class PositiveBigDecimalValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val bigDecimalValue = value.toString().toBigDecimalOrNull() ?: return false
        return PositiveValidatorForBigDecimal().isValid(bigDecimalValue, null)
    }
}
