package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import java.time.format.DateTimeFormatter

class MockEpcData {
    companion object {
        const val DEFAULT_EPC_CERTIFICATE_NUMBER = "0000-0000-0000-0892-1563"

        const val SECONDARY_EPC_CERTIFICATE_NUMBER = "0000-0000-0554-8411-0000"

        val expiryDateInThePast = LocalDate(2022, 1, 5)

        fun createEpcDataModel(
            certificateNumber: String = DEFAULT_EPC_CERTIFICATE_NUMBER,
            singleLineAddress: String = "1 Example Street, Example Town, EX1 1EX",
            energyRating: String = "C",
            expiryDate: LocalDate = LocalDate(2027, 1, 1),
            latestCertificateNumberForThisProperty: String? = DEFAULT_EPC_CERTIFICATE_NUMBER,
        ) = EpcDataModel(
            certificateNumber = certificateNumber,
            singleLineAddress = singleLineAddress,
            energyRating = energyRating,
            expiryDate = expiryDate,
            latestCertificateNumberForThisProperty = latestCertificateNumberForThisProperty,
        )

        fun createEpcRegisterClientEpcFoundResponse(
            certificateNumber: String = DEFAULT_EPC_CERTIFICATE_NUMBER,
            energyRating: String = "C",
            expiryDate: LocalDate = DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 5)),
            latestCertificateNumberForThisProperty: String = DEFAULT_EPC_CERTIFICATE_NUMBER,
        ) = """
            {
                "data": {
                    "epcRrn": "$certificateNumber",
                    "currentEnergyEfficiencyBand": "$energyRating",
                    "expiryDate": "${formatLocalDateToISO(expiryDate)}",
                    "latestEpcRrnForAddress": "$latestCertificateNumberForThisProperty",
                    "address": {
                        "addressLine1": "123 Test Street",
                        "town": "Test Town",
                        "postcode": "TT1 1TT",
                        "addressLine2": "Flat 1"
                    }
                }
            }
            """.trimIndent()

        val epcRegisterClientEpcNotFoundResponse =
            """
            {
                "errors": [
                    {
                        "code": "NOT_FOUND",
                        "title": "Certificate not found"
                    }
                ]
            }
            """.trimIndent()

        private fun formatLocalDateToISO(localDate: LocalDate): String {
            val startOfDayInstant = localDate.atStartOfDayIn(TimeZone.of("Europe/London"))
            return DateTimeFormatter.ISO_INSTANT.format(startOfDayInstant.toJavaInstant())
        }
    }
}
