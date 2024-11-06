package uk.gov.communities.prsdb.webapp.validation

import jakarta.validation.constraints.Pattern
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator

class PhoneNumberConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val validator = PatternValidator()
        validator.initialize(Pattern(regexp = """(\d+ ?)+"""))
        return validator.isValid(value as? CharSequence, null)
    }
}
