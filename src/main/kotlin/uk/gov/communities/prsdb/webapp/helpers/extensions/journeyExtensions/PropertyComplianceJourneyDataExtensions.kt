package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.yearsUntil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.AUTO_MATCHED_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel

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
                AUTO_MATCHED_EPC_JOURNEY_DATA_KEY
            } else {
                LOOKED_UP_EPC_JOURNEY_DATA_KEY
            }

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

        fun JourneyData.getHasFireSafetyDeclaration() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
                FireSafetyDeclarationFormModel::hasDeclared.name,
            )
    }
}
