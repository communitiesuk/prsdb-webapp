package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class EpcExpiryCheckFormModel : FormModel {
    @NotNull(message = "forms.epcExpiryCheck.missing")
    var tenancyStartedBeforeExpiry: Boolean? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): EpcExpiryCheckFormModel? =
            record.tenancyStartedBeforeEpcExpiry?.let {
                EpcExpiryCheckFormModel().apply {
                    this.tenancyStartedBeforeExpiry = it
                }
            }
    }
}
