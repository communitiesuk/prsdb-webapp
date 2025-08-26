package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator

open class EmailConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = EmailValidator().isValid(value as? CharSequence, null)
}

class OptionalEmailConstraintValidator : EmailConstraintValidator() {
    override fun isValid(value: Any?): Boolean {
        if (value == null) return true
        if (value is String && value.isBlank()) return true
        return super.isValid(value)
    }
}
