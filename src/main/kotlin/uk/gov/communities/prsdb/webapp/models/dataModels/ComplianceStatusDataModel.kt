package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.toJavaLocalDate
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import java.time.LocalDate

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
        // TODO PDJB-639 - add a version of this for the new journey framework (SavedJourneyState) if required
        fun fromIncompleteComplianceForm(propertyOwnership: PropertyOwnership): ComplianceStatusDataModel {
            val incompleteComplianceForm =
                propertyOwnership.incompleteComplianceForm?.toJourneyData()
                    ?: throw IllegalArgumentException(
                        "No incomplete compliance form found for PropertyOwnership with ID ${propertyOwnership.id}",
                    )

            return ComplianceStatusDataModel(
                propertyOwnershipId = propertyOwnership.id,
                singleLineAddress = propertyOwnership.address.singleLineAddress,
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

        private fun JourneyData.getGasSafetyStatus(): ComplianceCertStatus =
            if (hasCompletedStep(GasSafetyExemptionMissingStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.NOT_ADDED
            } else if (getIsGasSafetyCertOutdated() == true || hasCompletedStep(GasSafetyOutdatedStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.EXPIRED
            } else if (
                hasCompletedStep(GasSafetyUploadConfirmationStep.ROUTE_SEGMENT) ||
                hasCompletedStep(GasSafetyExemptionConfirmationStep.ROUTE_SEGMENT)
            ) {
                ComplianceCertStatus.ADDED
            } else {
                ComplianceCertStatus.NOT_STARTED
            }

        private fun JourneyData.getEicrStatus(): ComplianceCertStatus =
            if (hasCompletedStep(EicrExemptionMissingStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.NOT_ADDED
            } else if (getIsEicrOutdated() == true || hasCompletedStep(EicrOutdatedStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.EXPIRED
            } else if (
                hasCompletedStep(EicrUploadConfirmationStep.ROUTE_SEGMENT) ||
                hasCompletedStep(EicrExemptionConfirmationStep.ROUTE_SEGMENT)
            ) {
                ComplianceCertStatus.ADDED
            } else {
                ComplianceCertStatus.NOT_STARTED
            }

        private fun JourneyData.getEpcStatus(): ComplianceCertStatus =
            if (hasCompletedStep(EpcMissingStep.ROUTE_SEGMENT) || hasCompletedStep(EpcNotFoundStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.NOT_ADDED
            } else if (hasCompletedStep(EpcExpiredStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.EXPIRED
            } else if (getHasCompletedEpcAdded() || hasCompletedStep(EpcExemptionConfirmationStep.ROUTE_SEGMENT)) {
                ComplianceCertStatus.ADDED
            } else {
                ComplianceCertStatus.NOT_STARTED
            }

        private fun JourneyData.hasCompletedStep(routeSegment: String): Boolean = JourneyDataHelper.getPageData(this, routeSegment) != null

        private fun JourneyData.getIsGasSafetyCertOutdated(): Boolean? =
            getIssueDate(GasSafetyIssueDateStep.ROUTE_SEGMENT)?.let { issueDate ->
                val expiryCutoff =
                    DateTimeHelper().getCurrentDateInUK().toJavaLocalDate().minusYears(
                        GAS_SAFETY_CERT_VALIDITY_YEARS.toLong(),
                    )
                !issueDate.isAfter(expiryCutoff)
            }

        private fun JourneyData.getIsEicrOutdated(): Boolean? =
            getIssueDate(EicrIssueDateStep.ROUTE_SEGMENT)?.let { issueDate ->
                val expiryCutoff = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate().minusYears(EICR_VALIDITY_YEARS.toLong())
                issueDate.isBefore(expiryCutoff)
            }

        private fun JourneyData.getIssueDate(routeSegment: String): LocalDate? {
            val day = JourneyDataHelper.getFieldStringValue(this, routeSegment, TodayOrPastDateFormModel::day.name)?.toIntOrNull()
            val month = JourneyDataHelper.getFieldStringValue(this, routeSegment, TodayOrPastDateFormModel::month.name)?.toIntOrNull()
            val year = JourneyDataHelper.getFieldStringValue(this, routeSegment, TodayOrPastDateFormModel::year.name)?.toIntOrNull()
            if (day == null || month == null || year == null) return null

            return runCatching { LocalDate.of(year, month, day) }.getOrNull()
        }

        private fun JourneyData.getHasCompletedEpcAdded(): Boolean {
            if (hasCompletedStep(LowEnergyRatingStep.ROUTE_SEGMENT) || hasCompletedStep(MeesExemptionConfirmationStep.ROUTE_SEGMENT)) {
                return true
            }

            val acceptedEpc = getAcceptedEpc() ?: return false
            if (!acceptedEpc.isEnergyRatingEOrBetter()) return false
            if (!acceptedEpc.isPastExpiryDate()) return true

            return JourneyDataHelper.getFieldBooleanValue(
                this,
                EpcExpiryCheckStep.ROUTE_SEGMENT,
                EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name,
            ) == true
        }

        private fun JourneyData.getAcceptedEpc(): EpcDataModel? {
            val acceptedAutoMatched =
                JourneyDataHelper.getFieldBooleanValue(
                    this,
                    CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT,
                    CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
                ) == true
            if (acceptedAutoMatched) {
                return getEpcData(NonStepJourneyDataKey.AutoMatchedEpc.key)
            }

            val acceptedSearched =
                JourneyDataHelper.getFieldBooleanValue(
                    this,
                    CheckMatchedEpcStep.ROUTE_SEGMENT,
                    CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
                ) == true
            if (acceptedSearched) {
                return getEpcData(NonStepJourneyDataKey.LookedUpEpc.key)
            }

            return null
        }

        private fun JourneyData.getEpcData(key: String): EpcDataModel? {
            val serializedEpc = this[key]?.toString() ?: return null
            return runCatching { Json.decodeFromString<EpcDataModel?>(serializedEpc) }.getOrNull()
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
