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
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.CertificateUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EicrUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EpcUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ComplianceUpdateConfirmationEmail
import java.time.LocalDate
import kotlin.String

@PrsdbWebService
class PropertyComplianceService(
    private val propertyComplianceRepository: PropertyComplianceRepository,
    private val certificateUploadRepository: CertificateUploadRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
    private val updateConfirmationEmailNotificationService: EmailNotificationService<ComplianceUpdateConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    @Transactional
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
        val gasSafetyUpload = gasSafetyCertUploadId?.let { getCertificateFileUpload(it) }
        val eicrUpload = eicrUploadId?.let { getCertificateFileUpload(it) }
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

    fun getNumberOfNonCompliantPropertiesForLandlord(landlordBaseUserId: String) =
        getNonCompliantPropertiesForLandlord(landlordBaseUserId).size

    fun getNonCompliantPropertiesForLandlord(landlordBaseUserId: String): List<ComplianceStatusDataModel> {
        val compliances = propertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId)
        return compliances.map { ComplianceStatusDataModel.fromPropertyCompliance(it) }.filter { it.isNonCompliant }
    }

    @Transactional
    fun updatePropertyCompliance(
        propertyOwnershipId: Long,
        update: PropertyComplianceUpdateModel,
        checkUpdateIsValid: () -> Unit,
    ) {
        checkUpdateIsValid()
        val propertyCompliance = getComplianceForProperty(propertyOwnershipId)

        val didHaveMeesBefore = propertyCompliance.epcMeesExemptionReason != null

        if (update.gasSafetyCertUpdate != null) {
            propertyCompliance.gasSafetyFileUpload =
                update.gasSafetyCertUpdate.fileUploadId?.let { getCertificateFileUpload(it) }
            propertyCompliance.gasSafetyCertIssueDate = update.gasSafetyCertUpdate.issueDate
            propertyCompliance.gasSafetyCertEngineerNum = update.gasSafetyCertUpdate.engineerNum
            propertyCompliance.gasSafetyCertExemptionReason = update.gasSafetyCertUpdate.exemptionReason
            propertyCompliance.gasSafetyCertExemptionOtherReason = update.gasSafetyCertUpdate.exemptionOtherReason
        }

        if (update.eicrUpdate != null) {
            propertyCompliance.eicrFileUpload = update.eicrUpdate.fileUploadId?.let { getCertificateFileUpload(it) }
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
        sendUpdateConfirmationEmail(
            propertyOwnership = propertyCompliance.propertyOwnership,
            update = update,
            didHaveMeesBefore = didHaveMeesBefore,
        )
    }

    private fun sendUpdateConfirmationEmail(
        propertyOwnership: PropertyOwnership,
        update: PropertyComplianceUpdateModel,
        didHaveMeesBefore: Boolean,
    ) {
        val updateType =
            when {
                update.gasSafetyCertUpdate != null -> getGasSafetyUpdateType(update.gasSafetyCertUpdate)
                update.eicrUpdate != null -> getElectricalSafetyUpdateType(update.eicrUpdate)
                update.epcUpdate != null -> getEnergyPerformanceUpdateType(update.epcUpdate, didHaveMeesBefore)
                else -> throw PrsdbWebException("No compliance update type found in update model.")
            }

        updateConfirmationEmailNotificationService.sendEmail(
            recipientAddress = propertyOwnership.primaryLandlord.email,
            email =
                ComplianceUpdateConfirmationEmail(
                    propertyAddress = propertyOwnership.property.address.singleLineAddress,
                    registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber),
                    dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
                    complianceUpdateType = updateType,
                ),
        )
    }

    private fun getEnergyPerformanceUpdateType(
        epcUpdate: EpcUpdateModel,
        didHaveMeesBefore: Boolean,
    ): ComplianceUpdateConfirmationEmail.UpdateType =
        when {
            epcUpdate.tenancyStartedBeforeExpiry == false -> ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_EPC_INFORMATION
            epcUpdate.exemptionReason != null -> ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION
            epcUpdate.epcDataModel == null -> ComplianceUpdateConfirmationEmail.UpdateType.NO_EPC_INFORMATION
            !epcUpdate.epcDataModel.isEnergyRatingEOrBetter() && epcUpdate.meesExemptionReason == null ->
                getLowPerformanceUpdateType(didHaveMeesBefore)
            else -> ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION
        }

    private fun getLowPerformanceUpdateType(didHaveMeesBefore: Boolean): ComplianceUpdateConfirmationEmail.UpdateType =
        if (didHaveMeesBefore) {
            ComplianceUpdateConfirmationEmail.UpdateType.REMOVED_MEES_EPC_INFORMATION
        } else {
            ComplianceUpdateConfirmationEmail.UpdateType.LOW_RATED_EPC_INFORMATION
        }

    private fun getElectricalSafetyUpdateType(eicrUpdate: EicrUpdateModel): ComplianceUpdateConfirmationEmail.UpdateType =
        if (eicrUpdate.issueDate != null && eicrUpdate.fileUploadId == null) {
            ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_ELECTRICAL_INFORMATION
        } else {
            ComplianceUpdateConfirmationEmail.UpdateType.VALID_ELECTRICAL_INFORMATION
        }

    private fun getGasSafetyUpdateType(gasSafetyCertUpdate: GasSafetyCertUpdateModel): ComplianceUpdateConfirmationEmail.UpdateType =
        if (gasSafetyCertUpdate.issueDate != null && gasSafetyCertUpdate.fileUploadId == null) {
            ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_GAS_SAFETY_INFORMATION
        } else {
            ComplianceUpdateConfirmationEmail.UpdateType.VALID_GAS_SAFETY_INFORMATION
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

    fun deletePropertyCompliance(propertyCompliance: PropertyCompliance) {
        propertyComplianceRepository.delete(propertyCompliance)
    }

    fun deletePropertyComplianceByOwnershipId(propertyOwnershipId: Long) =
        propertyComplianceRepository.deleteByPropertyOwnership_Id(propertyOwnershipId)

    fun deletePropertyCompliancesByOwnershipIds(propertyOwnershipIds: List<Long>) {
        propertyComplianceRepository.deleteByPropertyOwnership_IdIn(propertyOwnershipIds)
    }

    // Only allow file uploads that are associated with a certificate upload to be attached to a property compliance record.
    private fun getCertificateFileUpload(id: Long): FileUpload {
        val certificate = certificateUploadRepository.findByFileUpload_Id(id)

        if (certificate == null) {
            throw PrsdbWebException("No certificate upload found for ID: $id")
        }

        return certificate.fileUpload
    }
}
