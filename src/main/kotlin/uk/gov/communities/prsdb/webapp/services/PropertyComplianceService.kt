package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.EICR_SAFETY_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback.Companion.extractFileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EicrUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EpcUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ComplianceUpdateConfirmationEmail
import java.time.Instant
import java.time.LocalDate
import kotlin.String

@PrsdbWebService
class PropertyComplianceService(
    private val propertyComplianceRepository: PropertyComplianceRepository,
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val session: HttpSession,
    private val updateConfirmationEmailNotificationService: EmailNotificationService<ComplianceUpdateConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val fileUploadRepository: FileUploadRepository,
    private val virusScanCallbackService: VirusScanCallbackService,
) {
    // TODO PDJB-812 remove
    @Transactional
    fun createPropertyCompliance(
        propertyOwnershipId: Long,
        gasSafetyCertUploadId: Long? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertEngineerNum: String? = null,
        gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyCertExemptionOtherReason: String? = null,
        eicrUploadId: Long? = null,
        // TODO PDJB-766: Remove eicrIssueDate once the compliance update journey uses expiry date instead
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
                // TODO PDJB-766: Remove eicrIssueDate and this derived calculation once the compliance update journey uses expiry date
                electricalSafetyExpiryDate = eicrIssueDate?.plusYears(EICR_SAFETY_VALIDITY_YEARS.toLong()),
                eicrExemptionReason = eicrExemptionReason,
                eicrExemptionOtherReason = eicrExemptionOtherReason,
                epcUrl = epcUrl,
                epcExpiryDate = epcExpiryDate,
                tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
                epcEnergyRating = epcEnergyRating,
                epcExemptionReason = epcExemptionReason,
                epcMeesExemptionReason = epcMeesExemptionReason,
            ).also {
                it.hasGasSupply = gasSafetyCertExemptionReason != GasSafetyExemptionReason.NO_GAS_SUPPLY
            },
        )
    }

    fun getComplianceForPropertyOrNull(propertyOwnershipId: Long): PropertyCompliance? =
        propertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId)

    @Transactional
    fun saveRegistrationComplianceData(
        registrationNumberValue: Long,
        hasGasSupply: Boolean? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyFileUploadIds: List<Long> = listOf(),
        electricalSafetyFileUploadIds: List<Long> = listOf(),
        electricalSafetyExpiryDate: LocalDate? = null,
        electricalCertType: CertificateType? = null,
        epcCertificateUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        epcEnergyRating: String? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
    ) {
        val propertyOwnership =
            propertyOwnershipRepository.findByRegistrationNumber_Number(registrationNumberValue)
                ?: throw EntityNotFoundException("Property ownership not found for registration number $registrationNumberValue")

        val record =
            PropertyCompliance(propertyOwnership = propertyOwnership).apply {
                populateGasSafetyFields(
                    record = this,
                    hasGasSupply = hasGasSupply,
                    gasSafetyCertIssueDate = gasSafetyCertIssueDate,
                    gasSafetyFileUploadIds = gasSafetyFileUploadIds,
                )
                populateElectricalSafetyFields(
                    record = this,
                    electricalSafetyFileUploadIds = electricalSafetyFileUploadIds,
                    electricalSafetyExpiryDate = electricalSafetyExpiryDate,
                    electricalCertType = electricalCertType,
                )
                populateEpcFields(
                    record = this,
                    epcCertificateUrl = epcCertificateUrl,
                    epcExpiryDate = epcExpiryDate,
                    epcEnergyRating = epcEnergyRating,
                    tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
                    epcExemptionReason = epcExemptionReason,
                    epcMeesExemptionReason = epcMeesExemptionReason,
                )
            }

        propertyComplianceRepository.save(record)

        updateFileUploadVirusScanningCallbacks(
            propertyOwnershipId = propertyOwnership.id,
            gasSafetyCertUploadIds = gasSafetyFileUploadIds,
            electricalSafetyCertUploadIds = electricalSafetyFileUploadIds,
            electricalCertType = electricalCertType,
        )
    }

    private fun populateGasSafetyFields(
        record: PropertyCompliance,
        hasGasSupply: Boolean?,
        gasSafetyCertIssueDate: LocalDate?,
        gasSafetyFileUploadIds: List<Long>,
    ) {
        record.gasSafetyCertExemptionReason = if (hasGasSupply == false) GasSafetyExemptionReason.NO_GAS_SUPPLY else null
        record.hasGasSupply = hasGasSupply
        record.gasSafetyCertIssueDate = gasSafetyCertIssueDate
        record.gasSafetyFileUploads =
            gasSafetyFileUploadIds
                .map { id -> fileUploadRepository.getReferenceById(id) }
                .toMutableList()
    }

    private fun populateElectricalSafetyFields(
        record: PropertyCompliance,
        electricalSafetyFileUploadIds: List<Long>,
        electricalSafetyExpiryDate: LocalDate?,
        electricalCertType: CertificateType?,
    ) {
        record.electricalSafetyFileUploads =
            electricalSafetyFileUploadIds
                .map { id -> fileUploadRepository.getReferenceById(id) }
                .toMutableList()
        record.electricalSafetyExpiryDate = electricalSafetyExpiryDate
        record.electricalCertType = electricalCertType
    }

    private fun populateEpcFields(
        record: PropertyCompliance,
        epcCertificateUrl: String?,
        epcExpiryDate: LocalDate?,
        epcEnergyRating: String?,
        tenancyStartedBeforeEpcExpiry: Boolean?,
        epcExemptionReason: EpcExemptionReason?,
        epcMeesExemptionReason: MeesExemptionReason?,
    ) {
        record.epcUrl = epcCertificateUrl
        record.epcExpiryDate = epcExpiryDate
        record.epcEnergyRating = epcEnergyRating
        record.tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry
        record.epcExemptionReason = epcExemptionReason
        record.epcMeesExemptionReason = epcMeesExemptionReason
    }

    private fun updateFileUploadVirusScanningCallbacks(
        propertyOwnershipId: Long,
        gasSafetyCertUploadIds: List<Long> = emptyList(),
        electricalSafetyCertUploadIds: List<Long> = emptyList(),
        electricalCertType: CertificateType? = null,
    ) {
        gasSafetyCertUploadIds.forEach {
            virusScanCallbackService.deleteAllCallbacksForFileUpload(it)
            virusScanCallbackService.saveEmailToMonitoringTeam(propertyOwnershipId, it, CertificateType.GasSafetyCert)
            virusScanCallbackService.saveEmailToOwner(propertyOwnershipId, it, CertificateType.GasSafetyCert)
        }

        if (electricalSafetyCertUploadIds.isNotEmpty()) {
            requireNotNull(electricalCertType) { "electricalCertType must not be null when electrical safety uploads are present" }
        }

        electricalSafetyCertUploadIds.forEach {
            virusScanCallbackService.deleteAllCallbacksForFileUpload(it)
            virusScanCallbackService.saveEmailToMonitoringTeam(propertyOwnershipId, it, electricalCertType!!)
            virusScanCallbackService.saveEmailToOwner(propertyOwnershipId, it, electricalCertType)
        }
    }

    fun getComplianceForProperty(propertyOwnershipId: Long): PropertyCompliance =
        getComplianceForPropertyOrNull(propertyOwnershipId)
            ?: throw EntityNotFoundException("No compliance record found for property ownership ID: $propertyOwnershipId")

    fun getNumberOfNonCompliantPropertiesForLandlord(landlordBaseUserId: String) =
        getNonCompliantPropertiesForLandlord(landlordBaseUserId).size

    fun getNonCompliantPropertiesForLandlord(landlordBaseUserId: String): List<ComplianceStatusDataModel> {
        val compliances = propertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId)
        return compliances.map { ComplianceStatusDataModel.fromPropertyCompliance(it) }.filter { it.shouldShowOnComplianceActionsPage }
    }

    // TODO: PDJB-812 remove this
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
            propertyCompliance.hasGasSupply = update.gasSafetyCertUpdate.exemptionReason != GasSafetyExemptionReason.NO_GAS_SUPPLY
        }

        if (update.eicrUpdate != null) {
            propertyCompliance.eicrFileUpload = update.eicrUpdate.fileUploadId?.let { getCertificateFileUpload(it) }
            propertyCompliance.eicrIssueDate = update.eicrUpdate.issueDate
            // TODO PDJB-766: Remove eicrIssueDate and this derived calculation once the compliance update journey uses expiry date
            propertyCompliance.electricalSafetyExpiryDate =
                update.eicrUpdate.issueDate?.plusYears(EICR_SAFETY_VALIDITY_YEARS.toLong())
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
                    propertyAddress = propertyOwnership.address.singleLineAddress,
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
            epcUpdate.tenancyStartedBeforeExpiry == false -> {
                ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_EPC_INFORMATION
            }

            epcUpdate.exemptionReason != null -> {
                ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION
            }

            epcUpdate.epcDataModel == null -> {
                ComplianceUpdateConfirmationEmail.UpdateType.NO_EPC_INFORMATION
            }

            !epcUpdate.epcDataModel.isEnergyRatingEOrBetter() && epcUpdate.meesExemptionReason == null -> {
                getLowPerformanceUpdateType(didHaveMeesBefore)
            }

            else -> {
                ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION
            }
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

    // Only allow file uploads that are associated with a certificate upload to be attached to a property compliance record.
    private fun getCertificateFileUpload(id: Long): FileUpload {
        val callbacks = virusScanCallbackRepository.findAllByFileUpload_Id(id)

        if (callbacks.isEmpty()) {
            throw PrsdbWebException("No virus callbacks found for ID: $id")
        }

        return callbacks.extractFileUpload()
    }

    @Transactional
    fun updateGasSafety(
        propertyOwnershipId: Long,
        initialLastModifiedDate: Instant,
        hasGasSupply: Boolean,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertUploadIds: List<Long> = listOf(),
    ) {
        val propertyCompliance = getComplianceForProperty(propertyOwnershipId)
        throwErrorIfLastModifiedDatesConflict(propertyCompliance, initialLastModifiedDate)

        propertyCompliance.apply {
            populateGasSafetyFields(
                record = this,
                hasGasSupply = hasGasSupply,
                gasSafetyCertIssueDate = gasSafetyCertIssueDate,
                gasSafetyFileUploadIds = gasSafetyCertUploadIds,
            )
        }

        propertyComplianceRepository.save(propertyCompliance)

        updateFileUploadVirusScanningCallbacks(
            propertyOwnershipId = propertyOwnershipId,
            gasSafetyCertUploadIds = gasSafetyCertUploadIds,
        )

        // TODO PDJB-770 - send update confirmation email to landlord if a certificate has been uploaded
    }

    private fun throwErrorIfLastModifiedDatesConflict(
        propertyCompliance: PropertyCompliance,
        initialLastModifiedDate: Instant,
    ) {
        if (propertyCompliance.getMostRecentlyUpdated() != initialLastModifiedDate) {
            throw UpdateConflictException(
                "The property compliance record has been updated since this update session started.",
            )
        }
    }
}
