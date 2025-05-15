package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc

class EpcFormModel : FormModel {
    @NotNull(message = "forms.epc.error.missing")
    var hasCert: HasEpc? = null
}
