package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.DateTimeUnit.Companion.YEAR
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.LocalDate

class MockPropertyComplianceData {
    companion object {
        fun createPropertyCompliance(
            propertyOwnership: PropertyOwnership = MockLandlordData.createPropertyOwnership(),
            gasSafetyFileUploads: List<FileUpload> =
                listOf(FileUpload(FileUploadStatus.QUARANTINED, "gas-safety", "pdf", "etag", "versionId")),
            gasSafetyCertIssueDate: LocalDate? = defaultGasAndEicrIssueDate,
            hasGasSupply: Boolean = true,
            electricalSafetyExpiryDate: LocalDate? = defaultElectricalSafetyExpiryDate,
            epcUrl: String? = "epc.url/0000-0000-0000-0000-0000",
            epcExpiryDate: LocalDate? = defaultEpcExpiryDate,
            tenancyStartedBeforeEpcExpiry: Boolean? = null,
            epcEnergyRating: String? = defaultGoodEpcEnergyRating,
            epcExemptionReason: EpcExemptionReason? = null,
            epcMeesExemptionReason: MeesExemptionReason? = null,
        ) = PropertyCompliance(
            propertyOwnership = propertyOwnership,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            hasGasSupply = hasGasSupply,
            gasSafetyFileUploads = gasSafetyFileUploads.toMutableList(),
            electricalSafetyExpiryDate = electricalSafetyExpiryDate,
            epcUrl = epcUrl,
            epcExpiryDate = epcExpiryDate,
            tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
            epcEnergyRating = epcEnergyRating,
            epcExemptionReason = epcExemptionReason,
            epcMeesExemptionReason = epcMeesExemptionReason,
        )

        val defaultGasAndEicrIssueDate = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate()
        val defaultElectricalSafetyExpiryDate = DateTimeHelper().getCurrentDateInUK().plus(1, YEAR).toJavaLocalDate()
        val defaultEpcExpiryDate = DateTimeHelper().getCurrentDateInUK().plus(5, DAY).toJavaLocalDate()
        val defaultGoodEpcEnergyRating = "C"
    }
}
