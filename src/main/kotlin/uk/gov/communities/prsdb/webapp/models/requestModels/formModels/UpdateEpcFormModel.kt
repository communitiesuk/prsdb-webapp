package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class UpdateEpcFormModel : FormModel {
    @NotNull(message = "forms.update.epc.error.missing")
    var hasNewCertificate: Boolean? = null
}
