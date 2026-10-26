package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_MONTHS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS_BEFORE_14_MONTHS_LATER
import java.time.LocalDate

class CertificateExpiryHelper {
    companion object {
        // GAS_SAFETY_CERT_VALIDITY_MONTHS after the issue date, or the anniversary of the issue date when 14 month expiry is disabled
        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled and use GAS_SAFETY_CERT_VALIDITY_MONTHS only.
        fun getGasExpiryDate(
            issueDate: LocalDate,
            isGas14MonthExpiryEnabled: Boolean,
        ): LocalDate =
            if (isGas14MonthExpiryEnabled) {
                issueDate.plusMonths(GAS_SAFETY_CERT_VALIDITY_MONTHS.toLong())
            } else {
                issueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS_BEFORE_14_MONTHS_LATER.toLong())
            }

        // Valid up to and including the expiry date
        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        fun isGasIssueDateExpired(
            issueDate: LocalDate,
            isGas14MonthExpiryEnabled: Boolean,
            currentDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
        ): Boolean = currentDate > getGasExpiryDate(issueDate, isGas14MonthExpiryEnabled)

        // Expired on the expiry date
        fun isEicExpiryDateExpired(
            expiryDate: LocalDate,
            currentDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
        ): Boolean = currentDate >= expiryDate

        // Expired on the expiry date
        fun isEicrExpiryDateExpired(
            expiryDate: LocalDate,
            currentDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
        ): Boolean = currentDate >= expiryDate

        // Valid up to and including the expiry date
        fun isEpcExpiryDateExpired(
            expiryDate: LocalDate,
            currentDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
        ): Boolean = currentDate > expiryDate
    }
}
