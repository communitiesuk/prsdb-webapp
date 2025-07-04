package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import java.time.LocalDate

@PrsdbWebService
class PropertyComplianceService(
    private val propertyComplianceRepository: PropertyComplianceRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
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
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcEnergyRating: String? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
        hasFireSafetyDeclaration: Boolean = false,
    ): PropertyCompliance {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        return propertyComplianceRepository.save(
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
                tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
                epcEnergyRating = epcEnergyRating,
                epcExemptionReason = epcExemptionReason,
                epcMeesExemptionReason = epcMeesExemptionReason,
            ),
        )
    }

    fun getComplianceForProperty(propertyOwnershipId: Long): PropertyCompliance? =
        propertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId)

    fun updatePropertyCompliance(
        propertyOwnershipId: Long,
        update: PropertyComplianceUpdateModel,
    ) {
        val propertyCompliance =
            getComplianceForProperty(propertyOwnershipId)
                ?: throw EntityNotFoundException("No compliance found for property ownership ID: $propertyOwnershipId")

        if (update.gasSafetyCertUpdate != null) {
            propertyCompliance.gasSafetyCertS3Key = update.gasSafetyCertUpdate.s3Key
            propertyCompliance.gasSafetyCertIssueDate = update.gasSafetyCertUpdate.issueDate
            propertyCompliance.gasSafetyCertEngineerNum = update.gasSafetyCertUpdate.engineerNum
            propertyCompliance.gasSafetyCertExemptionReason = update.gasSafetyCertUpdate.exemptionReason
            propertyCompliance.gasSafetyCertExemptionOtherReason = update.gasSafetyCertUpdate.exemptionOtherReason
        }

        if (update.eicrUpdate != null) {
            propertyCompliance.eicrS3Key = update.eicrUpdate.s3Key
            propertyCompliance.eicrIssueDate = update.eicrUpdate.issueDate
            propertyCompliance.eicrExemptionReason = update.eicrUpdate.exemptionReason
            propertyCompliance.eicrExemptionOtherReason = update.eicrUpdate.exemptionOtherReason
        }

        if (update.epcUpdate != null) {
            propertyCompliance.epcUrl = update.epcUpdate.url
            propertyCompliance.epcExpiryDate = update.epcUpdate.expiryDate
            propertyCompliance.tenancyStartedBeforeEpcExpiry = update.epcUpdate.tenancyStartedBeforeExpiry
            propertyCompliance.epcEnergyRating = update.epcUpdate.energyRating
            propertyCompliance.epcExemptionReason = update.epcUpdate.exemptionReason
            propertyCompliance.epcMeesExemptionReason = update.epcUpdate.meesExemptionReason
        }

        propertyComplianceRepository.save(propertyCompliance)
    }

    fun addToPropertiesWithComplianceAddedThisSession(propertyOwnershipId: Long) {
        val currentSet = getPropertiesWithComplianceAddedThisSession()
        val updatedSet = currentSet + propertyOwnershipId
        session.setAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION, updatedSet)
    }

    fun wasPropertyComplianceAddedThisSession(propertyOwnershipId: Long): Boolean =
        getPropertiesWithComplianceAddedThisSession().contains(propertyOwnershipId)

    @Suppress("UNCHECKED_CAST")
    private fun getPropertiesWithComplianceAddedThisSession() =
        session.getAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION) as? Set<Long> ?: emptySet()
}
