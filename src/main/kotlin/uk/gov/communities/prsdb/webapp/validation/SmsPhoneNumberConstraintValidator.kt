package uk.gov.communities.prsdb.webapp.validation

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

class SmsPhoneNumberConstraintValidator : PropertyConstraintValidator {
    private val phoneNumberValidator = PhoneNumberConstraintValidator()
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    override fun isValid(value: Any?): Boolean {
        return try {
            val number = phoneNumberUtil.parse(value.toString(), "GB")
            if (phoneNumberValidator.isValid(value)) {
                phoneNumberUtil.getNumberType(number) in setOf(PhoneNumberType.MOBILE, PhoneNumberType.FIXED_LINE_OR_MOBILE)
            } else {
                // Notify also accepts Ofcom's reserved SMS test range, which libphonenumber considers invalid.
                number.countryCode == 44 && number.nationalNumber in 7_700_900_000L..7_700_900_999L
            }
        } catch (exception: NumberParseException) {
            false
        }
    }
}
