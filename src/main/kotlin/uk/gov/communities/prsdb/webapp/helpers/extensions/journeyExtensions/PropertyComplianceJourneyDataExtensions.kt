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
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel

class PropertyComplianceJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getHasGasSafetyCert() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.GasSafety.urlPathSegment,
                GasSafetyFormModel::hasCert.name,
            )

        fun JourneyData.getGasSafetyCertIssueDate() =
            this.getFieldSetLocalDateValue(PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment)

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

        fun JourneyData.getGasSafetyCertOriginalName() =
            JourneyDataHelper.getFieldStringValue(
                this,
                PropertyComplianceStepId.GasSafetyUpload.urlPathSegment,
                GasSafetyUploadCertificateFormModel::name.name,
            )

        fun JourneyData.getHasGasSafetyCertExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.GasSafetyExemption.urlPathSegment,
                GasSafetyExemptionFormModel::hasExemption.name,
            )

        fun JourneyData.getGasSafetyCertExemptionReason() =
            JourneyDataHelper.getFieldEnumValue<GasSafetyExemptionReason>(
                this,
                PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment,
                GasSafetyExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getIsGasSafetyExemptionReasonOther() =
            this.getGasSafetyCertExemptionReason()?.let { it == GasSafetyExemptionReason.OTHER }

        fun JourneyData.getGasSafetyCertExemptionOtherReason() =
            JourneyDataHelper.getFieldStringValue(
                this,
                PropertyComplianceStepId.GasSafetyExemptionOtherReason.urlPathSegment,
                GasSafetyExemptionOtherReasonFormModel::otherReason.name,
            )

        fun JourneyData.getHasEICR() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.EICR.urlPathSegment,
                EicrFormModel::hasCert.name,
            )

        fun JourneyData.getEicrIssueDate() = this.getFieldSetLocalDateValue(PropertyComplianceStepId.EicrIssueDate.urlPathSegment)

        fun JourneyData.getIsEicrOutdated(): Boolean? {
            val issueDate = this.getEicrIssueDate() ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= EICR_VALIDITY_YEARS
        }

        fun JourneyData.getEicrOriginalName() =
            JourneyDataHelper.getFieldStringValue(
                this,
                PropertyComplianceStepId.EicrUpload.urlPathSegment,
                EicrUploadCertificateFormModel::name.name,
            )

        fun JourneyData.getHasEicrExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.EicrExemption.urlPathSegment,
                EicrExemptionFormModel::hasExemption.name,
            )

        fun JourneyData.getEicrExemptionReason() =
            JourneyDataHelper.getFieldEnumValue<EicrExemptionReason>(
                this,
                PropertyComplianceStepId.EicrExemptionReason.urlPathSegment,
                EicrExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getIsEicrExemptionReasonOther() = this.getEicrExemptionReason()?.let { it == EicrExemptionReason.OTHER }

        fun JourneyData.getEicrExemptionOtherReason() =
            JourneyDataHelper.getFieldStringValue(
                this,
                PropertyComplianceStepId.EicrExemptionOtherReason.urlPathSegment,
                EicrExemptionOtherReasonFormModel::otherReason.name,
            )

        fun JourneyData.getHasEPC() =
            JourneyDataHelper.getFieldEnumValue<HasEpc>(
                this,
                PropertyComplianceStepId.EPC.urlPathSegment,
                EicrFormModel::hasCert.name,
            )

        fun JourneyData.getEpcLookupCertificateNumber(): String? =
            JourneyDataHelper.getFieldStringValue(
                this,
                PropertyComplianceStepId.EpcLookup.urlPathSegment,
                EpcLookupFormModel::certificateNumber.name,
            )

        fun JourneyData.getEpcDetails(autoMatched: Boolean): EpcDataModel? {
            val journeyDataKey = getEpcDetailsJourneyDataKey(autoMatched)
            val serializedEpcDetails = JourneyDataHelper.getStringValueByKey(this, journeyDataKey) ?: return null
            return Json.decodeFromString<EpcDataModel>(serializedEpcDetails)
        }

        fun JourneyData.withEpcDetails(
            epcDetails: EpcDataModel?,
            autoMatched: Boolean,
        ): JourneyData {
            val journeyDataKey = getEpcDetailsJourneyDataKey(autoMatched)

            return if (epcDetails == null) {
                this + (journeyDataKey to null)
            } else {
                this + (journeyDataKey to Json.encodeToString(epcDetails))
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

        fun JourneyData.getAcceptedEpcDetails(): EpcDataModel? {
            // Check the automatched EPC first, then the looked up EPC
            if (this.getAutoMatchedEpcIsCorrect() == true) {
                return this.getEpcDetails(autoMatched = true)
            }
            if (this.getMatchedEpcIsCorrect() == true) {
                return this.getEpcDetails(autoMatched = false)
            }
            return null
        }

        fun JourneyData.getAutoMatchedEpcIsCorrect(): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.CheckAutoMatchedEpc.urlPathSegment,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.getMatchedEpcIsCorrect(): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.withResetCheckMatchedEpc(): JourneyData = this - PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment

        fun JourneyData.getEpcExemptionReason(): EpcExemptionReason? =
            JourneyDataHelper.getFieldEnumValue<EpcExemptionReason>(
                this,
                PropertyComplianceStepId.EpcExemptionReason.urlPathSegment,
                EicrExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getDidTenancyStartBeforeEpcExpiry(): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment,
                EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name,
            )

        fun JourneyData.getHasFireSafetyDeclaration() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
                FireSafetyDeclarationFormModel::hasDeclared.name,
            )

        fun JourneyData.getResponsibilityToTenantsAgreement() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
                ResponsibilityToTenantsFormModel::agreesToResponsibility.name,
            )

        fun JourneyData.getHasCompletedGasSafetyTask() =
            this.getHasCompletedGasSafetyUploadConfirmation() ||
                this.getHasCompletedGasSafetyExemptionConfirmation() ||
                this.getHasCompletedGasSafetyExemptionMissing() ||
                this.getHasCompletedGasSafetyOutdated()

        fun JourneyData.getHasCompletedEicrTask() =
            this.getHasCompletedEicrUploadConfirmation() ||
                this.getHasCompletedEicrExemptionConfirmation() ||
                this.getHasCompletedEicrExemptionMissing() ||
                this.getHasCompletedEicrOutdated()

        fun JourneyData.getHasCompletedEpcTask() =
            this.getHasCompletedEpcExemptionConfirmation() ||
                this.getHasCompletedEpcMissing() ||
                this.getHasCompletedEpcNotFound() ||
                this.getHasCompletedEpcTaskWithCheckAutoMatchedEpc() ||
                this.getHasCompletedEpcTaskWithCheckMatchedEpc() ||
                this.getHasCompletedEpcTaskWithEpcExpiryCheck() ||
                this.getHasCompletedEpcExpired() ||
                this.getHasCompletedMeesExemptionConfirmation() ||
                this.getHasCompletedEpcLowEnergyRating()

        fun JourneyData.getHasCompletedLandlordsResponsibilitiesTask() = this.getResponsibilityToTenantsAgreement() ?: false

        private fun JourneyData.getHasCompletedGasSafetyUploadConfirmation() =
            this.containsKey(PropertyComplianceStepId.GasSafetyUploadConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedGasSafetyExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.GasSafetyExemptionConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedGasSafetyExemptionMissing() =
            this.containsKey(PropertyComplianceStepId.GasSafetyExemptionMissing.urlPathSegment)

        private fun JourneyData.getHasCompletedGasSafetyOutdated() =
            this.containsKey(PropertyComplianceStepId.GasSafetyOutdated.urlPathSegment)

        private fun JourneyData.getHasCompletedEicrUploadConfirmation() =
            this.containsKey(PropertyComplianceStepId.EicrUploadConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedEicrExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.EicrExemptionConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedEicrExemptionMissing() =
            this.containsKey(PropertyComplianceStepId.EicrExemptionMissing.urlPathSegment)

        private fun JourneyData.getHasCompletedEicrOutdated() = this.containsKey(PropertyComplianceStepId.EicrOutdated.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.EpcExemptionConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcMissing() = this.containsKey(PropertyComplianceStepId.EpcMissing.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcNotFound() = this.containsKey(PropertyComplianceStepId.EpcNotFound.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcTaskWithCheckAutoMatchedEpc() =
            this.containsKey(PropertyComplianceStepId.CheckAutoMatchedEpc.urlPathSegment) &&
                this.getAutoMatchedEpcIsCorrect()!! &&
                !this.getEpcDetails(autoMatched = true)!!.isPastExpiryDate() &&
                this.getEpcDetails(autoMatched = true)!!.isEnergyRatingEOrBetter()

        private fun JourneyData.getHasCompletedEpcTaskWithCheckMatchedEpc() =
            this.containsKey(PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment) &&
                this.getMatchedEpcIsCorrect()!! &&
                !this.getEpcDetails(autoMatched = false)!!.isPastExpiryDate() &&
                this.getEpcDetails(autoMatched = false)!!.isEnergyRatingEOrBetter()

        private fun JourneyData.getHasCompletedEpcTaskWithEpcExpiryCheck() =
            this.containsKey(PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment) &&
                this.getDidTenancyStartBeforeEpcExpiry() == true &&
                this.getAcceptedEpcDetails()!!.isEnergyRatingEOrBetter()

        private fun JourneyData.getHasCompletedEpcExpired() = this.containsKey(PropertyComplianceStepId.EpcExpired.urlPathSegment)

        private fun JourneyData.getHasCompletedMeesExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.MeesExemptionConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcLowEnergyRating() =
            this.containsKey(PropertyComplianceStepId.LowEnergyRating.urlPathSegment)
    }
}
