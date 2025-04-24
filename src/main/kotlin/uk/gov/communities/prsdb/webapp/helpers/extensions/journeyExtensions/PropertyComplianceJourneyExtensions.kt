package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.yearsUntil
import org.apache.commons.io.FilenameUtils
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel

class PropertyComplianceJourneyExtensions : JourneyDataExtensions() {
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

        fun getGasSafetyCertFilename(
            propertyOwnershipId: Long,
            originalFileName: String,
        ) = "property_${propertyOwnershipId}_gas_safety_certificate.${FilenameUtils.getExtension(originalFileName)}"
    }
}
