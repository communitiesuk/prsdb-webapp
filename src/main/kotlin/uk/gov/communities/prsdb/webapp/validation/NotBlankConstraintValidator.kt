package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator

class NotBlankConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = NotBlankValidator().isValid(value as CharSequence?, null)
}
