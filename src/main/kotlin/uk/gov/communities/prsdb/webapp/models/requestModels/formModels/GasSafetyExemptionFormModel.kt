package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class GasSafetyExemptionFormModel : FormModel {
    @NotNull(message = "forms.gasSafetyExemption.missing")
    var hasExemption: Boolean? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): GasSafetyExemptionFormModel? =
            if (record.gasSafetyCertIssueDate == null) {
                GasSafetyExemptionFormModel().apply {
                    this.hasExemption = record.hasGasSafetyExemption
                }
            } else {
                null
            }
    }
}
