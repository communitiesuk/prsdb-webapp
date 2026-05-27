package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import java.time.LocalDate

data class ComplianceStatusDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val registrationNumber: String,
    val gasSafetyStatus: ComplianceCertStatus,
    val eicrStatus: ComplianceCertStatus,
    val epcStatusOld: ComplianceCertStatus,
    val epcStatusMay2026Redesign: ComplianceCertStatus,
    val isComplete: Boolean,
    val isOccupied: Boolean,
    val provideLaterDeadline: LocalDate? = null,
    val gasSafetyExpiryDate: LocalDate? = null,
    val eicrExpiryDate: LocalDate? = null,
    val epcExpiryDate: LocalDate? = null,
) {
    fun shouldShowCert(status: ComplianceCertStatus): Boolean =
        status == ComplianceCertStatus.EXPIRED ||
            (isOccupied && !listOf(ComplianceCertStatus.ADDED, ComplianceCertStatus.NOT_REQUIRED).contains(status))

    val shouldShowOnOldComplianceActionsPage: Boolean
        get() = certStatusesOld.any { shouldShowCert(it) }
    val shouldShowOnMay2026RedesignComplianceActionsPage: Boolean
        get() = certStatusesMay2026Redesign.any { shouldShowCert(it) }

    private val certStatusesOld = listOf(gasSafetyStatus, eicrStatus, epcStatusOld)
    private val certStatusesMay2026Redesign = listOf(gasSafetyStatus, eicrStatus, epcStatusMay2026Redesign)

    companion object {
        // TODO PDJB-928 - Update this to use real state instead of NOT_STARTED
        fun fromPropertyOwnershipWithoutCompliance(propertyOwnership: PropertyOwnership): ComplianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = propertyOwnership.id,
                singleLineAddress = propertyOwnership.address.singleLineAddress,
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                gasSafetyStatus = ComplianceCertStatus.NOT_STARTED,
                eicrStatus = ComplianceCertStatus.NOT_STARTED,
                epcStatusOld = ComplianceCertStatus.NOT_STARTED,
                epcStatusMay2026Redesign = ComplianceCertStatus.NOT_STARTED,
                isComplete = false,
                isOccupied = propertyOwnership.isOccupied,
            )

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
                eicrStatus = propertyCompliance.eicrStatus,
                epcStatusOld = propertyCompliance.epcStatusOld,
                epcStatusMay2026Redesign = propertyCompliance.epcStatusMay2026Redesign,
                isComplete = true,
                isOccupied = propertyCompliance.propertyOwnership.isOccupied,
                provideLaterDeadline =
                    propertyCompliance.propertyOwnership.lastOccupiedDate?.plusDays(
                        PROVIDE_LATER_DEADLINE_DAYS.toLong(),
                    ),
                gasSafetyExpiryDate = propertyCompliance.gasSafetyCertExpiryDate,
                eicrExpiryDate = propertyCompliance.electricalSafetyExpiryDate,
                epcExpiryDate = propertyCompliance.epcExpiryDate,
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

        private val PropertyCompliance.eicrStatus: ComplianceCertStatus
            get() =
                when {
                    this.electricalSafetyCertProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    this.isElectricalSafetyMissing -> ComplianceCertStatus.NOT_ADDED
                    this.isElectricalSafetyExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.epcStatusOld: ComplianceCertStatus
            get() =
                when {
                    isEpcNonCompliantDueToExpiry -> ComplianceCertStatus.EXPIRED
                    epcProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    isEpcMissing -> ComplianceCertStatus.NOT_ADDED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.epcStatusMay2026Redesign: ComplianceCertStatus
            get() =
                when {
                    epcProvideLater == true -> ComplianceCertStatus.PROVIDE_LATER
                    isEpcExpiredAndWouldBeCompliant -> ComplianceCertStatus.EXPIRED
                    isEpcMissing -> ComplianceCertStatus.NOT_ADDED
                    else -> ComplianceCertStatus.ADDED
                }
    }
}
