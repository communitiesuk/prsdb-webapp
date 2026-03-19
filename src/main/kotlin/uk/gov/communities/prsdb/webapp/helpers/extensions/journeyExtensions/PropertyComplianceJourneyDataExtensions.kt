package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.yearsUntil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionReasonStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateGasSafetyCertificateFormModel

class PropertyComplianceJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getHasGasSafetyCert() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.GasSafety.urlPathSegment,
                GasSafetyFormModel::hasCert.name,
            )

        fun JourneyData.getHasNewGasSafetyCertificate() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateGasSafety.urlPathSegment,
                UpdateGasSafetyCertificateFormModel::hasNewCertificate.name,
            )

        const val ORIGINALLY_NOT_INCLUDED_KEY = "originallyNotIncluded"

        fun JourneyData.getGasSafetyCertIssueDate() = this.getFieldSetLocalDateValue(GasSafetyIssueDateStep.ROUTE_SEGMENT)

        fun JourneyData.getIsGasSafetyCertOutdated(): Boolean? {
            val issueDate = this.getGasSafetyCertIssueDate() ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= GAS_SAFETY_CERT_VALIDITY_YEARS
        }

        fun JourneyData.getGasSafetyCertEngineerNum() =
            JourneyDataHelper.getFieldStringValue(
                this,
                PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment,
                GasSafeEngineerNumFormModel::engineerNumber.name,
            )

        fun JourneyData.getGasSafetyCertUploadId() =
            JourneyDataHelper.getFieldStringValue(
                this,
                GasSafetyCertificateUploadStep.ROUTE_SEGMENT,
                GasSafetyUploadCertificateFormModel::fileUploadId.name,
            )

        fun JourneyData.getHasGasSafetyCertExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                GasSafetyExemptionStep.ROUTE_SEGMENT,
                GasSafetyExemptionFormModel::hasExemption.name,
            )

        fun JourneyData.getGasSafetyCertExemptionReason() =
            JourneyDataHelper.getFieldEnumValue<GasSafetyExemptionReason>(
                this,
                GasSafetyExemptionReasonStep.ROUTE_SEGMENT,
                GasSafetyExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getIsGasSafetyExemptionReasonOther() =
            this.getGasSafetyCertExemptionReason()?.let { it == GasSafetyExemptionReason.OTHER }

        fun JourneyData.getGasSafetyCertExemptionOtherReason() =
            JourneyDataHelper.getFieldStringValue(
                this,
                GasSafetyExemptionOtherReasonStep.ROUTE_SEGMENT,
                GasSafetyExemptionOtherReasonFormModel::otherReason.name,
            )

        fun JourneyData.getHasEICR() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                EicrStep.ROUTE_SEGMENT,
                EicrFormModel::hasCert.name,
            )

        fun JourneyData.getHasNewEICR() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateEICR.urlPathSegment,
                UpdateEicrFormModel::hasNewCertificate.name,
            )

        fun JourneyData.getEicrIssueDate() = this.getFieldSetLocalDateValue(EicrIssueDateStep.ROUTE_SEGMENT)

        fun JourneyData.getIsEicrOutdated(): Boolean? {
            val issueDate = this.getEicrIssueDate() ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= EICR_VALIDITY_YEARS
        }

        fun JourneyData.getEicrUploadId() =
            JourneyDataHelper.getFieldStringValue(
                this,
                EicrUploadStep.ROUTE_SEGMENT,
                EicrUploadCertificateFormModel::fileUploadId.name,
            )

        fun JourneyData.getHasEicrExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                EicrExemptionStep.ROUTE_SEGMENT,
                EicrExemptionFormModel::hasExemption.name,
            )

        fun JourneyData.getEicrExemptionReason() =
            JourneyDataHelper.getFieldEnumValue<EicrExemptionReason>(
                this,
                EicrExemptionReasonStep.ROUTE_SEGMENT,
                EicrExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getIsEicrExemptionReasonOther() = this.getEicrExemptionReason()?.let { it == EicrExemptionReason.OTHER }

        fun JourneyData.getEicrExemptionOtherReason() =
            JourneyDataHelper.getFieldStringValue(
                this,
                EicrExemptionOtherReasonStep.ROUTE_SEGMENT,
                EicrExemptionOtherReasonFormModel::otherReason.name,
            )

        fun JourneyData.getHasEPC() =
            JourneyDataHelper.getFieldEnumValue<HasEpc>(
                this,
                EpcQuestionStep.ROUTE_SEGMENT,
                EicrFormModel::hasCert.name,
            )

        fun JourneyData.getHasNewEPC() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateEpc.urlPathSegment,
                UpdateEpcFormModel::hasNewCertificate.name,
            )

        fun JourneyData.getEpcLookupCertificateNumber(stepId: PropertyComplianceStepId = PropertyComplianceStepId.EpcLookup): String? =
            JourneyDataHelper.getFieldStringValue(
                this,
                stepId.urlPathSegment,
                EpcLookupFormModel::certificateNumber.name,
            )

        fun JourneyData.getEpcDetails(autoMatched: Boolean): EpcDataModel? {
            val journeyDataKey = getEpcDetailsJourneyDataKey(autoMatched)
            val serializedEpcDetails = JourneyDataHelper.getStringValueByKey(this, journeyDataKey) ?: return null
            return Json.decodeFromString<EpcDataModel>(serializedEpcDetails)
        }

        fun epcDetailsDataPair(
            epcDetails: EpcDataModel?,
            autoMatched: Boolean,
        ): Pair<String, String?> {
            val journeyDataKey = getEpcDetailsJourneyDataKey(autoMatched)

            return if (epcDetails == null) {
                (journeyDataKey to null)
            } else {
                (journeyDataKey to Json.encodeToString(epcDetails))
            }
        }

        private fun getEpcDetailsJourneyDataKey(autoMatched: Boolean): String =
            if (autoMatched) {
                NonStepJourneyDataKey.AutoMatchedEpc.key
            } else {
                NonStepJourneyDataKey.LookedUpEpc.key
            }

        fun JourneyData.getLatestEpcCertificateNumber(): String? =
            this
                .getEpcDetails(autoMatched = false)
                ?.latestCertificateNumberForThisProperty
                ?.let { EpcDataModel.parseCertificateNumberOrNull(it) }

        fun JourneyData.getAcceptedEpcDetails(
            automatchedStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckAutoMatchedEpc,
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

        fun JourneyData.getAutoMatchedEpcIsCorrect(
            stepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckAutoMatchedEpc,
        ): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId.urlPathSegment,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.getMatchedEpcIsCorrect(stepId: String = CheckMatchedEpcStep.ROUTE_SEGMENT): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.withResetCheckMatchedEpc(matchedEpcStepId: String = CheckMatchedEpcStep.ROUTE_SEGMENT): JourneyData =
            this - matchedEpcStepId

        fun JourneyData.getEpcExemptionReason(stepRouteSegment: String = EpcExemptionReasonStep.ROUTE_SEGMENT): EpcExemptionReason? =
            JourneyDataHelper.getFieldEnumValue<EpcExemptionReason>(
                this,
                stepRouteSegment,
                EicrExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getDidTenancyStartBeforeEpcExpiry(stepRouteSegment: String = EpcExpiryCheckStep.ROUTE_SEGMENT): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepRouteSegment,
                EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name,
            )

        fun JourneyData.getPropertyHasMeesExemption(stepRouteSegment: String = MeesExemptionCheckStep.ROUTE_SEGMENT): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepRouteSegment,
                MeesExemptionCheckFormModel::propertyHasExemption.name,
            )

        fun JourneyData.getMeesExemptionReason(stepRouteSegment: String = MeesExemptionReasonStep.ROUTE_SEGMENT) =
            JourneyDataHelper.getFieldEnumValue<MeesExemptionReason>(
                this,
                stepRouteSegment,
                MeesExemptionReasonFormModel::exemptionReason.name,
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
