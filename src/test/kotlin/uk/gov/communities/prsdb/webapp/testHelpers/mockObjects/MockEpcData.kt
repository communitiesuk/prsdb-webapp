package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.LocalDate
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

class MockEpcData {
    companion object {
        const val DEFAULT_EPC_CERTIFICATE_NUMBER = "0000-0000-0000-0554-8410"

        const val SECONDARY_EPC_CERTIFICATE_NUMBER = "0000-0000-0554-8411-0000"

        fun createEpcDataModel(
            certificateNumber: String = DEFAULT_EPC_CERTIFICATE_NUMBER,
            singleLineAddress: String = "1 Example Street, Example Town, EX1 1EX",
            energyRating: String = "C",
            expiryDate: LocalDate = LocalDate(2027, 1, 1),
            latestCertificateNumberForThisProperty: String = DEFAULT_EPC_CERTIFICATE_NUMBER,
        ) = EpcDataModel(
            certificateNumber = certificateNumber,
            singleLineAddress = singleLineAddress,
            energyRating = energyRating,
            expiryDate = expiryDate,
            latestCertificateNumberForThisProperty = latestCertificateNumberForThisProperty,
        )
    }
}
