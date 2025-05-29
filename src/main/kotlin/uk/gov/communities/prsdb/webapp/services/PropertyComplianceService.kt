package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import java.time.LocalDate

@Service
class PropertyComplianceService(
    private val propertyComplianceRepository: PropertyComplianceRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun createPropertyCompliance(
        propertyOwnershipId: Long,
        gasSafetyCertS3Key: String? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertEngineerNum: String? = null,
        gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyCertExemptionOtherReason: String? = null,
        eicrS3Key: String? = null,
        eicrIssueDate: LocalDate? = null,
        eicrExemptionReason: EicrExemptionReason? = null,
        eicrExemptionOtherReason: String? = null,
        epcUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        epcEnergyRating: String? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
        hasFireSafetyDeclaration: Boolean = false,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        propertyComplianceRepository.save(
            PropertyCompliance(
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
            ),
        )
    }
}
