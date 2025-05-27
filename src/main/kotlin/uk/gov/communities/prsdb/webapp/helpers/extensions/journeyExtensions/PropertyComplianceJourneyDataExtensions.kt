package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.yearsUntil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel

class PropertyComplianceJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getHasGasSafetyCert() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.GasSafety.urlPathSegment,
                GasSafetyFormModel::hasCert.name,
            )

        fun JourneyData.getIsGasSafetyCertOutdated(): Boolean? {
            val issueDate =
                this.getFieldSetLocalDateValue(PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment)
                    ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= 1
        }

        fun JourneyData.getHasGasSafetyCertExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.GasSafetyExemption.urlPathSegment,
                GasSafetyExemptionFormModel::hasExemption.name,
            )

        fun JourneyData.getIsGasSafetyExemptionReasonOther() =
            JourneyDataHelper
                .getFieldEnumValue<GasSafetyExemptionReason>(
                    this,
                    PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment,
                    GasSafetyExemptionReasonFormModel::exemptionReason.name,
                )?.let { it == GasSafetyExemptionReason.OTHER }

        fun JourneyData.getHasEICR() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.EICR.urlPathSegment,
                EicrFormModel::hasCert.name,
            )

        fun JourneyData.getIsEicrOutdated(): Boolean? {
            val issueDate =
                this.getFieldSetLocalDateValue(PropertyComplianceStepId.EicrIssueDate.urlPathSegment)
                    ?: return null
            val today = DateTimeHelper().getCurrentDateInUK()
            return issueDate.yearsUntil(today) >= 5
        }

        fun JourneyData.getHasEicrExemption() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.EicrExemption.urlPathSegment,
                EicrExemptionFormModel::hasExemption.name,
            )

        fun JourneyData.getIsEicrExemptionReasonOther() =
            JourneyDataHelper
                .getFieldEnumValue<EicrExemptionReason>(
                    this,
                    PropertyComplianceStepId.EicrExemptionReason.urlPathSegment,
                    EicrExemptionReasonFormModel::exemptionReason.name,
                )?.let { it == EicrExemptionReason.OTHER }

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

        fun JourneyData.getEpcDetails(): EpcDataModel? {
            val serializedEpcDetails = JourneyDataHelper.getStringValueByKey(this, LOOKED_UP_EPC_JOURNEY_DATA_KEY) ?: return null
            return Json.decodeFromString<EpcDataModel>(serializedEpcDetails)
        }

        fun JourneyData.withEpcDetails(epcDetails: EpcDataModel?): JourneyData =
            if (epcDetails == null) {
                this + (LOOKED_UP_EPC_JOURNEY_DATA_KEY to null)
            } else {
                this + (LOOKED_UP_EPC_JOURNEY_DATA_KEY to Json.encodeToString(epcDetails))
            }

        fun JourneyData.getHasFireSafetyDeclaration() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
                FireSafetyDeclarationFormModel::hasDeclared.name,
            )
    }
}
