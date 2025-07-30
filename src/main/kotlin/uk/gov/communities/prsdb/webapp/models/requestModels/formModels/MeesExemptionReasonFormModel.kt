package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class MeesExemptionReasonFormModel : FormModel {
    @NotNull(message = "forms.meesExemptionReason.error.missing")
    var exemptionReason: MeesExemptionReason? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): MeesExemptionReasonFormModel? =
            record.epcMeesExemptionReason?.let {
                MeesExemptionReasonFormModel().apply {
                    this.exemptionReason = it
                }
            }
    }
}
