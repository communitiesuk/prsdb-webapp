package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import java.time.LocalDate

class CertificateExpiryHelper {
    companion object {
        // Valid up to and including the anniversary of the issue date
        fun isGasIssueDateExpired(
            issueDate: LocalDate,
            currentDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
        ): Boolean = currentDate > issueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())

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
