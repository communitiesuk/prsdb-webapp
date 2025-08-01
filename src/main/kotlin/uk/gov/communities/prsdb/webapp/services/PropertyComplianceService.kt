package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import java.time.LocalDate

@PrsdbWebService
class PropertyComplianceService(
    private val propertyComplianceRepository: PropertyComplianceRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    fun createPropertyCompliance(
        propertyOwnershipId: Long,
        gasSafetyCertUploadId: Long? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertEngineerNum: String? = null,
        gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyCertExemptionOtherReason: String? = null,
        eicrUploadId: Long? = null,
        eicrIssueDate: LocalDate? = null,
        eicrExemptionReason: EicrExemptionReason? = null,
        eicrExemptionOtherReason: String? = null,
        epcUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcEnergyRating: String? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
    ): PropertyCompliance {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val gasSafetyUpload = gasSafetyCertUploadId?.let { fileUploadRepository.getReferenceById(it) }
        val eicrUpload = eicrUploadId?.let { fileUploadRepository.getReferenceById(it) }
        return propertyComplianceRepository.save(
            PropertyCompliance(
                propertyOwnership = propertyOwnership,
                gasSafetyCertUpload = gasSafetyUpload,
                gasSafetyCertIssueDate = gasSafetyCertIssueDate,
                gasSafetyCertEngineerNum = gasSafetyCertEngineerNum,
                gasSafetyCertExemptionReason = gasSafetyCertExemptionReason,
                gasSafetyCertExemptionOtherReason = gasSafetyCertExemptionOtherReason,
                eicrUpload = eicrUpload,
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

    fun getComplianceForPropertyOrNull(propertyOwnershipId: Long): PropertyCompliance? =
        propertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId)

    fun getComplianceForProperty(propertyOwnershipId: Long): PropertyCompliance =
        getComplianceForPropertyOrNull(propertyOwnershipId)
            ?: throw EntityNotFoundException("No compliance record found for property ownership ID: $propertyOwnershipId")

    @Transactional
    fun updatePropertyCompliance(
        propertyOwnershipId: Long,
        update: PropertyComplianceUpdateModel,
        checkUpdateIsValid: () -> Unit,
    ) {
        checkUpdateIsValid()
        val propertyCompliance = getComplianceForProperty(propertyOwnershipId)

        if (update.gasSafetyCertUpdate != null) {
            propertyCompliance.gasSafetyFileUpload =
                update.gasSafetyCertUpdate.fileUploadId?.let { fileUploadRepository.getReferenceById(it) }
            propertyCompliance.gasSafetyCertIssueDate = update.gasSafetyCertUpdate.issueDate
            propertyCompliance.gasSafetyCertEngineerNum = update.gasSafetyCertUpdate.engineerNum
            propertyCompliance.gasSafetyCertExemptionReason = update.gasSafetyCertUpdate.exemptionReason
            propertyCompliance.gasSafetyCertExemptionOtherReason = update.gasSafetyCertUpdate.exemptionOtherReason
        }

        if (update.eicrUpdate != null) {
            propertyCompliance.eicrFileUpload = update.eicrUpdate.fileUploadId?.let { fileUploadRepository.getReferenceById(it) }
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
