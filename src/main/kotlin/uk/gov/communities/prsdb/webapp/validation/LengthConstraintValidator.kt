package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.constraints.Length
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator

class LengthConstraintValidator(
    private val min: String,
    private val max: String,
) : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val lengthValidator = LengthValidator()
        lengthValidator.initialize(Length(min = min.toInt(), max = max.toInt()))
        return lengthValidator.isValid(value as CharSequence?, null)
    }
}
