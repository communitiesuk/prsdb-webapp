package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class MeesExemptionCheckFormModel : FormModel {
    @NotNull(message = "propertyCompliance.epcTask.meesExemptionCheck.error.missing")
    var propertyHasExemption: Boolean? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): MeesExemptionCheckFormModel? =
            MeesExemptionCheckFormModel().apply {
                this.propertyHasExemption = record.epcMeesExemptionReason != null
            }
    }
}
