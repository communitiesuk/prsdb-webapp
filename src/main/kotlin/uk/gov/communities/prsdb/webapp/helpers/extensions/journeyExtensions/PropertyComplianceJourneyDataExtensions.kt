package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.yearsUntil
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel

class PropertyComplianceJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getGasSafetyCertIssueDate() = this.getFieldSetLocalDateValue(GasSafetyIssueDateStep.ROUTE_SEGMENT)

        fun JourneyData.getIsGasSafetyCertOutdated(): Boolean? {
            val issueDate = this.getGasSafetyCertIssueDate() ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= GAS_SAFETY_CERT_VALIDITY_YEARS
        }

        fun JourneyData.getEicrIssueDate() = this.getFieldSetLocalDateValue(EicrIssueDateStep.ROUTE_SEGMENT)

        fun JourneyData.getIsEicrOutdated(): Boolean? {
            val issueDate = this.getEicrIssueDate() ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= EICR_VALIDITY_YEARS
        }

        fun JourneyData.getEpcDetails(autoMatched: Boolean): EpcDataModel? {
            val journeyDataKey = getEpcDetailsJourneyDataKey(autoMatched)
            val serializedEpcDetails = JourneyDataHelper.getStringValueByKey(this, journeyDataKey) ?: return null
            return Json.decodeFromString<EpcDataModel>(serializedEpcDetails)
        }

        private fun getEpcDetailsJourneyDataKey(autoMatched: Boolean): String =
            if (autoMatched) {
                NonStepJourneyDataKey.AutoMatchedEpc.key
            } else {
                NonStepJourneyDataKey.LookedUpEpc.key
            }

        fun JourneyData.getAcceptedEpcDetails(
            automatchedStepId: String = CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT,
            matchedStepId: String = CheckMatchedEpcStep.ROUTE_SEGMENT,
        ): EpcDataModel? {
            // Check the automatched EPC first, then the looked up EPC
            if (this.getAutoMatchedEpcIsCorrect(automatchedStepId) == true) {
                return this.getEpcDetails(autoMatched = true)
            }
            if (this.getMatchedEpcIsCorrect(matchedStepId) == true) {
                return this.getEpcDetails(autoMatched = false)
            }
            return null
        }

        fun JourneyData.getAutoMatchedEpcIsCorrect(stepId: String = CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.getMatchedEpcIsCorrect(stepId: String = CheckMatchedEpcStep.ROUTE_SEGMENT): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.getDidTenancyStartBeforeEpcExpiry(stepRouteSegment: String = EpcExpiryCheckStep.ROUTE_SEGMENT): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepRouteSegment,
                EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name,
            )

        fun JourneyData.getHasCompletedGasSafetyUploadConfirmation() = this.containsKey(GasSafetyUploadConfirmationStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedGasSafetyExemptionConfirmation() = this.containsKey(GasSafetyExemptionConfirmationStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedGasSafetyExemptionMissing() = this.containsKey(GasSafetyExemptionMissingStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEicrUploadConfirmation() = this.containsKey(EicrUploadConfirmationStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEicrExemptionConfirmation() = this.containsKey(EicrExemptionConfirmationStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEicrExemptionMissing() = this.containsKey(EicrExemptionMissingStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEpcExemptionConfirmation() = this.containsKey(EpcExemptionConfirmationStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEpcMissing() = this.containsKey(EpcMissingStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEpcNotFound() = this.containsKey(EpcNotFoundStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEpcExpired() = this.containsKey(EpcExpiredStep.ROUTE_SEGMENT)

        fun JourneyData.getHasCompletedEpcAdded() =
            this.getHasAddedInDateAndHighRatedEpc() ||
                this.getHasCompletedMeesExemptionConfirmation() ||
                this.getHasCompletedEpcLowEnergyRating()

        private fun JourneyData.getHasAddedInDateAndHighRatedEpc(): Boolean {
            val acceptedEpcDetails = this.getAcceptedEpcDetails() ?: return false
            val isEpcInDate = !acceptedEpcDetails.isPastExpiryDate() || this.getDidTenancyStartBeforeEpcExpiry() == true
            return isEpcInDate && acceptedEpcDetails.isEnergyRatingEOrBetter()
        }

        private fun JourneyData.getHasCompletedMeesExemptionConfirmation() = this.containsKey(MeesExemptionConfirmationStep.ROUTE_SEGMENT)

        private fun JourneyData.getHasCompletedEpcLowEnergyRating() = this.containsKey(LowEnergyRatingStep.ROUTE_SEGMENT)
    }
}
