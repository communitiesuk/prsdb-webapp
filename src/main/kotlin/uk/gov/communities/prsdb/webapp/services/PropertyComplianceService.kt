package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import java.time.Instant
import java.time.LocalDate

@PrsdbWebService
class PropertyComplianceService(
    private val propertyComplianceRepository: PropertyComplianceRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val virusScanCallbackService: VirusScanCallbackService,
) {
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
        //  See the old email templates removed in PDJB-812 for reference
    }

    @Transactional
    fun updateElectricalSafety(
        propertyOwnershipId: Long,
        initialLastModifiedDate: Instant,
        electricalCertType: CertificateType? = null,
        electricalSafetyExpiryDate: LocalDate? = null,
        electricalSafetyCertUploadIds: List<Long> = listOf(),
    ) {
        val propertyCompliance = getComplianceForProperty(propertyOwnershipId)
        throwErrorIfLastModifiedDatesConflict(propertyCompliance, initialLastModifiedDate)

        propertyCompliance.apply {
            populateElectricalSafetyFields(
                record = this,
                electricalSafetyFileUploadIds = electricalSafetyCertUploadIds,
                electricalSafetyExpiryDate = electricalSafetyExpiryDate,
                electricalCertType = electricalCertType,
            )
        }

        propertyComplianceRepository.save(propertyCompliance)

        updateFileUploadVirusScanningCallbacks(
            propertyOwnershipId = propertyOwnershipId,
            gasSafetyCertUploadIds = emptyList(),
            electricalSafetyCertUploadIds = electricalSafetyCertUploadIds,
            electricalCertType = electricalCertType,
        )
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
