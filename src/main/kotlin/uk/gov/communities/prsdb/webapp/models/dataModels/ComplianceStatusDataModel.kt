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
    val epcStatus: ComplianceCertStatus,
    val isComplete: Boolean,
    val isOccupied: Boolean,
    val provideLaterDeadline: LocalDate? = null,
) {
    fun shouldShowCert(status: ComplianceCertStatus): Boolean =
        status == ComplianceCertStatus.EXPIRED ||
            (isOccupied && !listOf(ComplianceCertStatus.ADDED, ComplianceCertStatus.NOT_REQUIRED).contains(status))

    val shouldShowOnComplianceActionsPage: Boolean
        get() = certStatuses.any { shouldShowCert(it) }

    private val certStatuses = listOf(gasSafetyStatus, eicrStatus, epcStatus)

    companion object {
        // TODO PDJB-928 - Update this to use real state instead of NOT_STARTED
        fun fromPropertyOwnershipWithoutCompliance(propertyOwnership: PropertyOwnership): ComplianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = propertyOwnership.id,
                singleLineAddress = propertyOwnership.address.singleLineAddress,
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                gasSafetyStatus = ComplianceCertStatus.NOT_STARTED,
                eicrStatus = ComplianceCertStatus.NOT_STARTED,
                epcStatus = ComplianceCertStatus.NOT_STARTED,
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
                epcStatus = propertyCompliance.epcStatus,
                isComplete = true,
                isOccupied = propertyCompliance.propertyOwnership.isOccupied,
                provideLaterDeadline =
                    propertyCompliance.propertyOwnership.lastOccupiedDate?.plusDays(
                        PROVIDE_LATER_DEADLINE_DAYS.toLong(),
                    ),
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
