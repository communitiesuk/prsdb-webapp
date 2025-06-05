package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
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
            epcUrl: String? = "epc.url",
            epcExpiryDate: LocalDate? = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate(),
            epcEnergyRating: String? = "C",
            epcExemptionReason: EpcExemptionReason? = null,
            epcMeesExemptionReason: MeesExemptionReason? = null,
            hasFireSafetyDeclaration: Boolean = true,
        ) = PropertyCompliance(
            propertyOwnership = propertyOwnership,
            hasFireSafetyDeclaration = hasFireSafetyDeclaration,
            gasSafetyCertS3Key = gasSafetyCertS3Key,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            gasSafetyCertEngineerNum = gasSafetyCertEngineerNum,
            gasSafetyCertExemptionReason = gasSafetyCertExemptionReason,
            gasSafetyCertExemptionOtherReason = gasSafetyCertExemptionOtherReason,
            eicrS3Key = eicrS3Key,
            eicrIssueDate = eicrIssueDate,
            eicrExemptionReason = eicrExemptionReason,
            eicrExemptionOtherReason = eicrExemptionOtherReason,
            epcUrl = epcUrl,
            epcExpiryDate = epcExpiryDate,
            epcEnergyRating = epcEnergyRating,
            epcExemptionReason = epcExemptionReason,
            epcMeesExemptionReason = epcMeesExemptionReason,
        )
    }
}
