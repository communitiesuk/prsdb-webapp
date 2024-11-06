package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator

class EmailConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = EmailValidator().isValid(value as? CharSequence, null)
}
