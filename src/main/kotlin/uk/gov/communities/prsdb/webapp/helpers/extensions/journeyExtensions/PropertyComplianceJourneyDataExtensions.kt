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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel
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

        fun JourneyData.getStillHasNoGasCertOrExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateGasSafety.urlPathSegment,
                ORIGINALLY_NOT_INCLUDED_KEY,
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

        fun JourneyData.getHasNewEICR() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateEICR.urlPathSegment,
                UpdateEicrFormModel::hasNewCertificate.name,
            )

        fun JourneyData.getStillHasNoEicrOrExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateEICR.urlPathSegment,
                ORIGINALLY_NOT_INCLUDED_KEY,
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

        fun JourneyData.getHasNewEPC() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateEpc.urlPathSegment,
                UpdateEpcFormModel::hasNewCertificate.name,
            )

        fun JourneyData.getStillHasNoEpcOrExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.UpdateEpc.urlPathSegment,
                ORIGINALLY_NOT_INCLUDED_KEY,
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

        fun JourneyData.getAcceptedEpcDetails(
            automatchedStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckAutoMatchedEpc,
            matchedStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckMatchedEpc,
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

        fun JourneyData.getMatchedEpcIsCorrect(stepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckMatchedEpc): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId.urlPathSegment,
                CheckMatchedEpcFormModel::matchedEpcIsCorrect.name,
            )

        fun JourneyData.withResetCheckMatchedEpc(
            matchedEpcStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckMatchedEpc,
        ): JourneyData = this - matchedEpcStepId.urlPathSegment

        fun JourneyData.getEpcExemptionReason(
            stepId: PropertyComplianceStepId = PropertyComplianceStepId.EpcExemptionReason,
        ): EpcExemptionReason? =
            JourneyDataHelper.getFieldEnumValue<EpcExemptionReason>(
                this,
                stepId.urlPathSegment,
                EicrExemptionReasonFormModel::exemptionReason.name,
            )

        fun JourneyData.getDidTenancyStartBeforeEpcExpiry(
            stepId: PropertyComplianceStepId = PropertyComplianceStepId.EpcExpiryCheck,
        ): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId.urlPathSegment,
                EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name,
            )

        fun JourneyData.getPropertyHasMeesExemption(
            stepId: PropertyComplianceStepId = PropertyComplianceStepId.MeesExemptionCheck,
        ): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                stepId.urlPathSegment,
                MeesExemptionCheckFormModel::propertyHasExemption.name,
            )

        fun JourneyData.getMeesExemptionReason(stepId: PropertyComplianceStepId = PropertyComplianceStepId.MeesExemptionReason) =
            JourneyDataHelper.getFieldEnumValue<MeesExemptionReason>(
                this,
                stepId.urlPathSegment,
                MeesExemptionReasonFormModel::exemptionReason.name,
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

        fun JourneyData.getHasCompletedGasSafetyUploadConfirmation() =
            this.containsKey(PropertyComplianceStepId.GasSafetyUploadConfirmation.urlPathSegment)

        fun JourneyData.getHasCompletedGasSafetyExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.GasSafetyExemptionConfirmation.urlPathSegment)

        fun JourneyData.getHasCompletedGasSafetyExemptionMissing() =
            this.containsKey(PropertyComplianceStepId.GasSafetyExemptionMissing.urlPathSegment)

        private fun JourneyData.getHasCompletedGasSafetyOutdated() =
            this.containsKey(PropertyComplianceStepId.GasSafetyOutdated.urlPathSegment)

        fun JourneyData.getHasCompletedEicrUploadConfirmation() =
            this.containsKey(PropertyComplianceStepId.EicrUploadConfirmation.urlPathSegment)

        fun JourneyData.getHasCompletedEicrExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.EicrExemptionConfirmation.urlPathSegment)

        fun JourneyData.getHasCompletedEicrExemptionMissing() =
            this.containsKey(PropertyComplianceStepId.EicrExemptionMissing.urlPathSegment)

        fun JourneyData.getHasCompletedEicrOutdated() = this.containsKey(PropertyComplianceStepId.EicrOutdated.urlPathSegment)

        fun JourneyData.getHasCompletedEpcExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.EpcExemptionConfirmation.urlPathSegment)

        fun JourneyData.getHasCompletedEpcMissing() = this.containsKey(PropertyComplianceStepId.EpcMissing.urlPathSegment)

        fun JourneyData.getHasCompletedEpcNotFound() = this.containsKey(PropertyComplianceStepId.EpcNotFound.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcTaskWithCheckAutoMatchedEpc(
            automatchedStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckAutoMatchedEpc,
        ) = this.containsKey(automatchedStepId.urlPathSegment) &&
            this.getAutoMatchedEpcIsCorrect(automatchedStepId)!! &&
            !this.getEpcDetails(autoMatched = true)!!.isPastExpiryDate() &&
            this.getEpcDetails(autoMatched = true)!!.isEnergyRatingEOrBetter()

        private fun JourneyData.getHasCompletedEpcTaskWithCheckMatchedEpc(
            matchedStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckMatchedEpc,
        ) = this.containsKey(matchedStepId.urlPathSegment) &&
            this.getMatchedEpcIsCorrect(matchedStepId)!! &&
            !this.getEpcDetails(autoMatched = false)!!.isPastExpiryDate() &&
            this.getEpcDetails(autoMatched = false)!!.isEnergyRatingEOrBetter()

        private fun JourneyData.getHasCompletedEpcTaskWithEpcExpiryCheck(
            automatchedStepId: PropertyComplianceStepId = PropertyComplianceStepId.CheckAutoMatchedEpc,
            epcExpiryCheckStepId: PropertyComplianceStepId = PropertyComplianceStepId.EpcExpiryCheck,
        ) = this.containsKey(epcExpiryCheckStepId.urlPathSegment) &&
            this.getDidTenancyStartBeforeEpcExpiry() == true &&
            this.getAcceptedEpcDetails(automatchedStepId)!!.isEnergyRatingEOrBetter()

        fun JourneyData.getHasCompletedEpcExpired() = this.containsKey(PropertyComplianceStepId.EpcExpired.urlPathSegment)

        private fun JourneyData.getHasCompletedMeesExemptionConfirmation() =
            this.containsKey(PropertyComplianceStepId.MeesExemptionConfirmation.urlPathSegment)

        private fun JourneyData.getHasCompletedEpcLowEnergyRating() =
            this.containsKey(PropertyComplianceStepId.LowEnergyRating.urlPathSegment)
    }
}
