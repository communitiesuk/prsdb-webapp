package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class EpcExemptionReasonFormModel : FormModel {
    @NotNull(message = "forms.epcExemptionReason.missing")
    var exemptionReason: EpcExemptionReason? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): EpcExemptionReasonFormModel? =
            record.epcExemptionReason?.let {
                EpcExemptionReasonFormModel().apply {
                    this.exemptionReason = it
                }
            }
    }
}
