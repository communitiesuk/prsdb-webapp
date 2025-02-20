package uk.gov.communities.prsdb.webapp.helpers

import com.google.i18n.phonenumbers.PhoneNumberUtil

fun PhoneNumberUtil.getFormattedUkPhoneNumber(): String {
    val number = this.getExampleNumber("GB")
    return "${number.countryCode}${number.nationalNumber}"
}

fun PhoneNumberUtil.getFormattedInternationalPhoneNumber(regionCode: String): String {
    val number = this.getExampleNumber(regionCode)
    return "+${number.countryCode}${number.nationalNumber}"
}
