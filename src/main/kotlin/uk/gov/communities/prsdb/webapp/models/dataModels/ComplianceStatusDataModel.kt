package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import java.time.LocalDate

data class ComplianceStatusDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val registrationNumber: String,
    val gasSafetyStatus: ComplianceCertStatus,
    val electricalSafetyStatus: ComplianceCertStatus,
    val epcStatusOld: ComplianceCertStatus,
    val epcStatusMay2026Redesign: ComplianceCertStatus,
    val isComplete: Boolean,
    val isOccupied: Boolean,
    val provideLaterDeadline: LocalDate? = null,
    val gasSafetyExpiryDate: LocalDate? = null,
    val electricalSafetyExpiryDate: LocalDate? = null,
    val epcExpiryDate: LocalDate? = null,
    val tenancyStartedBeforeEpcExpiry: Boolean = false,
) {
    fun shouldShowCert(status: ComplianceCertStatus): Boolean =
        status == ComplianceCertStatus.EXPIRED ||
            (isOccupied && status in ComplianceCertStatus.NEEDS_COMPLIANCE_IF_OCCUPIED_STATUSES)

    fun shouldShowGasSafetyAction(): Boolean = shouldShowCert(gasSafetyStatus)

    fun shouldShowElectricalSafetyAction(): Boolean = shouldShowCert(electricalSafetyStatus)

    fun shouldShowEpcAction(): Boolean = shouldShowCert(epcStatusMay2026Redesign)

    val shouldShowOnOldComplianceActionsPage: Boolean
        get() = certStatusesOld.any { shouldShowCert(it) }
    val shouldShowOnMay2026RedesignComplianceActionsPage: Boolean
        get() = certStatusesMay26Redesign.any { shouldShowCert(it) }

    val isAllValid: Boolean
        get() = certStatusesMay26Redesign.all { it in ComplianceCertStatus.VALID_STATUSES }

    val displayAnyMissingOrFaulty: Boolean
        get() = isOccupied && certStatusesMay26Redesign.any { it in ComplianceCertStatus.NEEDS_COMPLIANCE_IF_OCCUPIED_STATUSES }

    val expiredCertificateCount: Int
        get() = certStatusesMay26Redesign.count { it == ComplianceCertStatus.EXPIRED }

    private val certStatusesOld = listOf(gasSafetyStatus, electricalSafetyStatus, epcStatusOld)

    private val certStatusesMay26Redesign = listOf(gasSafetyStatus, electricalSafetyStatus, epcStatusMay2026Redesign)

    companion object {
        fun fromPropertyCompliance(propertyCompliance: PropertyCompliance): ComplianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = propertyCompliance.propertyOwnership.id,
                singleLineAddress = propertyCompliance.propertyOwnership.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(
                            propertyCompliance.propertyOwnership.registrationNumber,
                        ).toString(),
                gasSafetyStatus = propertyCompliance.gasSafetyStatus,
                electricalSafetyStatus = propertyCompliance.electricalSafetyStatus,
                epcStatusOld = propertyCompliance.epcStatusOld,
                epcStatusMay2026Redesign = propertyCompliance.epcStatusMay2026Redesign,
                isComplete = true,
                isOccupied = propertyCompliance.propertyOwnership.isOccupied,
                provideLaterDeadline =
                    propertyCompliance.propertyOwnership.lastOccupiedDate?.plusDays(
                        PROVIDE_LATER_DEADLINE_DAYS.toLong(),
                    ),
                gasSafetyExpiryDate = propertyCompliance.gasSafetyCertExpiryDate,
                electricalSafetyExpiryDate = propertyCompliance.electricalSafetyExpiryDate,
                epcExpiryDate = propertyCompliance.epcExpiryDate,
                tenancyStartedBeforeEpcExpiry = propertyCompliance.tenancyStartedBeforeEpcExpiry ?: false,
            )

        private val PropertyCompliance.gasSafetyStatus: ComplianceCertStatus
            get() =
                when {
                    this.hasGasSupply == false -> ComplianceCertStatus.NOT_REQUIRED
                    this.gasSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isGasSafetyCertMissing -> ComplianceCertStatus.HAS_FAULTS
                    this.isGasSafetyCertExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.electricalSafetyStatus: ComplianceCertStatus
            get() =
                when {
                    this.electricalSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isElectricalSafetyMissing -> ComplianceCertStatus.HAS_FAULTS
                    this.isElectricalSafetyExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.epcStatusOld: ComplianceCertStatus
            get() =
                when {
                    isEpcNonCompliantDueToExpiry -> ComplianceCertStatus.EXPIRED
                    epcProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    epcHasFaults -> ComplianceCertStatus.HAS_FAULTS
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.epcStatusMay2026Redesign: ComplianceCertStatus
            get() =
                when {
                    epcProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    epcHasFaults -> ComplianceCertStatus.HAS_FAULTS
                    isEpcExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }
    }
}
