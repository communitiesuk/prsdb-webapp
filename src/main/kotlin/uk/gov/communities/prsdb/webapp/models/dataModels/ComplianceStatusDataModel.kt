package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

data class ComplianceStatusDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val registrationNumber: String,
    val gasSafetyStatus: ComplianceCertStatus,
    val electricalSafetyStatus: ComplianceCertStatus,
    val epcStatus: ComplianceCertStatus,
    val isComplete: Boolean,
    val isOccupied: Boolean,
) {
    fun shouldShowCert(status: ComplianceCertStatus): Boolean =
        status == ComplianceCertStatus.EXPIRED ||
            (isOccupied && status !in ComplianceCertStatus.VALID_STATUSES)

    val shouldShowOnComplianceActionsPage: Boolean
        get() = certStatuses.any { shouldShowCert(it) }

    val isAllValid: Boolean
        get() = certStatuses.all { it in ComplianceCertStatus.VALID_STATUSES }

    val displayAnyMissing: Boolean
        get() = isOccupied && certStatuses.any { it in ComplianceCertStatus.MISSING_STATUSES }

    val expiredCertificateCount: Int
        get() = certStatuses.count { it == ComplianceCertStatus.EXPIRED }

    private val certStatuses = listOf(gasSafetyStatus, electricalSafetyStatus, epcStatus)

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
                epcStatus = propertyCompliance.epcStatus,
                isComplete = true,
                isOccupied = propertyCompliance.propertyOwnership.isOccupied,
            )

        private val PropertyCompliance.gasSafetyStatus: ComplianceCertStatus
            get() =
                when {
                    this.hasGasSupply == false -> ComplianceCertStatus.NOT_REQUIRED
                    this.gasSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isGasSafetyCertMissing -> ComplianceCertStatus.NOT_ADDED
                    this.isGasSafetyCertExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.electricalSafetyStatus: ComplianceCertStatus
            get() =
                when {
                    this.electricalSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isElectricalSafetyMissing -> ComplianceCertStatus.NOT_ADDED
                    this.isElectricalSafetyExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.epcStatus: ComplianceCertStatus
            get() =
                when {
                    this.isEpcNonCompliantDueToExpiry == true -> ComplianceCertStatus.EXPIRED
                    this.epcProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isEpcMissing -> ComplianceCertStatus.NOT_ADDED
                    else -> ComplianceCertStatus.ADDED
                }
    }
}
