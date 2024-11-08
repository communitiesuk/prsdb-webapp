package uk.gov.communities.prsdb.webapp.validation

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber

class PhoneNumberConstraintValidator : PropertyConstraintValidator {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    override fun isValid(value: Any?): Boolean {
        try {
            val phoneNumber: PhoneNumber = phoneNumberUtil.parse(value.toString(), "GB")
            return if (isValidUKNumber(phoneNumber)) true else isValidInternationalNumber(phoneNumber)
        } catch (e: NumberParseException) {
            return false
        }
    }

    private fun isValidInternationalNumber(phoneNumber: PhoneNumber): Boolean =
        phoneNumber.hasCountryCode() &&
            phoneNumberUtil.isValidNumber(phoneNumber) &&
            phoneNumberUtil.canBeInternationallyDialled(phoneNumber)

    private fun isValidUKNumber(phoneNumber: PhoneNumber): Boolean = phoneNumberUtil.isValidNumberForRegion(phoneNumber, "GB")
}
