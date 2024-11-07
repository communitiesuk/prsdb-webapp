package uk.gov.communities.prsdb.webapp.validation

import jakarta.validation.constraints.Pattern
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator
// import com.google.i18n.phonenumbers.Phonenumber

// fun isValidNumber(number: Phonenumber.PhoneNumber): Boolean

// TODO use libaray to validate (check if valid uk number, then if valid international number)

class PhoneNumberConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean {
        val validator = PatternValidator()
        validator.initialize(Pattern(regexp = """(\d+ ?)+"""))
        return validator.isValid(value as? CharSequence, null)
    }
}
