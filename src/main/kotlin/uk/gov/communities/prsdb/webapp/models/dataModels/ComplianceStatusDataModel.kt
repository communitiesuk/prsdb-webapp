package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

data class ComplianceStatusDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val registrationNumber: String,
    val gasSafetyStatus: ComplianceCertStatus,
    val eicrStatus: ComplianceCertStatus,
    val epcStatus: ComplianceCertStatus,
    val isComplete: Boolean,
) {
    val isInProgress: Boolean
        get() = !isComplete && certStatuses.any { it != ComplianceCertStatus.NOT_STARTED }

    val isNonCompliant: Boolean
        get() = certStatuses.any { it != ComplianceCertStatus.ADDED }

    private val certStatuses = listOf(gasSafetyStatus, eicrStatus, epcStatus)

    companion object {
        // TODO PDJB-80 - Update this to use real state instead of NOT_STARTED
        fun fromPropertyOwnershipWithoutCompliance(propertyOwnership: PropertyOwnership): ComplianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = propertyOwnership.id,
                singleLineAddress = propertyOwnership.address.singleLineAddress,
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                gasSafetyStatus = ComplianceCertStatus.NOT_STARTED,
                eicrStatus = ComplianceCertStatus.NOT_STARTED,
                epcStatus = ComplianceCertStatus.NOT_STARTED,
                isComplete = false,
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
            )

        private val PropertyCompliance.gasSafetyStatus: ComplianceCertStatus
            get() =
                when {
                    this.isGasSafetyCertMissing -> ComplianceCertStatus.NOT_ADDED
                    this.isGasSafetyCertExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.eicrStatus: ComplianceCertStatus
            get() =
                when {
                    this.isEicrMissing -> ComplianceCertStatus.NOT_ADDED
                    this.isEicrExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }

        private val PropertyCompliance.epcStatus: ComplianceCertStatus
            get() =
                when {
                    this.isEpcMissing -> ComplianceCertStatus.NOT_ADDED
                    this.isEpcExpired == true -> ComplianceCertStatus.EXPIRED
                    else -> ComplianceCertStatus.ADDED
                }
    }
}
