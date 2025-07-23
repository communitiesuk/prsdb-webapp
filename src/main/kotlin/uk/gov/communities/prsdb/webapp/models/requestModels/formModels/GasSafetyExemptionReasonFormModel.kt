package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class GasSafetyExemptionReasonFormModel : FormModel {
    @NotNull(message = "forms.gasSafetyExemptionReason.missing")
    var exemptionReason: GasSafetyExemptionReason? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): GasSafetyExemptionReasonFormModel? =
            record.gasSafetyCertExemptionReason?.let {
                GasSafetyExemptionReasonFormModel().apply {
                    this.exemptionReason = it
                }
            }
    }
}
