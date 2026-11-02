package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.CertificateExpiryHelper
import java.time.LocalDate

data class ComplianceStatusDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val registrationNumber: String,
    val gasSafetyStatus: ComplianceCertStatus,
    val electricalSafetyStatus: ComplianceCertStatus,
    val epcStatus: ComplianceCertStatus,
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

    fun shouldShowEpcAction(): Boolean = shouldShowCert(epcStatus)

    val shouldShowOnComplianceActionsPage: Boolean
        get() = certStatuses.any { shouldShowCert(it) }

    val isAllValid: Boolean
        get() = certStatuses.all { it in ComplianceCertStatus.VALID_STATUSES }

    val isAnyProvideLater: Boolean
        get() = certStatuses.any { it == ComplianceCertStatus.PROVIDE_LATER }

    val displayAnyMissingOrFaulty: Boolean
        get() = isOccupied && certStatuses.any { it in ComplianceCertStatus.NEEDS_COMPLIANCE_IF_OCCUPIED_STATUSES }

    val expiredCertificateCount: Int
        get() = certStatuses.count { it == ComplianceCertStatus.EXPIRED }

    private val certStatuses = listOf(gasSafetyStatus, electricalSafetyStatus, epcStatus)

    companion object {
        // We may want to move gas/EPC date access and compliance status derivation into the service layer to follow the same pattern as EIC/EICR.
        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        fun fromPropertyCompliance(
            propertyCompliance: PropertyCompliance,
            electricalSafetyExpiryDate: LocalDate?,
            isGas14MonthExpiryEnabled: Boolean,
        ): ComplianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = propertyCompliance.propertyOwnership.id,
                singleLineAddress = propertyCompliance.propertyOwnership.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(
                            propertyCompliance.propertyOwnership.registrationNumber,
                        ).toString(),
                gasSafetyStatus = gasSafetyStatusOf(propertyCompliance, isGas14MonthExpiryEnabled),
                electricalSafetyStatus = electricalSafetyStatusOf(propertyCompliance, electricalSafetyExpiryDate),
                epcStatus = propertyCompliance.epcStatus,
                isComplete = true,
                isOccupied = propertyCompliance.propertyOwnership.isOccupied,
                provideLaterDeadline = propertyCompliance.propertyOwnership.provideLaterDeadline,
                gasSafetyExpiryDate = propertyCompliance.getGasSafetyCertExpiryDate(isGas14MonthExpiryEnabled),
                electricalSafetyExpiryDate = electricalSafetyExpiryDate,
                epcExpiryDate = propertyCompliance.epcExpiryDate,
                tenancyStartedBeforeEpcExpiry = propertyCompliance.tenancyStartedBeforeEpcExpiry ?: false,
            )

        private fun electricalSafetyStatusOf(
            propertyCompliance: PropertyCompliance,
            expiryDate: LocalDate?,
        ): ComplianceCertStatus =
            when {
                propertyCompliance.electricalSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                expiryDate == null -> ComplianceCertStatus.HAS_FAULTS
                propertyCompliance.isElectricalSafetyExpired(expiryDate) -> ComplianceCertStatus.EXPIRED
                else -> ComplianceCertStatus.ADDED
            }

        private fun PropertyCompliance.isElectricalSafetyExpired(expiryDate: LocalDate): Boolean =
            when (electricalCertType) {
                CertificateType.Eic -> CertificateExpiryHelper.isEicExpiryDateExpired(expiryDate)
                CertificateType.Eicr -> CertificateExpiryHelper.isEicrExpiryDateExpired(expiryDate)
                else -> throw PrsdbWebException("Electrical cert type must be EIC or EICR but was $electricalCertType")
            }

        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        internal fun gasSafetyStatusOf(
            propertyCompliance: PropertyCompliance,
            isGas14MonthExpiryEnabled: Boolean,
        ): ComplianceCertStatus =
            with(propertyCompliance) {
                when {
                    this.hasGasSupply == false -> ComplianceCertStatus.NOT_REQUIRED
                    this.gasSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isGasSafetyCertMissing -> ComplianceCertStatus.HAS_FAULTS
                    isGasSafetyCertExpired(isGas14MonthExpiryEnabled) == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }
            }

        internal fun epcStatusOf(propertyCompliance: PropertyCompliance): ComplianceCertStatus = propertyCompliance.epcStatus

        private val PropertyCompliance.epcStatus: ComplianceCertStatus
            get() =
                when {
                    epcProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    epcHasFaults -> ComplianceCertStatus.HAS_FAULTS
                    isEpcExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }
    }
}
