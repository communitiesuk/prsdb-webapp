package uk.gov.communities.prsdb.webapp.validation

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

class SmsPhoneNumberConstraintValidator : PropertyConstraintValidator {
    private val phoneNumberValidator = PhoneNumberConstraintValidator()
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    override fun isValid(value: Any?): Boolean {
        if (phoneNumberValidator.isValid(value)) {
            return true
        }
        // Notify also accepts Ofcom's reserved SMS test range, which libphonenumber considers invalid.
        return try {
            val number = phoneNumberUtil.parse(value.toString(), "GB")
            number.countryCode == 44 && number.nationalNumber in 7_700_900_000L..7_700_900_999L
        } catch (exception: NumberParseException) {
            false
        }
    }
}
