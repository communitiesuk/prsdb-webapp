package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
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
            gasSafetyCertS3Key: String? = "property-gas-safety-cert.pdf",
            gasSafetyCertIssueDate: LocalDate? = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate(),
            gasSafetyCertEngineerNum: String? = "1234567",
            gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
            gasSafetyCertExemptionOtherReason: String? = null,
            eicrS3Key: String? = "eicr.pdf",
            eicrIssueDate: LocalDate? = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate(),
            eicrExemptionReason: EicrExemptionReason? = null,
            eicrExemptionOtherReason: String? = null,
            epcUrl: String? = "epc.url/0000-0000-0000-0000-0000",
            epcExpiryDate: LocalDate? = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate(),
            tenancyStartedBeforeEpcExpiry: Boolean? = null,
            epcEnergyRating: String? = "C",
            epcExemptionReason: EpcExemptionReason? = null,
            epcMeesExemptionReason: MeesExemptionReason? = null,
            hasFireSafetyDeclaration: Boolean = true,
        ) = PropertyCompliance(
            propertyOwnership = propertyOwnership,
            hasFireSafetyDeclaration = hasFireSafetyDeclaration,
            gasSafetyCertUpload = gasSafetyCertS3Key?.let { FileUpload(FileUploadStatus.QUARANTINED, it) },
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            gasSafetyCertEngineerNum = gasSafetyCertEngineerNum,
            gasSafetyCertExemptionReason = gasSafetyCertExemptionReason,
            gasSafetyCertExemptionOtherReason = gasSafetyCertExemptionOtherReason,
            eicrUpload = eicrS3Key?.let { FileUpload(FileUploadStatus.QUARANTINED, it) },
            eicrIssueDate = eicrIssueDate,
            eicrExemptionReason = eicrExemptionReason,
            eicrExemptionOtherReason = eicrExemptionOtherReason,
            epcUrl = epcUrl,
            epcExpiryDate = epcExpiryDate,
            tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
            epcEnergyRating = epcEnergyRating,
            epcExemptionReason = epcExemptionReason,
            epcMeesExemptionReason = epcMeesExemptionReason,
        )
    }
}
