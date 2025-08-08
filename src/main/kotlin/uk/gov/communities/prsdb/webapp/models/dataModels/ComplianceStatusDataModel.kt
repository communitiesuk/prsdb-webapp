package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcAdded
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExpired
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcNotFound
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated

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
        fun fromIncompleteComplianceForm(propertyOwnership: PropertyOwnership): ComplianceStatusDataModel {
            val incompleteComplianceForm =
                propertyOwnership.incompleteComplianceForm?.toJourneyData()
                    ?: throw IllegalArgumentException(
                        "No incomplete compliance form found for PropertyOwnership with ID ${propertyOwnership.id}",
                    )

            return ComplianceStatusDataModel(
                propertyOwnershipId = propertyOwnership.id,
                singleLineAddress = propertyOwnership.property.address.singleLineAddress,
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                gasSafetyStatus = incompleteComplianceForm.getGasSafetyStatus(),
                eicrStatus = incompleteComplianceForm.getEicrStatus(),
                epcStatus = incompleteComplianceForm.getEpcStatus(),
                isComplete = false,
            )
        }

        fun fromPropertyCompliance(propertyCompliance: PropertyCompliance): ComplianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = propertyCompliance.propertyOwnership.id,
                singleLineAddress = propertyCompliance.propertyOwnership.property.address.singleLineAddress,
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

        private fun JourneyData.getGasSafetyStatus(): ComplianceCertStatus =
            if (this.getHasCompletedGasSafetyExemptionMissing()) {
                ComplianceCertStatus.NOT_ADDED
            } else if (this.getIsGasSafetyCertOutdated() == true) {
                ComplianceCertStatus.EXPIRED
            } else if (this.getHasCompletedGasSafetyUploadConfirmation() || this.getHasCompletedGasSafetyExemptionConfirmation()) {
                ComplianceCertStatus.ADDED
            } else {
                ComplianceCertStatus.NOT_STARTED
            }

        private fun JourneyData.getEicrStatus(): ComplianceCertStatus =
            if (this.getHasCompletedEicrExemptionMissing()) {
                ComplianceCertStatus.NOT_ADDED
            } else if (this.getIsEicrOutdated() == true) {
                ComplianceCertStatus.EXPIRED
            } else if (this.getHasCompletedEicrUploadConfirmation() || this.getHasCompletedEicrExemptionConfirmation()) {
                ComplianceCertStatus.ADDED
            } else {
                ComplianceCertStatus.NOT_STARTED
            }

        private fun JourneyData.getEpcStatus(): ComplianceCertStatus =
            if (this.getHasCompletedEpcMissing() || this.getHasCompletedEpcNotFound()) {
                ComplianceCertStatus.NOT_ADDED
            } else if (this.getHasCompletedEpcExpired()) {
                ComplianceCertStatus.EXPIRED
            } else if (this.getHasCompletedEpcAdded() || this.getHasCompletedEpcExemptionConfirmation()) {
                ComplianceCertStatus.ADDED
            } else {
                ComplianceCertStatus.NOT_STARTED
            }

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
